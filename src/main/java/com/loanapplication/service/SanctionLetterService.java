package com.loanapplication.service;

import com.loanapplication.dto.SanctionLetterResponseDto;

public interface SanctionLetterService {

    SanctionLetterResponseDto generateSanctionLetter(
            Integer dealId
    );

    SanctionLetterResponseDto getSanctionLetterByDealId(
            Integer dealId
    );
}