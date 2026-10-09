package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CibilScoreResponse {

    private Integer customerId;
    private Integer cibilScore;
    private String status;
    private Integer incomeScore;
    private Integer employmentScore;
    private Integer ageScore;
    private Integer foirScore;
    private Double foirPercentage;
}
