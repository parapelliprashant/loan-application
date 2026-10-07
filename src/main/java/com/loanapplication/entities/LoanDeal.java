package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "LoanDeals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DealId")
    private Integer dealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "LoanType")
    private String loanType;

    @Column(name = "LoanAmount", precision = 18, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "InterestRate", precision = 8, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "TenureMonths")
    private Integer tenureMonths;

    @Column(name = "EmiAmount", precision = 18, scale = 2)
    private BigDecimal emiAmount;

    @Column(name = "BankName")
    private String bankName;

    @Column(name = "BankAccountNumber")
    private String bankAccountNumber;

    @Column(name = "IFSCCode")
    private String ifscCode;

    @Column(name = "EmiDay")
    private Integer emiDay;

    @Column(name = "ApprovedAmount", precision = 18, scale = 2)
    private BigDecimal approvedAmount;

    @OneToMany(mappedBy = "loanDeal")
    private List<Disbursement> disbursements;

    @OneToMany(mappedBy = "loanDeal")
    private List<SanctionLetter> sanctionLetters;

    @OneToMany(mappedBy = "loanDeal")
    private List<DealReview> dealReviews;

    @OneToMany(mappedBy = "loanDeal")
    private List<LoanAccount> loanAccounts;
}