package com.loanapplication.controllers;

import com.loanapplication.dto.LoanDealRequestDto;
import com.loanapplication.dto.LoanDealResponseDto;
import com.loanapplication.dto.LoanReviewRequestDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.LoanDealService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-deals")
public class LoanDealController {

    private final LoanDealService loanDealService;

    public LoanDealController(
            LoanDealService loanDealService) {

        this.loanDealService =
                loanDealService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LoanDealResponseDto>>
    applyForLoan(
            @RequestBody LoanDealRequestDto requestDto) {

        LoanDealResponseDto responseDto =
                loanDealService
                        .applyForLoan(
                                requestDto
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan application submitted successfully",
                        responseDto,
                        null
                )
        );
    }

    /*
     * Officer-side Pending Deals Screen
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LoanDealResponseDto>>>
    getPendingLoanDeals() {

        List<LoanDealResponseDto> responseDto =
                loanDealService
                        .getPendingLoanDeals();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Pending loan deals fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/{dealId}")
    public ResponseEntity<ApiResponse<LoanDealResponseDto>>
    getLoanDeal(
            @PathVariable Integer dealId) {

        LoanDealResponseDto responseDto =
                loanDealService
                        .getLoanDeal(
                                dealId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan deal fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<LoanDealResponseDto>>>
    getCustomerLoanDeals(
            @PathVariable Integer customerId) {

        List<LoanDealResponseDto> responseDto =
                loanDealService
                        .getCustomerLoanDeals(
                                customerId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Customer loan deals fetched successfully",
                        responseDto,
                        null
                )
        );
    }

    @PutMapping("/{dealId}/review")
    public ResponseEntity<ApiResponse<LoanDealResponseDto>>
    reviewLoan(
            @PathVariable Integer dealId,
            @RequestBody LoanReviewRequestDto requestDto) {

        LoanDealResponseDto responseDto =
                loanDealService
                        .reviewLoan(
                                dealId,
                                requestDto
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan review completed successfully",
                        responseDto,
                        null
                )
        );
    }
}