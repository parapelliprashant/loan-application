package com.loanapplication.service.impl;

import com.loanapplication.dto.LoanDealRequestDto;
import com.loanapplication.dto.LoanDealResponseDto;
import com.loanapplication.dto.LoanReviewRequestDto;
import com.loanapplication.entities.Customer;
import com.loanapplication.entities.LoanDeal;
import com.loanapplication.entities.ScoreCard;
import com.loanapplication.exception.InvalidLoanStateException;
import com.loanapplication.exception.LoanEligibilityException;
import com.loanapplication.exception.LoanValidationException;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.CustomerRepo;
import com.loanapplication.repo.LoanDealRepo;
import com.loanapplication.repo.ScoreCardRepo;
import com.loanapplication.service.LoanDealService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class LoanDealServiceImpl implements LoanDealService {

    private final CustomerRepo customerRepo;
    private final ScoreCardRepo scoreRepo;
    private final LoanDealRepo dealRepo;
    private final ModelMapper mapper;

    public LoanDealServiceImpl(
            CustomerRepo customerRepo,
            ScoreCardRepo scoreRepo,
            LoanDealRepo dealRepo,
            ModelMapper mapper) {

        this.customerRepo = customerRepo;
        this.scoreRepo = scoreRepo;
        this.dealRepo = dealRepo;
        this.mapper = mapper;
    }

    @Override
    public LoanDealResponseDto applyForLoan(LoanDealRequestDto req) {

        if (req.getCustomerId() == null) {
            throw new LoanValidationException(
                    "Customer id is required"
            );
        }

        Customer customer =
                customerRepo.findById(req.getCustomerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + req.getCustomerId()
                                ));

        ScoreCard card =
                getCard(req.getCustomerId());

        if ("AUTO_REJECTED".equalsIgnoreCase(
                card.getCurrentStatus())) {

            throw new LoanEligibilityException(
                    "Customer is not eligible to apply for loan"
            );
        }

        validateAmount(
                req.getRequestedLoanAmount(),
                card.getEligibleLoanAmount()
        );

        validateLoanType(
                req.getLoanType()
        );

        validateTenure(
                req.getLoanType(),
                req.getTenureMonths()
        );

        validateEmiDay(
                req.getEmiDay()
        );

        validateBankDetails(req);

        LoanDeal deal =
                mapper.map(
                        req,
                        LoanDeal.class
                );

        deal.setDealId(null);
        deal.setCustomer(customer);
        deal.setLoanAmount(
                req.getRequestedLoanAmount()
        );
        deal.setInterestRate(
                getInterest(req.getLoanType())
        );

        String approval =
                getApproval(
                        card.getCibilScore()
                );

        if ("AUTO_APPROVED".equals(approval)) {

            deal.setDealStatus(
                    "APPROVED"
            );

            deal.setApprovedAmount(
                    deal.getLoanAmount()
            );

        } else {

            deal.setDealStatus(
                    "PENDING_REVIEW"
            );

            deal.setApprovedAmount(null);
        }

        return toDto(
                dealRepo.save(deal),
                card,
                approval
        );
    }

    @Override
    public LoanDealResponseDto getLoanDeal(Integer dealId) {

        LoanDeal deal =
                getDeal(dealId);

        ScoreCard card =
                getCard(
                        deal.getCustomer()
                                .getCustomerId()
                );

        return toDto(
                deal,
                card,
                getApproval(card.getCibilScore())
        );
    }

    @Override
    public List<LoanDealResponseDto> getCustomerLoanDeals(
            Integer customerId) {

        customerRepo.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: "
                                        + customerId
                        ));

        ScoreCard card =
                getCard(customerId);

        return dealRepo
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(deal ->
                        toDto(
                                deal,
                                card,
                                getApproval(
                                        card.getCibilScore()
                                )
                        )
                )
                .toList();
    }

    @Override
    public List<LoanDealResponseDto> getPendingLoanDeals() {

        return dealRepo
                .findByDealStatusIgnoreCase(
                        "PENDING_REVIEW"
                )
                .stream()
                .map(deal -> {

                    ScoreCard card =
                            getCard(
                                    deal.getCustomer()
                                            .getCustomerId()
                            );

                    return toDto(
                            deal,
                            card,
                            "MANUAL_REVIEW"
                    );
                })
                .toList();
    }

    @Override
    public LoanDealResponseDto reviewLoan(
            Integer dealId,
            LoanReviewRequestDto req) {

        LoanDeal deal =
                getDeal(dealId);

        if (!"PENDING_REVIEW".equalsIgnoreCase(
                deal.getDealStatus())) {

            throw new InvalidLoanStateException(
                    "Only loan applications pending review can be reviewed"
            );
        }

        if (req.getDecision() == null
                || req.getDecision().isBlank()) {

            throw new LoanValidationException(
                    "Decision is required"
            );
        }

        String decision =
                req.getDecision()
                        .trim()
                        .toUpperCase();

        if ("APPROVED".equals(decision)) {

            deal.setApprovedAmount(
                    deal.getLoanAmount()
            );

            deal.setDealStatus(
                    "APPROVED"
            );

        } else if ("REJECTED".equals(decision)) {

            deal.setApprovedAmount(
                    BigDecimal.ZERO
            );

            deal.setDealStatus(
                    "REJECTED"
            );

        } else {

            throw new LoanValidationException(
                    "Decision must be APPROVED or REJECTED"
            );
        }

        LoanDeal saved =
                dealRepo.save(deal);

        ScoreCard card =
                getCard(
                        saved.getCustomer()
                                .getCustomerId()
                );

        return toDto(
                saved,
                card,
                "MANUAL_REVIEW"
        );
    }

    private LoanDeal getDeal(Integer dealId) {

        return dealRepo
                .findById(dealId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan deal not found with id: "
                                        + dealId
                        ));
    }

    private ScoreCard getCard(Integer customerId) {

        return scoreRepo
                .findTopByCustomerCustomerIdOrderByAppliedDateDesc(
                        customerId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Scorecard not found for customer id: "
                                        + customerId
                        ));
    }

    private LoanDealResponseDto toDto(
            LoanDeal deal,
            ScoreCard card,
            String approval) {

        LoanDealResponseDto dto =
                mapper.map(
                        deal,
                        LoanDealResponseDto.class
                );

        dto.setCustomerId(
                deal.getCustomer()
                        .getCustomerId()
        );

        dto.setRequestedLoanAmount(
                deal.getLoanAmount()
        );

        dto.setEligibleLoanAmount(
                card.getEligibleLoanAmount()
        );

        dto.setCibilScore(
                card.getCibilScore()
        );

        dto.setApprovalType(
                approval
        );

        dto.setStatus(
                deal.getDealStatus()
        );

        return dto;
    }

    private void validateAmount(
            BigDecimal amount,
            BigDecimal eligibleAmount) {

        if (amount == null
                || amount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new LoanValidationException(
                    "Requested loan amount must be greater than zero"
            );
        }

        if (amount.compareTo(
                eligibleAmount
        ) > 0) {

            throw new LoanValidationException(
                    "Requested loan amount should not exceed eligible loan amount of "
                            + eligibleAmount
            );
        }
    }

    private void validateLoanType(String loanType) {

        if (loanType == null
                || loanType.isBlank()) {

            throw new LoanValidationException(
                    "Loan type is required"
            );
        }

        if (!"HOME_LOAN".equalsIgnoreCase(loanType)
                && !"VEHICLE_LOAN".equalsIgnoreCase(loanType)) {

            throw new LoanValidationException(
                    "Loan type must be HOME_LOAN or VEHICLE_LOAN"
            );
        }
    }

    private void validateTenure(
            String loanType,
            Integer tenure) {

        if (tenure == null) {

            throw new LoanValidationException(
                    "Loan tenure is required"
            );
        }

        if ("HOME_LOAN".equalsIgnoreCase(loanType)
                && tenure != 60
                && tenure != 120
                && tenure != 180
                && tenure != 240) {

            throw new LoanValidationException(
                    "Home loan tenure must be 60, 120, 180, or 240 months"
            );
        }

        if ("VEHICLE_LOAN".equalsIgnoreCase(loanType)
                && tenure != 12
                && tenure != 24
                && tenure != 36
                && tenure != 48
                && tenure != 60
                && tenure != 84) {

            throw new LoanValidationException(
                    "Vehicle loan tenure must be 12, 24, 36, 48, 60, or 84 months"
            );
        }
    }

    private void validateEmiDay(Integer day) {

        if (day == null
                || (day != 5
                && day != 7
                && day != 10
                && day != 15)) {

            throw new LoanValidationException(
                    "EMI day must be 5, 7, 10, or 15"
            );
        }
    }

    private void validateBankDetails(
            LoanDealRequestDto req) {

        if (req.getBankName() == null
                || req.getBankName().isBlank()) {

            throw new LoanValidationException(
                    "Bank name is required"
            );
        }

        if (req.getAccountHolderName() == null
                || req.getAccountHolderName().isBlank()) {

            throw new LoanValidationException(
                    "Account holder name is required"
            );
        }

        if (req.getBankAccountNumber() == null
                || req.getBankAccountNumber().isBlank()) {

            throw new LoanValidationException(
                    "Bank account number is required"
            );
        }

        if (req.getIfscCode() == null
                || req.getIfscCode().isBlank()) {

            throw new LoanValidationException(
                    "IFSC code is required"
            );
        }

        if (req.getPreferredDisbursementDate() == null) {

            throw new LoanValidationException(
                    "Preferred disbursement date is required"
            );
        }
    }

    private BigDecimal getInterest(String loanType) {

        return "HOME_LOAN".equalsIgnoreCase(loanType)
                ? new BigDecimal("15.00")
                : new BigDecimal("10.00");
    }

    private String getApproval(Integer score) {

        if (score > 800) return "AUTO_APPROVED";
        if (score >= 650) return "MANUAL_REVIEW";

        return "AUTO_REJECTED";
    }
}