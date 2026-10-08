package com.loanapplication.repo;

import com.loanapplication.entities.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanAccountRepo extends JpaRepository<LoanAccount,Integer> {
}
