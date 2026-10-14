package com.loanapplication.service.impl;
import com.loanapplication.dto.EmiSchedulerResponseDto;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.repo.EmiSchedulerRepo;
import com.loanapplication.repo.LoanAccountRepo;
import com.loanapplication.service.EmiSchedluerService;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmiSchedulerImpl implements EmiSchedluerService {

    private final ModelMapper modelMapper;
    private final EmiSchedulerRepo emiSchedulerRepo;
    private final LoanAccountRepo loanAccountRepo;


    @Override
    public List<EmiSchedulerResponseDto> generateEMiSceduleForEverymonth(int loanAccountId) {

          LoanAccount loanAccount =  loanAccountRepo.findById(loanAccountId).orElseThrow(()-> new RuntimeException(""));

        BigDecimal principal = loanAccount.getLoanAmount();
        BigDecimal annualRate = loanAccount.getInterestRate();
        int tenure = loanAccount.getTenureMonths();
        LocalDate emiDate = loanAccount.getDisbursementDate().plusMonths(1).toLocalDate();

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
               schedule.setLoanAccount(loanAccount);
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

                emiSchedulerRepo.saveAll(emiSchedulesList);


           return emiSchedulesList.stream().map(schedule -> modelMapper.map(schedule, EmiSchedulerResponseDto.class))
                   .collect(Collectors.toList());




    }

    @Override
    public Page<EmiSchedulerResponseDto> getEmiScheduleforParticularCustomer(int loanId, Pageable pageable) {

          Page<EmiSchedule> emiScheduleList =      emiSchedulerRepo.findEmiSchedulesByLoanId(loanId,pageable);

          return  emiScheduleList.map(emiSchedule -> modelMapper.map(emiScheduleList, EmiSchedulerResponseDto.class));
    }


}
