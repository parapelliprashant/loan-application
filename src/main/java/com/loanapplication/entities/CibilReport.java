
package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "CibilReports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CibilReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CibilReportId")
    private Integer cibilReportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "PanNo")
    private String panNo;

    @Column(name = "IncomeScore")
    private Integer incomeScore;

    @Column(name = "EmploymentScore")
    private Integer employmentScore;

    @Column(name = "AgeScore")
    private Integer ageScore;

    @Column(name = "FoirScore")
    private Integer foirScore;

    @Column(name = "FoirPercentage", precision = 7, scale = 2)
    private BigDecimal foirPercentage;

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "CheckDate")
    private LocalDateTime checkDate;
}
