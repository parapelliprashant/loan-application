package com.loanapplication.repo;


import com.loanapplication.entities.InterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface InterestAccuralRepo extends JpaRepository<InterestAccrual,Integer> {

     @Query("select coalesce(sum(i.interestAmount),0) from InterestAccrual i" +
             " where i.loanAccount.loanAccountId = :loanAccountId " +

                 "    and i.accrualDate <= :closureDate "+
                     "and i.status = :status")
    BigDecimal calculateAccuredInterest( @Param("loanAccountId") Integer loanAccountId,
    @Param("closureDate") LocalDateTime closureDate,
    @Param("status") String status);


}
