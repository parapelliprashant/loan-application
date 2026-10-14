package com.loanapplication.repo;
import com.loanapplication.entities.CibilReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CibilReportRepo extends JpaRepository<CibilReport, Integer> {
    Optional<CibilReport> findTopByCustomerCustomerIdOrderByCheckDateDesc(
            Integer customerId
    );
}