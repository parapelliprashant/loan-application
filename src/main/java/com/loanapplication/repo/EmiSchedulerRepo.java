package com.loanapplication.repo;

import com.loanapplication.entities.EmiSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmiSchedulerRepo extends JpaRepository<EmiSchedule,Integer> {


    @Query(value = "SELECT e FROM EmiSchedule e WHERE e.loanAccount.loanAccountId = :loanId ORDER BY e.installmentNo ASC",
            countQuery = "SELECT count(e) FROM EmiSchedule e WHERE e.loanAccount.loanAccountId = :loanId")

    Page<EmiSchedule> findEmiSchedulesByLoanId(@Param("loanId") int loanId, Pageable pageable);
}
