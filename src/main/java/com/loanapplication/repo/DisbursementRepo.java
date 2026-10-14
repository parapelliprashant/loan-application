package com.loanapplication.repo;

import com.loanapplication.entities.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisbursementRepo
        extends JpaRepository<Disbursement, Integer> {

    Optional<Disbursement>
    findTopByLoanDealDealIdOrderByDisbursementDateDesc(
            Integer dealId
    );
}