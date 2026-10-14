
package com.loanapplication.service;

import com.loanapplication.dto.CibilRequestDto;
import com.loanapplication.dto.CibilResponseDto;

public interface CibilService {

    CibilResponseDto generateCibilScore(CibilRequestDto request);

    CibilResponseDto getLatestCibilReport(Integer customerId);

    CibilResponseDto getCibilReportById(Integer reportId);
}
