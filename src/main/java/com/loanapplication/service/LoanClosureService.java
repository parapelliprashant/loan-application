package com.loanapplication.service;

import com.loanapplication.dto.ForeClosureRequestDto;
import com.loanapplication.dto.ForeClosureResponseDto;

import java.math.BigDecimal;

public interface LoanClosureService {

    public ForeClosureResponseDto createForeClosureRequest(Integer loanAccountId,ForeClosureRequestDto foreClosureRequestDto);

    public BigDecimal calculateForeClosureAmount(Integer loanAccountId);


}