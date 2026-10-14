package com.loanapplication.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CibilRequestDto {

    private Integer customerId;
    private String panNo;

    private BigDecimal grossMonthlySalary;
    private String employmentType;
    private Integer age;
    private BigDecimal totalMonthlyDebtPayment;
}