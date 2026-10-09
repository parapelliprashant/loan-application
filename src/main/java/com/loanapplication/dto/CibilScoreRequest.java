package com.loanapplication.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CibilScoreRequest {

    @NotNull
    private Integer customerId;

    private String panNo;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monthlyIncome;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal monthlyExpenses;

    @NotBlank
    private String employmentType;

    @NotNull
    @Past
    private LocalDate dob;
}
