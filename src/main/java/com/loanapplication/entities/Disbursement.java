package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Disbursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DisbursementId")
    private Integer disbursementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DealId", nullable = false)
    private LoanDeal loanDeal;

    @Column(name = "DisburseAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal disburseAmount;

    @Column(name = "BankPartner")
    private String bankPartner;

    @Column(name = "DisbursementDate")
    private LocalDateTime disbursementDate;

    @Column(name = "Status")
    private String status;
}