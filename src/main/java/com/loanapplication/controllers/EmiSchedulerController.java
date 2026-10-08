package com.loanapplication.controllers;


import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.EmiSchedluerService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/loan")
public class EmiSchedulerController {

    private final EmiSchedluerService emiSchedluerService;


    @PostMapping("{loanAccountId}/emi-schedule")
    public ResponseEntity<ApiResponse<List<EmiSchedulerResponseDto>>> generateEmiSchedule(@PathVariable int loanAccountId)
    {

        List<EmiSchedulerResponseDto> scheduleList  =   emiSchedluerService.generateEMiSceduleForEverymonth(loanAccountId);

         ApiResponse<List<EmiSchedulerResponseDto>>  response= new ApiResponse<>(true,"Emi schedule gerneted sucesfully ",scheduleList,null);

         return  ResponseEntity.ok(response);
    }

    @GetMapping("{loanId}/emi-schedule")

    public ResponseEntity<ApiResponse<Page<EmiSchedulerResponseDto>>> getEmiSchedlureForPArticularPerson
            (@PathVariable int loanId,@RequestParam (defaultValue = "0" )int page,@RequestParam (defaultValue = "10") int size ) {
        Pageable pageable = PageRequest.of(page, size);


        Page<EmiSchedulerResponseDto> scheduleList = emiSchedluerService.getEmiScheduleforParticularCustomer(loanId, pageable);

          ApiResponse<Page<EmiSchedulerResponseDto>> response =   new ApiResponse<>(true,"Emi Fetched SucesFully",scheduleList,null);

         return ResponseEntity.ok(response);
    }






}
