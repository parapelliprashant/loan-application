
package com.loanapplication.controllers;

import com.loanapplication.dto.CibilRequestDto;
import com.loanapplication.dto.CibilResponseDto;
import com.loanapplication.service.CibilService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cibil")
public class CibilController {

    private final CibilService cibilService;

    public CibilController(CibilService cibilService) {
        this.cibilService = cibilService;
    }

    // Generate CIBIL Score
    @PostMapping("/generate")
    public ResponseEntity<CibilResponseDto> generateCibilScore(
            @RequestBody CibilRequestDto request) {

        return ResponseEntity.ok(
                cibilService.generateCibilScore(request)
        );
    }

    // Get Latest CIBIL Report by Customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CibilResponseDto> getLatestCibilReport(
            @PathVariable Integer customerId) {

        return ResponseEntity.ok(
                cibilService.getLatestCibilReport(customerId)
        );
    }

    // Get CIBIL Report by Report ID
    @GetMapping("/report/{reportId}")
    public ResponseEntity<CibilResponseDto> getCibilReportById(
            @PathVariable Integer reportId) {

        return ResponseEntity.ok(
                cibilService.getCibilReportById(reportId)
        );
    }
}
