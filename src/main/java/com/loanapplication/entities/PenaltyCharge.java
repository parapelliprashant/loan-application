package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PenaltyCharges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PenaltyChargeId")
    private Integer penaltyChargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmiScheduleId", nullable = false)
    private EmiSchedule emiSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "PenaltyAmount", precision = 18, scale = 2)
    private BigDecimal penaltyAmount;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
}