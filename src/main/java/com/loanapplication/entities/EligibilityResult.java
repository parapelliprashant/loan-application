package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "EligibilityResults")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EligibilityId")
    private Integer eligibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "IsEligible", nullable = false)
    private Boolean eligible = false;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "RejectionReason", length = 1000)
    private String rejectionReason;
}