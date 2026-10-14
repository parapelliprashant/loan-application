package com.loanapplication.service;

import com.loanapplication.dto.LoanDealRequestDto;
import com.loanapplication.dto.LoanDealResponseDto;
import com.loanapplication.dto.LoanReviewRequestDto;

import java.util.List;

public interface LoanDealService {

    LoanDealResponseDto applyForLoan(
            LoanDealRequestDto requestDto
    );

    LoanDealResponseDto getLoanDeal(
            Integer dealId
    );

    List<LoanDealResponseDto> getCustomerLoanDeals(
            Integer customerId
    );

    List<LoanDealResponseDto> getPendingLoanDeals();

    LoanDealResponseDto reviewLoan(
            Integer dealId,
            LoanReviewRequestDto requestDto
    );
}