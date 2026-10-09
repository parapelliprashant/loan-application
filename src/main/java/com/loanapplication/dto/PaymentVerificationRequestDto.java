package com.loanapplication.dto;

import lombok.Data;

@Data
public class PaymentVerificationRequestDto {

    private Integer loanAccountId;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;
}