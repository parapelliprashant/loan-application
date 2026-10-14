package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanctionLetterResponseDto {

    private Integer sanctionId;

    private Integer dealId;

    private Integer customerId;

    private String loanType;

    private BigDecimal approvedAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal emiAmount;

    private LocalDateTime createdAt;
}