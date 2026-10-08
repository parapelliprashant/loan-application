package com.loanapplication.controllers;


import com.loanapplication.dto.EmiSchedulerRequestDto;
import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.EmiSchedluerService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/loan")
public class EmiSchedulerController {

    private final EmiSchedluerService emiSchedluerService;


    @PostMapping("/generateemischedule")
    public ResponseEntity<ApiResponse<List<EmiSchedulerResponseDto>>> generateEmiSchedule(@RequestBody EmiSchedulerRequestDto emiSchedulerRequestDto)
    {

         List<EmiSchedulerResponseDto> scheduleList =     emiSchedluerService.generateEMiSceduleForEverymonth(emiSchedulerRequestDto);

         ApiResponse<List<EmiSchedulerResponseDto>>  response= new ApiResponse<>(true,"Emi schedule gerneted sucesfully ",scheduleList,null);

         return  ResponseEntity.ok(response);
    }

}
