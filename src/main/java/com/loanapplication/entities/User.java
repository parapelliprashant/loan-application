
package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleId", nullable = false)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", unique = true)
    private Customer customer;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "Mobile")
    private String mobile;

    @Column(name = "Password")
    private String password;

    @Column(name = "Enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "TotpConfigured", nullable = false)
    private Boolean totpConfigured = false;

    @Column(name = "SecretKey")
    private String secretKey;

    @Column(name = "PendingSecret")
    private String pendingSecret;

    @Column(name = "EmailOtpHash")
    private String emailOtpHash;

    @Column(name = "EmailOtpExpiry")
    private java.time.LocalDateTime emailOtpExpiry;

    @Column(name = "EmailOtpAttempts")
    private Integer emailOtpAttempts = 0;

    @Column(name = "EmailOtpSentAt")
    private java.time.LocalDateTime emailOtpSentAt;

    @OneToMany(mappedBy = "officer")
    private List<DealReview> dealReviews;

    @OneToMany(mappedBy = "closedBy")
    private List<ForeClosureRequest> foreClosureRequests;

    @OneToMany(mappedBy = "closedBy")
    private List<LoanClosure> loanClosures;
}