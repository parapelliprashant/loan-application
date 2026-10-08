package com.loanapplication.service;


import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmiSchedluerService {


    public List<EmiSchedulerResponseDto> generateEMiSceduleForEverymonth(int loanAccountId);

    public Page<EmiSchedulerResponseDto> getEmiScheduleforParticularCustomer(int loanAccountId, Pageable pageable);


              }
