// EligibilityService.java
package com.loanapplication.service;

import com.loanapplication.dto.EligibilityResponseDto;

public interface EligibilityService {

    EligibilityResponseDto checkEligibility(
            Integer customerId
    );

    EligibilityResponseDto getEligibility(
            Integer customerId
    );
}