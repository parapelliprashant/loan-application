package com.loanapplication.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmiPaymentResponseDto {

    private Integer emiScheduleId;

    private Integer installmentNo;

    private LocalDate dueDate;

    private BigDecimal emiAmount;

    private BigDecimal penaltyAmount;

    private BigDecimal totalPayable;

    private String paymentStatus;

    private boolean canPay;
}