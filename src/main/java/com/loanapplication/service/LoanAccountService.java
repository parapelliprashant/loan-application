package com.loanapplication.service;

import com.loanapplication.dto.LoanAccountResponseDto;

import java.util.List;

public interface LoanAccountService {

    LoanAccountResponseDto createLoanAccount(
            Integer dealId
    );

    LoanAccountResponseDto getLoanAccountByDealId(
            Integer dealId
    );

    LoanAccountResponseDto getLoanAccountByAccountNo(
            String loanAccountNo
    );

    List<LoanAccountResponseDto> getCustomerLoanAccounts(
            Integer customerId
    );

    List<LoanAccountResponseDto> getAllLoanAccounts();
}