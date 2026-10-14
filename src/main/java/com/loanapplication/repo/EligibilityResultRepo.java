// EligibilityResultRepo.java
package com.loanapplication.repo;

import com.loanapplication.entities.EligibilityResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EligibilityResultRepo
        extends JpaRepository<EligibilityResult, Integer> {

    Optional<EligibilityResult>
    findTopByCustomerCustomerIdOrderByEligibilityIdDesc(
            Integer customerId
    );
}