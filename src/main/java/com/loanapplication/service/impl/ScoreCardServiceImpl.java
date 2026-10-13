package com.loanapplication.service.impl;

import com.loanapplication.dto.ScoreCardResponseDto;
import com.loanapplication.entities.EligibilityResult;
import com.loanapplication.entities.ScoreCard;
import com.loanapplication.exception.LoanEligibilityException;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.EligibilityResultRepo;
import com.loanapplication.repo.ScoreCardRepo;
import com.loanapplication.service.ScoreCardService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScoreCardServiceImpl implements ScoreCardService {

    private final EligibilityResultRepo eligibilityRepo;
    private final ScoreCardRepo scoreRepo;
    private final ModelMapper mapper;

    public ScoreCardServiceImpl(
            EligibilityResultRepo eligibilityRepo,
            ScoreCardRepo scoreRepo,
            ModelMapper mapper) {

        this.eligibilityRepo = eligibilityRepo;
        this.scoreRepo = scoreRepo;
        this.mapper = mapper;
    }

    @Override
    public ScoreCardResponseDto generateScoreCard(Integer customerId) {

        EligibilityResult result =
                eligibilityRepo
                        .findTopByCustomerCustomerIdOrderByEligibilityIdDesc(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer id: "
                                                + customerId
                                ));

        if (!Boolean.TRUE.equals(result.getEligible())) {
            throw new LoanEligibilityException(
                    "Scorecard cannot be generated because customer is not eligible"
            );
        }

        Integer score = result.getCibilScore();

        ScoreCard card =
                mapper.map(
                        result,
                        ScoreCard.class
                );

        card.setScoreCardId(null);
        card.setEligibleLoanAmount(
                result.getLoanAmount()
        );
        card.setRiskCategory(
                getStatus(score)
        );
        card.setCurrentStatus(
                getApproval(score)
        );
        card.setRejectionReason(null);
        card.setAppliedDate(
                LocalDateTime.now()
        );

        return toDto(
                scoreRepo.save(card)
        );
    }

    @Override
    public ScoreCardResponseDto getScoreCard(Integer customerId) {

        ScoreCard card =
                scoreRepo
                        .findTopByCustomerCustomerIdOrderByAppliedDateDesc(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Scorecard not found for customer id: "
                                                + customerId
                                ));

        return toDto(card);
    }

    private ScoreCardResponseDto toDto(ScoreCard card) {

        ScoreCardResponseDto dto =
                mapper.map(
                        card,
                        ScoreCardResponseDto.class
                );

        dto.setCustomerId(
                card.getCustomer().getCustomerId()
        );

        dto.setCibilStatus(
                card.getRiskCategory()
        );

        dto.setApprovalType(
                getApproval(
                        card.getCibilScore()
                )
        );

        return dto;
    }

    private String getStatus(Integer score) {

        if (score >= 900) return "EXCELLENT";
        if (score >= 800) return "VERY_GOOD";
        if (score >= 750) return "GOOD";
        if (score >= 700) return "AVERAGE";
        if (score >= 650) return "RISKY";
        return "REJECTED";
    }

    private String getApproval(Integer score) {
        if (score > 800) return "AUTO_APPROVED";
        if (score >= 650) return "MANUAL_REVIEW";
        return "AUTO_REJECTED";
    }
}