package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "InterestAccruals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterestAccrual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AccrualId")
    private Integer accrualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "InterestAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "AccrualDate")
    private LocalDateTime accrualDate;

    @Column(name = "InstallmentNo")
    private Integer installmentNo;

    @Column(name = "Status")
    private String status;
}