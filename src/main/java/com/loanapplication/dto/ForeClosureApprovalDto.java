package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForeClosureApprovalDto {

    private String status;
    private Integer userId;
    private String remarks;
}