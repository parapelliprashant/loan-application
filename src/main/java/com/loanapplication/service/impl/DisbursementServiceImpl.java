package com.loanapplication.service.impl;

import com.loanapplication.dto.DisbursementRequestDto;
import com.loanapplication.dto.DisbursementResponseDto;
import com.loanapplication.entities.Disbursement;
import com.loanapplication.entities.LoanDeal;
import com.loanapplication.exception.DuplicateResourceException;
import com.loanapplication.exception.InvalidLoanStateException;
import com.loanapplication.exception.LoanValidationException;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.DisbursementRepo;
import com.loanapplication.repo.LoanDealRepo;
import com.loanapplication.repo.SanctionLetterRepo;
import com.loanapplication.service.DisbursementService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class DisbursementServiceImpl implements DisbursementService {

    private final DisbursementRepo disbRepo;
    private final LoanDealRepo dealRepo;
    private final SanctionLetterRepo sanctionRepo;
    private final ModelMapper mapper;

    public DisbursementServiceImpl(
            DisbursementRepo disbRepo,
            LoanDealRepo dealRepo,
            SanctionLetterRepo sanctionRepo,
            ModelMapper mapper) {

        this.disbRepo = disbRepo;
        this.dealRepo = dealRepo;
        this.sanctionRepo = sanctionRepo;
        this.mapper = mapper;
    }

    @Override
    public DisbursementResponseDto processDisbursement(
            DisbursementRequestDto req) {

        if (req.getDealId() == null) {
            throw new LoanValidationException(
                    "Deal id is required"
            );
        }

        LoanDeal deal =
                dealRepo.findById(req.getDealId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with id: "
                                                + req.getDealId()
                                ));

        if (!"APPROVED".equalsIgnoreCase(
                deal.getDealStatus())) {

            throw new InvalidLoanStateException(
                    "Disbursement can be processed only for approved loan deals"
            );
        }

        if (sanctionRepo
                .findTopByLoanDealDealIdOrderByCreatedAtDesc(
                        req.getDealId()
                )
                .isEmpty()) {

            throw new InvalidLoanStateException(
                    "Sanction letter must be generated before disbursement"
            );
        }

        disbRepo
                .findTopByLoanDealDealIdOrderByDisbursementDateDesc(
                        req.getDealId()
                )
                .ifPresent(disb -> {

                    if ("SUCCESS".equalsIgnoreCase(
                            disb.getStatus())) {

                        throw new DuplicateResourceException(
                                "Loan has already been successfully disbursed for deal id: "
                                        + req.getDealId()
                        );
                    }
                });

        validateAmount(
                req.getDisburseAmount(),
                deal.getApprovedAmount()
        );

        if (req.getBankPartner() == null
                || req.getBankPartner().isBlank()) {

            throw new LoanValidationException(
                    "Bank partner is required"
            );
        }

        String status =
                getStatus(
                        req.getStatus()
                );

        LocalDateTime date =
                req.getDisbursementDate() != null
                        ? req.getDisbursementDate()
                        : LocalDateTime.now();

        if ("SUCCESS".equals(status)
                && date.isAfter(
                LocalDateTime.now()
        )) {

            throw new LoanValidationException(
                    "Successful disbursement date cannot be in the future"
            );
        }

        Disbursement disb =
                mapper.map(
                        req,
                        Disbursement.class
                );

        disb.setDisbursementId(null);
        disb.setLoanDeal(deal);
        disb.setDisbursementDate(date);
        disb.setStatus(status);

        return toDto(
                disbRepo.save(disb)
        );
    }

    @Override
    public DisbursementResponseDto getDisbursementByDealId(
            Integer dealId) {

        Disbursement disb =
                disbRepo
                        .findTopByLoanDealDealIdOrderByDisbursementDateDesc(
                                dealId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disbursement not found for deal id: "
                                                + dealId
                                ));

        return toDto(disb);
    }

    private void validateAmount(
            BigDecimal amount,
            BigDecimal approvedAmount) {

        if (amount == null
                || amount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new LoanValidationException(
                    "Disbursement amount must be greater than zero"
            );
        }

        if (approvedAmount == null
                || approvedAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new InvalidLoanStateException(
                    "Approved loan amount is not available"
            );
        }

        if (amount.compareTo(
                approvedAmount
        ) != 0) {

            throw new LoanValidationException(
                    "Disbursement amount must be equal to approved loan amount"
            );
        }
    }

    private String getStatus(String status) {

        if (status == null
                || status.isBlank()) {

            throw new LoanValidationException(
                    "Disbursement status is required"
            );
        }

        String value =
                status.trim()
                        .toUpperCase();

        if (!"SUCCESS".equals(value)
                && !"FAILED".equals(value)
                && !"PENDING".equals(value)) {

            throw new LoanValidationException(
                    "Status must be SUCCESS, FAILED, or PENDING"
            );
        }

        return value;
    }

    private DisbursementResponseDto toDto(
            Disbursement disb) {

        DisbursementResponseDto dto =
                mapper.map(
                        disb,
                        DisbursementResponseDto.class
                );

        dto.setDealId(
                disb.getLoanDeal()
                        .getDealId()
        );

        dto.setCustomerId(
                disb.getLoanDeal()
                        .getCustomer()
                        .getCustomerId()
        );

        return dto;
    }
}