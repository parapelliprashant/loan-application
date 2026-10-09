package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponseDto {

    private Integer loanAccountId;

    private BigDecimal paymentAmount;

    private String razorpayPaymentId;

    private String paymentStatus;
}