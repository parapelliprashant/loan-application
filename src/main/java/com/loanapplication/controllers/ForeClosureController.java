package com.loanapplication.controllers;

import com.loanapplication.dto.ForeClosureRequestDto;
import com.loanapplication.dto.ForeClosureResponseDto;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.LoanClosureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/loan")
public class ForeClosureController {

    private final LoanClosureService loanClosureService;

    @PostMapping("{loanId}/foreclosure/quote")
    public ResponseEntity<ApiResponse<ForeClosureResponseDto>> createForeClosureQuote(@PathVariable int loanId, @RequestBody ForeClosureRequestDto
            foreClosureRequestDto)
    {
        ForeClosureResponseDto foreClosureResponseDto =       loanClosureService.createForeClosureRequest(loanId,foreClosureRequestDto);

        ApiResponse<ForeClosureResponseDto> response = new ApiResponse<ForeClosureResponseDto>(true,"ForeClosure Created SucesFully",foreClosureResponseDto,null);

        return   ResponseEntity.ok(response);

    }

    @PostMapping("{foreClosureId}/foreclosure/approve")
    public ResponseEntity<ApiResponse<ForeClosureResponseDto>> processForeClosureQuote(@PathVariable Integer foreClosureId)
    {
        ForeClosureResponseDto foreClosureResponseDto =       loanClosureService.processForeClosureRequest(foreClosureId);
        ApiResponse<ForeClosureResponseDto> response = new ApiResponse<ForeClosureResponseDto>(true,"ForeClosure Created SucesFully",foreClosureResponseDto,null);

        return   ResponseEntity.ok(response);

    }




}
