package com.loanapplication.controllers;

import com.loanapplication.dto.LoanAccountResponseDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.LoanAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-accounts")
public class LoanAccountController {

    private final LoanAccountService loanAccountService;

    public LoanAccountController(
            LoanAccountService loanAccountService) {

        this.loanAccountService =
                loanAccountService;
    }

    @PostMapping("/deal/{dealId}")
    public ResponseEntity<ApiResponse<LoanAccountResponseDto>>
    createLoanAccount(
            @PathVariable Integer dealId) {

        LoanAccountResponseDto responseDto =
                loanAccountService
                        .createLoanAccount(
                                dealId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan account created successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ApiResponse<LoanAccountResponseDto>>
    getLoanAccountByDealId(
            @PathVariable Integer dealId) {

        LoanAccountResponseDto responseDto =
                loanAccountService
                        .getLoanAccountByDealId(
                                dealId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan account fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/account/{loanAccountNo}")
    public ResponseEntity<ApiResponse<LoanAccountResponseDto>>
    getLoanAccountByAccountNo(
            @PathVariable String loanAccountNo) {

        LoanAccountResponseDto responseDto =
                loanAccountService
                        .getLoanAccountByAccountNo(
                                loanAccountNo
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan account fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<LoanAccountResponseDto>>>
    getCustomerLoanAccounts(
            @PathVariable Integer customerId) {

        List<LoanAccountResponseDto> responseDto =
                loanAccountService
                        .getCustomerLoanAccounts(
                                customerId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer loan accounts fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanAccountResponseDto>>>
    getAllLoanAccounts() {

        List<LoanAccountResponseDto> responseDto =
                loanAccountService
                        .getAllLoanAccounts();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan accounts fetched successfully",
                        responseDto,
                        null
                )
        );
    }
}