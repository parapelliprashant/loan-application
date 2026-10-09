package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentPreviewResponseDto {

    private Integer loanAccountId;

    private List<EmiPaymentResponseDto> payableEmis;

    private BigDecimal totalEmiAmount;

    private BigDecimal totalPenaltyAmount;

    private BigDecimal totalPayableAmount;
}