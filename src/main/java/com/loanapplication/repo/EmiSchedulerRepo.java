
package com.loanapplication.repo;

import com.loanapplication.entities.EmiSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmiSchedulerRepo
        extends JpaRepository<EmiSchedule, Integer> {

    @Query(
            value = """
            SELECT e FROM EmiSchedule e
            WHERE e.loanAccount.loanAccountId = :loanId
            ORDER BY e.installmentNo ASC
            """,
            countQuery = """
            SELECT COUNT(e) FROM EmiSchedule e
            WHERE e.loanAccount.loanAccountId = :loanId
            """
    )
    Page<EmiSchedule> findEmiSchedulesByLoanId(
            @Param("loanId") int loanId,
            Pageable pageable
    );

    List<EmiSchedule> findByLoanAccountLoanAccountId(
            Integer loanAccountId
    );

    @Query("""
        SELECT e FROM EmiSchedule e
        JOIN FETCH e.loanAccount la
        WHERE e.dueDate <= :emiDate
          AND e.paymentStatus = 'UNPAID'
        ORDER BY e.dueDate ASC
        """)
    List<EmiSchedule>
    sendNotificationsRegardingpaymentBeforeDueDateAndUnpaidEmis(
            @Param("emiDate") LocalDate emiDate
    );

    @Modifying
    @Query("""
        UPDATE EmiSchedule e
        SET e.paymentStatus = 'CANCELLED',
            e.cancellationReason = :reason
        WHERE e.loanAccount.loanAccountId = :loanId
          AND e.paymentStatus IN ('PENDING', 'UNPAID')
          AND e.dueDate > :today
        """)
    int cancelFutureUnpaidEmis(
            @Param("loanId") Integer loanId,
            @Param("today") LocalDate today,
            @Param("reason") String reason
    );
}
