package com.loanapplication.repo;

import com.loanapplication.entities.PenaltyCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyChargeRepo extends JpaRepository<PenaltyCharge, Integer> {

    List<PenaltyCharge> findByLoanAccountLoanAccountId(Integer loanAccountId);

    List<PenaltyCharge> findByEmiScheduleEmiScheduleId(Integer emiScheduleId);
}