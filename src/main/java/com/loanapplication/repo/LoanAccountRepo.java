package com.loanapplication.repo;

import com.loanapplication.entities.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanAccountRepo
        extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount>
    findByLoanDealDealId(
            Integer dealId
    );

    List<LoanAccount>
    findByCustomerCustomerId(
            Integer customerId
    );

    Optional<LoanAccount>
    findByLoanAccountNo(
            String loanAccountNo
    );
}