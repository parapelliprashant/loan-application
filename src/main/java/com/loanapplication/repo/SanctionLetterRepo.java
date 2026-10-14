package com.loanapplication.repo;
import com.loanapplication.entities.SanctionLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface SanctionLetterRepo
        extends JpaRepository<SanctionLetter, Integer> {
    Optional<SanctionLetter>
    findTopByLoanDealDealIdOrderByCreatedAtDesc(
            Integer dealId
    );
}