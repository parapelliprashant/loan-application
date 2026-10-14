package com.loanapplication.service;

import com.loanapplication.dto.DisbursementRequestDto;
import com.loanapplication.dto.DisbursementResponseDto;

public interface DisbursementService {

    DisbursementResponseDto processDisbursement(
            DisbursementRequestDto requestDto
    );

    DisbursementResponseDto getDisbursementByDealId(
            Integer dealId
    );
}