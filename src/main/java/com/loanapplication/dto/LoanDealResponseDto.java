package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDealResponseDto {

    private Integer dealId;

    private Integer customerId;

    private String loanType;

    private BigDecimal requestedLoanAmount;

    private BigDecimal eligibleLoanAmount;

    private BigDecimal approvedAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private Integer emiDay;

    private String bankName;

    private String accountHolderName;

    private String bankAccountNumber;

    private String ifscCode;

    private LocalDate preferredDisbursementDate;

    private Integer cibilScore;

    private String approvalType;

    private String status;
}