package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentHistoryResponseDto {

    private Integer paymentId;

    private BigDecimal paymentAmount;

    private LocalDateTime paymentDate;

    private String paymentStatus;

    private String paymentName;

    private String razorpayOrderId;

    private String razorpayPaymentId;
}