package com.loanapplication.service.impl;

import com.loanapplication.dto.EmiSchedulerRequestDto;
import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.service.EmiSchedluerService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmiSchedulerImpl implements EmiSchedluerService {

    private final ModelMapper modelMapper;


    @Override
    public List<EmiSchedulerResponseDto> generateEMiSceduleForEverymonth(EmiSchedulerRequestDto emiSchedulerRequestDto) {

        BigDecimal principal = emiSchedulerRequestDto.getPrincipalAmount();
        BigDecimal annualRate = emiSchedulerRequestDto.getAnnualRate();
        int tenure = emiSchedulerRequestDto.getTenure();
        LocalDate emiDate = emiSchedulerRequestDto.getFirstDueDate();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12),10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);

        double p = principal.doubleValue();
                double r =monthlyRate.doubleValue();
                        double n = tenure;

        double equatedmonthlyInstalment  = (p* r * Math.pow(1+r,n))/(Math.pow(1+r,n)-1);

        BigDecimal emi = BigDecimal.valueOf(equatedmonthlyInstalment).setScale(2,RoundingMode.HALF_UP);



        List<EmiSchedule> emiSchedulesList = new ArrayList<>();
        BigDecimal openingBalance = principal;

           for(int i = 1;i<=tenure;i++) {
               BigDecimal interestAmount = openingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
               BigDecimal principalAmount = emi.subtract(interestAmount);

               if (i == tenure) {
                   principalAmount = openingBalance;
                   emi = principalAmount.add(interestAmount);
               }


               BigDecimal closingBalance = openingBalance.subtract(principalAmount);
               if (closingBalance.compareTo(BigDecimal.ZERO) < 0) {
                   closingBalance = BigDecimal.ZERO.setScale(2);
               }


               EmiSchedule schedule = new EmiSchedule();
               schedule.setLoanAccount(null);
               schedule.setInstallmentNo(i);
               schedule.setDueDate(emiDate);
               schedule.setOpeningBalance(openingBalance);
               schedule.setInterestAmount(interestAmount);
               schedule.setPrincipalAmount(principalAmount);
               schedule.setClosingBalance(closingBalance);
               schedule.setEmi(emi);
               schedule.setPaymentStatus("PENDING");

               emiSchedulesList.add(schedule);

               openingBalance = closingBalance;
               emiDate = emiDate.plusMonths(1);
           }


           return emiSchedulesList.stream().map(schedule -> modelMapper.map(schedule, EmiSchedulerResponseDto.class))
                   .collect(Collectors.toList());




    }
}
