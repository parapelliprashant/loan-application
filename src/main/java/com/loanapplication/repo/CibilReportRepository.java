package com.loanapplication.repo;

import com.loanapplication.entities.CibilReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CibilReportRepository extends JpaRepository<CibilReport, Integer> {

    List<CibilReport> findByCustomerCustomerIdOrderByCheckDateDesc(Integer customerId);
}
