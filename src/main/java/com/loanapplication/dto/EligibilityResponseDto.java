package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponseDto {

    private Integer eligibilityId;
    private Integer customerId;
    private Integer cibilScore;
    private String cibilStatus;
    private Boolean eligible;
    private BigDecimal eligibleLoanAmount;
    private String approvalType;
    private String rejectionReason;
}