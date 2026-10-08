package com.loanapplication.repo;

import com.loanapplication.entities.EmiSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmiSchedulerRepo extends JpaRepository<EmiSchedule,Integer> {
}
