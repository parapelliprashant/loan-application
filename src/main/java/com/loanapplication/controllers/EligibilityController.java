// check for global exception handler
package com.loanapplication.controllers;
import com.loanapplication.dto.EligibilityResponseDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.EligibilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/eligibility")
public class EligibilityController {
    private final EligibilityService eligibilityService;
    public EligibilityController(
            EligibilityService eligibilityService) {

        this.eligibilityService =
                eligibilityService;
    }

    @PostMapping("/{customerId}/check")
    public ResponseEntity<ApiResponse<EligibilityResponseDto>>
    checkEligibility(
            @PathVariable Integer customerId) {

        EligibilityResponseDto responseDto =
                eligibilityService
                        .checkEligibility(
                                customerId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan eligibility decision completed successfully",
                        responseDto,
                        null
                )
        );
    }
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<EligibilityResponseDto>>
    getEligibility(
            @PathVariable Integer customerId) {

        EligibilityResponseDto responseDto =
                eligibilityService
                        .getEligibility(
                                customerId
                        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Loan eligibility fetched successfully",
                        responseDto,
                        null
                )
        );
    }
}