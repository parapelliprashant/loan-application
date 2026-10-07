package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ForeClosureRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ForeClosureRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "ForeClosureType")
    private String foreclosureType;

    @Column(name = "ForeClosureAmount", precision = 18, scale = 2)
    private BigDecimal foreclosureAmount;

    @Column(name = "PartialAmount", precision = 18, scale = 2)
    private BigDecimal partialAmount;

    @Column(name = "RequestedDate")
    private LocalDateTime requestedDate;

    @Column(name = "ExpectedClosureDate")
    private LocalDateTime expectedClosureDate;

    @Column(name = "Reason", length = 1000)
    private String reason;

    @Column(name = "Status")
    private String status;

    @Column(name = "ApprovedDate")
    private LocalDateTime approvedDate;

    @Column(name = "IsPaid", nullable = false)
    private Boolean paid = false;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ClosedBy")
    private User closedBy;
}