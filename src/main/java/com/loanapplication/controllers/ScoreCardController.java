// ScoreCardController.java
package com.loanapplication.controllers;
import com.loanapplication.dto.ScoreCardResponseDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.ScoreCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/scorecards")
public class ScoreCardController {
    private final ScoreCardService scoreCardService;
    public ScoreCardController(
            ScoreCardService scoreCardService) {
        this.scoreCardService =
                scoreCardService;
    }
    @PostMapping("/{customerId}/generate")
    public ResponseEntity<ApiResponse<ScoreCardResponseDto>>
    generateScoreCard(
            @PathVariable Integer customerId) {
        ScoreCardResponseDto responseDto =
                scoreCardService
                        .generateScoreCard(
                                customerId
                        );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Scorecard generated successfully",
                        responseDto,
                        null
                )
        );
    }
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<ScoreCardResponseDto>>
    getScoreCard(
            @PathVariable Integer customerId) {
        ScoreCardResponseDto responseDto =
                scoreCardService
                        .getScoreCard(
                                customerId
                        );
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Scorecard fetched successfully",
                        responseDto,
                        null
                )
        );
    }
}