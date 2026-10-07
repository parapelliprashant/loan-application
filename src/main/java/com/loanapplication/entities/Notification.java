package com.loanapplication.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationId")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private Customer customer;

    @Column(name = "Message", nullable = false, length = 1000)
    private String message;

    @Column(name = "IsRead", nullable = false)
    private Boolean read = false;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;
}