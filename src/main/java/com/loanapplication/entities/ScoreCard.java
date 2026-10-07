package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ScoreCards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScoreCardId")
    private Integer scoreCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "CurrentStatus")
    private String currentStatus;

    @Column(name = "RejectionReason", length = 1000)
    private String rejectionReason;

    @Column(name = "AppliedDate")
    private LocalDateTime appliedDate;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "RiskCategory")
    private String riskCategory;

    @Column(name = "EligibleLoanAmount", precision = 18, scale = 2)
    private BigDecimal eligibleLoanAmount;
}