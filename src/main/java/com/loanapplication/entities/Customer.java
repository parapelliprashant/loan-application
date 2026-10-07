package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "Password")
    private String password;

    @Column(name = "MobileNo")
    private String mobileNo;

    @Column(name = "AadhaarNo")
    private String aadhaarNo;

    @Column(name = "EmploymentType")
    private String employmentType;

    @Column(name = "MonthlyIncome", precision = 18, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(name = "IsEmailVerified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer")
    private List<Notification> notifications;

    @OneToMany(mappedBy = "customer")
    private List<CibilReport> cibilReports;

    @OneToMany(mappedBy = "customer")
    private List<EligibilityResult> eligibilityResults;

    @OneToMany(mappedBy = "customer")
    private List<KycDocument> kycDocuments;

    @OneToMany(mappedBy = "customer")
    private List<LoanDeal> loanDeals;

    @OneToMany(mappedBy = "customer")
    private List<ScoreCard> scoreCards;

    @OneToMany(mappedBy = "customer")
    private List<LoanAccount> loanAccounts;

    @OneToMany(mappedBy = "customer")
    private List<SupportTicket> supportTickets;
}