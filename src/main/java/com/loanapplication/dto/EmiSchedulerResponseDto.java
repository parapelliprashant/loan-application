package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class EmiSchedulerResponseDto {


    private Integer emiScheduleId;
    private Integer loanAccountId;
    private Integer installmentNo;
    private LocalDate dueDate;
    private BigDecimal openingBalance;
    private BigDecimal interestAmount;
    private BigDecimal principalAmount;
    private BigDecimal closingBalance;
    private BigDecimal emi;
    private String paymentStatus;
    private LocalDateTime paidDate;
    private String cancellationReason;
}
