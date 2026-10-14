package com.loanapplication.dto;

import com.loanapplication.entities.LoanAccount;
import com.loanapplication.entities.User;
import lombok.Data;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ForeClosureResponseDto {


        private Integer requestId;



        private String foreclosureType;


        private BigDecimal foreclosureAmount;


        private BigDecimal partialAmount;

        private LocalDateTime requestedDate;

        private LocalDateTime expectedClosureDate;

        private String reason;

        private String status;

        private LocalDateTime approvedDate;


        private Boolean paid = false;

        private LocalDateTime paidDate;



}
