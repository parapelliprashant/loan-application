package com.loanapplication.controllers;

import com.loanapplication.dto.CibilScoreRequest;
import com.loanapplication.dto.CibilScoreResponse;
import com.loanapplication.service.CibilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/cibil")
@RequiredArgsConstructor
public class CibilController {

    private final CibilService cibilService;

    @PostMapping
    public ResponseEntity<CibilScoreResponse> generateScore(
            @PathVariable Integer customerId
            ) {

        CibilScoreRequest request = new CibilScoreRequest(  //at the time of actuall merging then fetch it from session
                1,
                "ABCDE1234F",
                new BigDecimal("75000"),
                new BigDecimal("20000"),
                "private",
                LocalDate.of(1996, 5, 15)
        );
        if (!customerId.equals(request.getCustomerId())) {
            throw new IllegalArgumentException("Path customerId and request customerId must be the same");
        }
        return ResponseEntity.ok(cibilService.generateScore(request));
    }

    @GetMapping
    public ResponseEntity<CibilScoreResponse> getCurrentScore(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(cibilService.getCurrentScore(customerId));
    }


}
