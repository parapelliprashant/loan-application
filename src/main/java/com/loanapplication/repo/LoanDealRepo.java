package com.loanapplication.repo;

import com.loanapplication.entities.LoanDeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanDealRepo
        extends JpaRepository<LoanDeal, Integer> {

    List<LoanDeal> findByCustomerCustomerId(
            Integer customerId
    );

    List<LoanDeal> findByDealStatusIgnoreCase(
            String dealStatus
    );
}