package com.loanapplication.repo;
import com.loanapplication.entities.ScoreCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ScoreCardRepo
        extends JpaRepository<ScoreCard, Integer> {
    Optional<ScoreCard>
    findTopByCustomerCustomerIdOrderByAppliedDateDesc(
            Integer customerId
    );
}

