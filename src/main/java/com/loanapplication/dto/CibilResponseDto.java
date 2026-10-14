package com.loanapplication.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CibilResponseDto {

    private Integer cibilReportId;
    private Integer customerId;
    private String panNo;

    private Integer incomeScore;
    private Integer employmentScore;
    private Integer ageScore;
    private Integer foirScore;

    private BigDecimal foirPercentage;

    private Integer cibilScore;
    private String cibilStatus;
    private String approvalRecommendation;

    private LocalDateTime checkDate;
    private String message;
}