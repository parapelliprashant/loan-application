package com.loanapplication.service.impl;

import com.loanapplication.dto.LoanAccountResponseDto;
import com.loanapplication.entities.Disbursement;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.entities.LoanDeal;
import com.loanapplication.entities.SanctionLetter;
import com.loanapplication.exception.DuplicateResourceException;
import com.loanapplication.exception.InvalidLoanStateException;
import com.loanapplication.exception.ResourceNotFoundException;
import com.loanapplication.repo.DisbursementRepo;
import com.loanapplication.repo.LoanAccountRepo;
import com.loanapplication.repo.LoanDealRepo;
import com.loanapplication.repo.SanctionLetterRepo;
import com.loanapplication.service.LoanAccountService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanAccountServiceImpl implements LoanAccountService {

    private final LoanAccountRepo accountRepo;
    private final LoanDealRepo dealRepo;
    private final DisbursementRepo disbRepo;
    private final SanctionLetterRepo sanctionRepo;
    private final ModelMapper mapper;

    public LoanAccountServiceImpl(
            LoanAccountRepo accountRepo,
            LoanDealRepo dealRepo,
            DisbursementRepo disbRepo,
            SanctionLetterRepo sanctionRepo,
            ModelMapper mapper) {

        this.accountRepo = accountRepo;
        this.dealRepo = dealRepo;
        this.disbRepo = disbRepo;
        this.sanctionRepo = sanctionRepo;
        this.mapper = mapper;
    }

    @Override
    public LoanAccountResponseDto createLoanAccount(Integer dealId) {

        LoanDeal deal =
                dealRepo.findById(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan deal not found with id: " + dealId
                                ));

        if (accountRepo
                .findByLoanDealDealId(dealId)
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Loan account already exists for deal id: " + dealId
            );
        }

        Disbursement disb =
                disbRepo
                        .findTopByLoanDealDealIdOrderByDisbursementDateDesc(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Disbursement not found for deal id: " + dealId
                                ));

        if (!"SUCCESS".equalsIgnoreCase(
                disb.getStatus())) {

            throw new InvalidLoanStateException(
                    "Loan account can be created only after successful disbursement"
            );
        }

        SanctionLetter sanction =
                sanctionRepo
                        .findTopByLoanDealDealIdOrderByCreatedAtDesc(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Sanction letter not found for deal id: " + dealId
                                ));

        LoanAccount account =
                mapper.map(
                        deal,
                        LoanAccount.class
                );

        account.setLoanAccountId(null);
        account.setCustomer(deal.getCustomer());
        account.setLoanDeal(deal);
        account.setLoanAccountNo(
                "TEMP-" + System.nanoTime()
        );
        account.setLoanAmount(
                disb.getDisburseAmount()
        );
        account.setOutstandingPrincipal(
                disb.getDisburseAmount()
        );
        account.setLoanStatus(
                "ACTIVE"
        );
        account.setEmiAmount(
                sanction.getEmiAmount()
        );
        account.setDisbursementDate(
                disb.getDisbursementDate()
        );
        account.setTotalPaidAmount(
                BigDecimal.ZERO
        );
        account.setCreatedAt(
                LocalDateTime.now()
        );

        LoanAccount saved =
                accountRepo.save(account);

        saved.setLoanAccountNo(
                "LA-" + String.format(
                        "%08d",
                        saved.getLoanAccountId()
                )
        );

        return toDto(
                accountRepo.save(saved)
        );
    }

    @Override
    public LoanAccountResponseDto getLoanAccountByDealId(
            Integer dealId) {

        LoanAccount account =
                accountRepo
                        .findByLoanDealDealId(dealId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found for deal id: " + dealId
                                ));

        return toDto(account);
    }

    @Override
    public LoanAccountResponseDto getLoanAccountByAccountNo(
            String loanAccountNo) {

        LoanAccount account =
                accountRepo
                        .findByLoanAccountNo(loanAccountNo)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan account not found with account number: "
                                                + loanAccountNo
                                ));

        return toDto(account);
    }

    @Override
    public List<LoanAccountResponseDto> getCustomerLoanAccounts(
            Integer customerId) {

        return accountRepo
                .findByCustomerCustomerId(customerId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<LoanAccountResponseDto> getAllLoanAccounts() {

        return accountRepo
                .findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private LoanAccountResponseDto toDto(
            LoanAccount account) {

        LoanAccountResponseDto dto =
                mapper.map(
                        account,
                        LoanAccountResponseDto.class
                );

        dto.setDealId(
                account.getLoanDeal()
                        .getDealId()
        );

        dto.setCustomerId(
                account.getCustomer()
                        .getCustomerId()
        );

        dto.setCustomerName(
                getName(account)
        );

        return dto;
    }

    private String getName(
            LoanAccount account) {

        String first =
                account.getCustomer().getFirstName() != null
                        ? account.getCustomer().getFirstName()
                        : "";

        String last =
                account.getCustomer().getLastName() != null
                        ? account.getCustomer().getLastName()
                        : "";

        return (first + " " + last).trim();
    }
}