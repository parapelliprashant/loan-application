package com.loanapplication.service.impl;

import com.loanapplication.dto.ForeClosureApprovalDto;
import com.loanapplication.dto.ForeClosureRequestDto;
import com.loanapplication.dto.ForeClosureResponseDto;
import com.loanapplication.entities.ForeClosureRequest;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.repo.EmiSchedulerRepo;
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
    private final EmiSchedulerRepo emiSchedulerRepo;



    @Override
    @Transactional
    public ForeClosureResponseDto createForeClosureRequest(Integer loanAccountId,ForeClosureRequestDto foreClosureRequestDto)
    {
        LoanAccount loanAccount = loanAccountRepo.findById(loanAccountId).orElseThrow(() -> new RuntimeException("Loan account not found"));

        String type = foreClosureRequestDto.getForeclosureType();

        BigDecimal remainingPrincipal = null;
        BigDecimal foreClosureAmount;
        BigDecimal partialAmount ;


        if("PARTIAL".equalsIgnoreCase(type))
        {
            partialAmount =         foreClosureRequestDto.getPartialAmount();

            if(partialAmount == null || partialAmount.compareTo(BigDecimal.ZERO) <=0)
            {
                throw  new RuntimeException("Inavlid Partial Amount ");
            }
            if(partialAmount.compareTo(loanAccount.getEmiAmount()) < 0)
            {
                throw  new RuntimeException("Amount Should be greater than 1 Emi  ");
            }

            foreClosureAmount = null;
            if (partialAmount.compareTo(loanAccount.getOutstandingPrincipal()) >= 0)
            {
                throw  new RuntimeException("Partial Amount cannot be greater than Outstanding Amount ");
            }


        } else if ("FULL".equalsIgnoreCase(type)) {

            foreClosureAmount = calculateForeClosureAmount(loanAccountId);
            partialAmount = null;
        } else {
            throw new RuntimeException("Invalid foreclosur Type");
        }

        ForeClosureRequest request = modelMapper.map(foreClosureRequestDto, ForeClosureRequest.class);
        request.setLoanAccount(loanAccount);
        request.setForeclosureAmount(foreClosureAmount);
        request.setPartialAmount(partialAmount);
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

    @Override
    public ForeClosureResponseDto processForeClosureRequest(Integer foreClosureId) {

        ForeClosureRequest foreClosureRequest =  foreClosureRequestRepo.findById(foreClosureId).orElseThrow(() -> new RuntimeException("ForeClosure Request Not Found"));

        if(!"PENDING".equalsIgnoreCase(foreClosureRequest.getStatus()))
        {
            throw  new RuntimeException("Flocosure Request has been Already processed");
        }


        foreClosureRequest.setApprovedDate(LocalDateTime.now());
        foreClosureRequest.setStatus("APPROVED");
        ForeClosureRequest savedForeClosureRequest =   foreClosureRequestRepo.save(foreClosureRequest);

        return modelMapper.map(savedForeClosureRequest,ForeClosureResponseDto.class);

    }


    @Override
    @Transactional
    public ForeClosureResponseDto createEmiScheduleAfterPartPayment(
            Integer foreClosureId) {

        ForeClosureRequest request = foreClosureRequestRepo.findById(foreClosureId)
                .orElseThrow(() ->
                        new RuntimeException("Foreclosure request not found"));

        if (!"PARTIAL".equalsIgnoreCase(request.getForeclosureType())) {
            throw new RuntimeException(
                    "This request is not a part-payment request");
        }

        if (!"APPROVED".equalsIgnoreCase(request.getStatus())) {
            throw new RuntimeException(
                    "Request must be approved and not already processed");
        }

        if (!Boolean.TRUE.equals(request.getPaid())) {
            throw new RuntimeException(
                    "Part-payment has not been successfully paid");
        }

        LoanAccount loanAccount = request.getLoanAccount();

        BigDecimal outstandingPrincipal = loanAccount.getOutstandingPrincipal();
        BigDecimal partialAmount = request.getPartialAmount();

        if (outstandingPrincipal == null
                || partialAmount == null
                || partialAmount.signum() <= 0
                || partialAmount.compareTo(outstandingPrincipal) >= 0) {
            throw new RuntimeException(
                    "Invalid part-payment amount or outstanding principal");
        }

        BigDecimal newOutstandingPrincipal = outstandingPrincipal
                .subtract(partialAmount)
                .setScale(2, RoundingMode.HALF_UP);

        loanAccount.setOutstandingPrincipal(newOutstandingPrincipal);
        loanAccountRepo.save(loanAccount);

        emiSchedluerService.regenerateFutureEmisAfterPartPayment(
                loanAccount.getLoanAccountId());

        request.setStatus("PART_PAYMENT_APPLIED");
        foreClosureRequestRepo.save(request);

        return modelMapper.map(request, ForeClosureResponseDto.class);
    }

}
