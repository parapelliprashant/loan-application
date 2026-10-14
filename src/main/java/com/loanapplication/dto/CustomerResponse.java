package com.loanapplication.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CustomerResponse {

    private Integer customerId;
    private String firstName;
    private String lastName;
    private Integer age;
    private String email;
    private String mobileNo;
    private String aadhaarNo;
    private String pan;
    private String employmentType;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private Boolean emailVerified;
}