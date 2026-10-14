package com.loanapplication.service.impl;

import com.loanapplication.dto.EligibilityResponseDto;
import com.loanapplication.entities.CibilReport;
import com.loanapplication.entities.Customer;
import com.loanapplication.entities.EligibilityResult;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.CibilReportRepo;
import com.loanapplication.repo.CustomerRepo;
import com.loanapplication.repo.EligibilityResultRepo;
import com.loanapplication.service.EligibilityService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EligibilityServiceImpl implements EligibilityService {

    private final CustomerRepo customerRepo;
    private final CibilReportRepo cibilReportRepo;
    private final EligibilityResultRepo eligibilityResultRepo;
    private final ModelMapper modelMapper;

    public EligibilityServiceImpl(CustomerRepo customerRepo,
                                  CibilReportRepo cibilReportRepo,
                                  EligibilityResultRepo eligibilityResultRepo,
                                  ModelMapper modelMapper) {
        this.customerRepo = customerRepo;
        this.cibilReportRepo = cibilReportRepo;
        this.eligibilityResultRepo = eligibilityResultRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    public EligibilityResponseDto checkEligibility(Integer customerId) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: " + customerId
                        ));

        CibilReport report = cibilReportRepo
                .findTopByCustomerCustomerIdOrderByCheckDateDesc(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "CIBIL report not found for customer id: " + customerId
                        ));

        Integer score = report.getCibilScore();

        if (score == null) {
            throw new ResourceNotFoundException(
                    "CIBIL score not available for customer id: " + customerId
            );
        }

        EligibilityResult result = new EligibilityResult();

        result.setCustomer(customer);
        result.setCibilScore(score);
        result.setEligible(score >= 650);
        result.setLoanAmount(getEligibleAmount(score));
        result.setRejectionReason(
                score < 650
                        ? "Customer is not eligible because CIBIL score is below 650"
                        : null
        );

        return toDto(
                eligibilityResultRepo.save(result)
        );
    }

    @Override
    public EligibilityResponseDto getEligibility(Integer customerId) {

        EligibilityResult result =
                eligibilityResultRepo
                        .findTopByCustomerCustomerIdOrderByEligibilityIdDesc(customerId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Eligibility result not found for customer id: "
                                                + customerId
                                ));

        return toDto(result);
    }

    private EligibilityResponseDto toDto(
            EligibilityResult result) {

        EligibilityResponseDto dto =
                modelMapper.map(
                        result,
                        EligibilityResponseDto.class
                );

        dto.setCustomerId(
                result.getCustomer().getCustomerId()
        );

        dto.setEligibleLoanAmount(
                result.getLoanAmount()
        );

        dto.setCibilStatus(
                getCibilStatus(
                        result.getCibilScore()
                )
        );

        dto.setApprovalType(
                getApprovalType(
                        result.getCibilScore()
                )
        );

        return dto;
    }

    private BigDecimal getEligibleAmount(Integer score) {

        if (score >= 800) {
            return new BigDecimal("10000000");
        }

        if (score >= 750) {
            return new BigDecimal("7500000");
        }

        if (score >= 700) {
            return new BigDecimal("5000000");
        }

        if (score >= 650) {
            return new BigDecimal("2500000");
        }

        return BigDecimal.ZERO;
    }

    private String getCibilStatus(Integer score) {

        if (score >= 900) return "EXCELLENT";
        if (score >= 800) return "VERY_GOOD";
        if (score >= 750) return "GOOD";
        if (score >= 700) return "AVERAGE";
        if (score >= 650) return "RISKY";

        return "REJECTED";
    }

    private String getApprovalType(Integer score) {

        if (score > 800) return "AUTO_APPROVED";
        if (score >= 650) return "MANUAL_REVIEW";

        return "AUTO_REJECTED";
    }
}