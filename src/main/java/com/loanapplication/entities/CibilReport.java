package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "CibilScore")
    private Integer cibilScore;

    @Column(name = "CheckDate")
    private LocalDateTime checkDate;
}