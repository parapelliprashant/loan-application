package com.loanapplication.controllers;

import com.loanapplication.dto.DisbursementRequestDto;
import com.loanapplication.dto.DisbursementResponseDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.DisbursementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/disbursements")
public class DisbursementController {

    private final DisbursementService disbursementService;

    public DisbursementController(
            DisbursementService disbursementService) {

        this.disbursementService =
                disbursementService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DisbursementResponseDto>>
    processDisbursement(
            @RequestBody DisbursementRequestDto requestDto) {

        DisbursementResponseDto responseDto =
                disbursementService
                        .processDisbursement(
                                requestDto
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Disbursement processed successfully",
                        responseDto,
                        null
                )
        );
    }

    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ApiResponse<DisbursementResponseDto>>
    getDisbursementByDealId(
            @PathVariable Integer dealId) {

        DisbursementResponseDto responseDto =
                disbursementService
                        .getDisbursementByDealId(
                                dealId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Disbursement fetched successfully",
                        responseDto,
                        null
                )
        );
    }
}