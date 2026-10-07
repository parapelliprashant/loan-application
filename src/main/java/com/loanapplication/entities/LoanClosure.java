package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "LoanClosures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanClosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ClosureId")
    private Integer closureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "ClosureType")
    private String closureType;

    @Column(name = "FinalSettlementAmount", precision = 18, scale = 2)
    private BigDecimal finalSettlementAmount;

    @Column(name = "ClosureDate")
    private LocalDateTime closureDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClosedBy")
    private User closedBy;

    @Column(name = "Remarks", length = 1000)
    private String remarks;

    @Column(name = "ClosureStatus")
    private String closureStatus;
}