
package com.loanapplication.repo;

import com.loanapplication.entities.RazorpayPaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RazorpayPaymentOrderRepo
        extends JpaRepository<RazorpayPaymentOrder, Integer> {

    Optional<RazorpayPaymentOrder> findByRazorpayOrderId(
            String razorpayOrderId
    );
}
