// ScoreCardService.java
package com.loanapplication.service;

import com.loanapplication.dto.ScoreCardResponseDto;

public interface ScoreCardService {

    ScoreCardResponseDto generateScoreCard(
            Integer customerId
    );

    ScoreCardResponseDto getScoreCard(
            Integer customerId
    );
}