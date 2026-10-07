package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "LoanAccounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LoanAccountId")
    private Integer loanAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealId", nullable = false)
    private LoanDeal loanDeal;

    @Column(name = "LoanAccountNo", nullable = false, unique = true)
    private String loanAccountNo;

    @Column(name = "LoanAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "OutstandingPrincipal", precision = 18, scale = 2)
    private BigDecimal outstandingPrincipal;

    @Column(name = "LoanStatus")
    private String loanStatus;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "DisbursementDate")
    private LocalDateTime disbursementDate;

    @Column(name = "TotalPaidAmount", precision = 18, scale = 2)
    private BigDecimal totalPaidAmount;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "loanAccount")
    private List<EmiSchedule> emiSchedules;

    @OneToMany(mappedBy = "loanAccount")
    private List<ForeClosureRequest> foreClosureRequests;

    @OneToMany(mappedBy = "loanAccount")
    private List<InterestAccrual> interestAccruals;

    @OneToMany(mappedBy = "loanAccount")
    private List<LoanClosure> loanClosures;

    @OneToMany(mappedBy = "loanAccount")
    private List<LoanPayment> loanPayments;

    @OneToMany(mappedBy = "loanAccount")
    private List<SupportTicket> supportTickets;

    @OneToMany(mappedBy = "loanAccount")
    private List<PenaltyCharge> penaltyCharges;
}