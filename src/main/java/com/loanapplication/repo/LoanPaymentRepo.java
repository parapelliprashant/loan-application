package com.loanapplication.repo;

import com.loanapplication.entities.LoanPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanPaymentRepo
        extends JpaRepository<LoanPayment, Integer> {

    Page<LoanPayment> findByLoanAccountLoanAccountId(
            Integer loanAccountId,
            Pageable pageable
    );

    Optional<LoanPayment> findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<LoanPayment> findByRazorpayPaymentId(
            String razorpayPaymentId
    );
}