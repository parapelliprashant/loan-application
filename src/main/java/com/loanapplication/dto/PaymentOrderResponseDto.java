package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentOrderResponseDto {

    private Integer loanAccountId;

    private BigDecimal amount;

    private String razorpayOrderId;

    private String currency;

    private String paymentStatus;
}