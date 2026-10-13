// CibilReportRepo.java
package com.loanapplication.repo;

import com.loanapplication.entities.CibilReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CibilReportRepo
        extends JpaRepository<CibilReport, Integer> {

    Optional<CibilReport>
    findTopByCustomerCustomerIdOrderByCheckDateDesc(
            Integer customerId
    );
}