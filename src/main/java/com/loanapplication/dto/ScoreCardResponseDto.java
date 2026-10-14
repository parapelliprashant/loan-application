
package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCardResponseDto {

    private Integer scoreCardId;
    private Integer customerId;
    private Integer cibilScore;
    private String cibilStatus;
    private BigDecimal eligibleLoanAmount;
    private String approvalType;
    private String currentStatus;
    private String rejectionReason;
    private LocalDateTime appliedDate;
}