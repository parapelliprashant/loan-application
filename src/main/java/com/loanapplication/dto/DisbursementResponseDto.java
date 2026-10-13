package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementResponseDto {

    private Integer disbursementId;

    private Integer dealId;

    private Integer customerId;

    private BigDecimal disburseAmount;

    private String bankPartner;

    private LocalDateTime disbursementDate;

    private String status;
}