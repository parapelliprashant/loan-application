package com.loanapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosureRequestDto {



        private Integer  loanAccountId;

        private String foreclosureType;

        private BigDecimal partialAmount;



        private LocalDateTime expectedClosureDate;

        private String reason;



}
