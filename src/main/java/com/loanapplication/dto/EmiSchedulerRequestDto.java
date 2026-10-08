package com.loanapplication.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmiSchedulerRequestDto {
        private BigDecimal principalAmount;
        private BigDecimal annualRate;
        private Integer tenure;
        private LocalDate firstDueDate;
    
}
