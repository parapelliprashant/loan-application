
package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "RazorpayPaymentOrders")
@Getter
@Setter
@NoArgsConstructor
public class RazorpayPaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentOrderId")
    private Integer paymentOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "RazorpayOrderId", nullable = false, unique = true)
    private String razorpayOrderId;

    @Column(name = "Amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "Currency", nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "OrderStatus", nullable = false)
    private String orderStatus = "CREATED";

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "ProcessedAt")
    private LocalDateTime processedAt;

    @Column(name = "EmiSnapshot", columnDefinition = "TEXT")
    private String emiSnapshot;

    @Column(name = "PaymentPurpose", nullable = false, length = 30)
    private String paymentPurpose = "EMI";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ForeClosureRequestId")
    private ForeClosureRequest foreClosureRequest;
}
