package com.loanapplication.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @Min(18)
    private Integer age;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String mobileNo;

    @NotBlank
    private String pan;

    @NotBlank
    private String aadhaarNo;

    @NotBlank
    private String employmentType;

    @NotNull
    @PositiveOrZero
    private BigDecimal monthlyIncome;

    @NotNull
    @PositiveOrZero
    private BigDecimal monthlyExpense;
}