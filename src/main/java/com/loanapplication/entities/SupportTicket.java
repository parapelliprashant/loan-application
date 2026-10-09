package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SupportTickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TicketId")
    private Integer ticketId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanAccountId")
    private LoanAccount loanAccount;

    @Column(name = "Subject")
    private String subject;

    @Column(name = "Description", length = 2000)
    private String description;

    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
