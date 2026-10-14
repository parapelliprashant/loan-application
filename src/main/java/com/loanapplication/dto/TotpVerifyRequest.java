package com.loanapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TotpVerifyRequest {

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String code;
}