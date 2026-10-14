package com.loanapplication.service.impl;

import com.loanapplication.dto.ForeClosureRequestDto;
import com.loanapplication.dto.ForeClosureResponseDto;
import com.loanapplication.entities.ForeClosureRequest;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.repo.ForeClosureRequestRepo;
import com.loanapplication.repo.InterestAccuralRepo;
import com.loanapplication.repo.LoanAccountRepo;
import com.loanapplication.service.EmiSchedluerService;
import com.loanapplication.service.LoanClosureService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class LoanClosureServiceImpl implements LoanClosureService {

    private final LoanAccountRepo loanAccountRepo;

    private final InterestAccuralRepo interestAccuralRepo;
    private final ModelMapper modelMapper;
    private final ForeClosureRequestRepo foreClosureRequestRepo;
    private final EmiSchedluerService emiSchedluerService;



    @Override
    @Transactional
    public ForeClosureResponseDto createForeClosureRequest(Integer loanAccountId,ForeClosureRequestDto foreClosureRequestDto)
    {
        LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId).orElseThrow(() -> new RuntimeException("Loan account not found"));

        String type = foreClosureRequestDto.getForeclosureType();

        BigDecimal remainingPrincipal = null;
        BigDecimal foreclosureAmount;


        if("PARTIAL".equalsIgnoreCase(type))
        {
          BigDecimal partclosureAmount =           foreClosureRequestDto.getPartialAmount();
             BigDecimal outStandingAmount  =  loanAccount.getOutstandingPrincipal();
             remainingPrincipal = outStandingAmount.subtract(partclosureAmount);
            foreclosureAmount = remainingPrincipal;
            emiSchedluerService.generateEMiSceduleForEverymonth(loanAccountId);

        } else if ("FULL".equalsIgnoreCase(type)) {

            foreclosureAmount = calculateForeClosureAmount(loanAccountId);
        } else {
            throw new RuntimeException("Invalid foreclosure type. Must be FULL or PARTIAL.");
        }

            ForeClosureRequest request = modelMapper.map(foreClosureRequestDto, ForeClosureRequest.class);
            request.setLoanAccount(loanAccount);
            request.setForeclosureAmount(foreclosureAmount);
            request.setForeclosureType(type);
            request.setPaid(false);
            request.setPaidDate(null);
            request.setRequestedDate(LocalDateTime.now());
            request.setStatus("PENDING");

            ForeClosureRequest savedForeClosureRequestRepo = foreClosureRequestRepo.save(request);


        return modelMapper.map(savedForeClosureRequestRepo,ForeClosureResponseDto.class);

    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateForeClosureAmount(Integer loanAccountId) {


          LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId).orElseThrow(() -> new RuntimeException("Loan Account not found"));

        BigDecimal outStandingPrincipal =   loanAccount.getOutstandingPrincipal();

        BigDecimal interestAccural =                     interestAccuralRepo.calculateAccuredInterest(loanAccount.getLoanAccountId(), LocalDateTime.now(),"UNPAID");
        BigDecimal foreClosureCharges =  outStandingPrincipal.multiply(BigDecimal.valueOf(2)).divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);
        return  outStandingPrincipal.add(interestAccural).add(foreClosureCharges);





    }
}
