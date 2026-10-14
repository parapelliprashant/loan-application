package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisbursementRequestDto {

    private Integer dealId;

    private BigDecimal disburseAmount;

    private String bankPartner;

    private LocalDateTime disbursementDate;

    private String status;
}