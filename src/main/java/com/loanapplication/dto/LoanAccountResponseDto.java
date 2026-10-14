package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountResponseDto {

    private Integer loanAccountId;

    private String loanAccountNo;

    private Integer dealId;

    private Integer customerId;

    private String customerName;

    private BigDecimal loanAmount;

    private BigDecimal outstandingPrincipal;

    private String loanStatus;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal emiAmount;

    private LocalDateTime disbursementDate;

    private BigDecimal totalPaidAmount;

    private LocalDateTime createdAt;
}