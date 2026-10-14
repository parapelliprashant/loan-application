package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanDealRequestDto {

    private Integer customerId;

    private Integer scoreCardId;

    private String loanType;

    private BigDecimal requestedLoanAmount;

    private Integer tenureMonths;

    private Integer emiDay;

    private String bankName;

    private String accountHolderName;

    private String bankAccountNumber;

    private String ifscCode;

    private LocalDate preferredDisbursementDate;
}