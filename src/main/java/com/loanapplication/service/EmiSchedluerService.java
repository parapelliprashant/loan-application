package com.loanapplication.service;

import com.loanapplication.dto.EmiSchedulerRequestDto;
import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;

import java.util.List;

public interface EmiSchedluerService {


    public List<EmiSchedulerResponseDto> generateEMiSceduleForEverymonth(EmiSchedulerRequestDto emiSchedulerRequestDto);



              }
