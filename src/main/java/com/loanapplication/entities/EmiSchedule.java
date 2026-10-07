package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "EmiSchedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmiScheduleId")
    private Integer emiScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "InstallmentNo", nullable = false)
    private Integer installmentNo;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "PrincipalAmount", precision = 18, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "InterestAmount", precision = 18, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "OpeningBalance", precision = 18, scale = 2)
    private BigDecimal openingBalance;

    @Column(name = "ClosingBalance", precision = 18, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "Emi", precision = 18, scale = 2)
    private BigDecimal emi;

    @Column(name = "PaymentStatus")
    private String paymentStatus;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;

    @Column(name = "CancellationReason", length = 1000)
    private String cancellationReason;

    @OneToMany(mappedBy = "emiSchedule")
    private List<PenaltyCharge> penaltyCharges;
}