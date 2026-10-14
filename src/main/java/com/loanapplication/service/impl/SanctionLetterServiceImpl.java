package com.loanapplication.service.impl;

import com.loanapplication.dto.SanctionLetterResponseDto;
import com.loanapplication.entities.LoanDeal;
import com.loanapplication.entities.SanctionLetter;
import com.loanapplication.exception.DuplicateResourceException;
import com.loanapplication.exception.InvalidLoanStateException;
import com.loanapplication.exception.LoanValidationException;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.LoanDealRepo;
import com.loanapplication.repo.SanctionLetterRepo;
import com.loanapplication.service.SanctionLetterService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class SanctionLetterServiceImpl implements SanctionLetterService {

    private final LoanDealRepo dealRepo;
    private final SanctionLetterRepo sanctionRepo;
    private final ModelMapper mapper;

    public SanctionLetterServiceImpl(
            LoanDealRepo dealRepo,
            SanctionLetterRepo sanctionRepo,
            ModelMapper mapper) {

        this.dealRepo = dealRepo;
        this.sanctionRepo = sanctionRepo;
        this.mapper = mapper;
    }

    @Override
    public SanctionLetterResponseDto generateSanctionLetter(Integer dealId) {

        LoanDeal deal =
                dealRepo.findById(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with id: " + dealId
                                ));

        if (!"APPROVED".equalsIgnoreCase(
                deal.getDealStatus())) {

            throw new InvalidLoanStateException(
                    "Sanction letter can be generated only for approved loan deals"
            );
        }

        if (sanctionRepo
                .findTopByLoanDealDealIdOrderByCreatedAtDesc(dealId)
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Sanction letter already generated for deal id: " + dealId
            );
        }

        if (deal.getApprovedAmount() == null
                || deal.getApprovedAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new LoanValidationException(
                    "Approved loan amount is not available"
            );
        }

        if (deal.getInterestRate() == null) {
            throw new LoanValidationException(
                    "Interest rate is not available"
            );
        }

        if (deal.getTenureMonths() == null
                || deal.getTenureMonths() <= 0) {

            throw new LoanValidationException(
                    "Loan tenure is not available"
            );
        }

        BigDecimal emi =
                calculateEmi(
                        deal.getApprovedAmount(),
                        deal.getInterestRate(),
                        deal.getTenureMonths()
                );

        deal.setEmiAmount(emi);
        dealRepo.save(deal);

        SanctionLetter sanction =
                mapper.map(
                        deal,
                        SanctionLetter.class
                );

        sanction.setSanctionId(null);
        sanction.setLoanDeal(deal);
        sanction.setLoanAmount(
                deal.getApprovedAmount()
        );
        sanction.setEmiAmount(emi);
        sanction.setCreatedAt(
                LocalDateTime.now()
        );

        return toDto(
                sanctionRepo.save(sanction)
        );
    }

    @Override
    public SanctionLetterResponseDto getSanctionLetterByDealId(
            Integer dealId) {

        SanctionLetter sanction =
                sanctionRepo
                        .findTopByLoanDealDealIdOrderByCreatedAtDesc(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found for deal id: "
                                                + dealId
                                ));

        return toDto(sanction);
    }

    private BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualRate,
            Integer months) {

        MathContext mc =
                new MathContext(
                        20,
                        RoundingMode.HALF_UP
                );

        BigDecimal rate =
                annualRate
                        .divide(
                                BigDecimal.valueOf(12),
                                mc
                        )
                        .divide(
                                BigDecimal.valueOf(100),
                                mc
                        );

        BigDecimal power =
                BigDecimal.ONE
                        .add(rate)
                        .pow(
                                months,
                                mc
                        );

        BigDecimal emi =
                principal
                        .multiply(rate, mc)
                        .multiply(power, mc)
                        .divide(
                                power.subtract(
                                        BigDecimal.ONE
                                ),
                                mc
                        );

        return emi.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    private SanctionLetterResponseDto toDto(
            SanctionLetter sanction) {

        SanctionLetterResponseDto dto =
                mapper.map(
                        sanction,
                        SanctionLetterResponseDto.class
                );

        LoanDeal deal =
                sanction.getLoanDeal();

        dto.setDealId(
                deal.getDealId()
        );

        dto.setCustomerId(
                deal.getCustomer()
                        .getCustomerId()
        );

        dto.setLoanType(
                deal.getLoanType()
        );

        dto.setApprovedAmount(
                sanction.getLoanAmount()
        );

        return dto;
    }
}