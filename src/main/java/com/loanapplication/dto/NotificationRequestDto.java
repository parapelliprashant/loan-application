package com.loanapplication.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequestDto {

    private Integer customerId; // Reference the ID instead of the heavy JPA Entity

    private String message;

    private Boolean read = false;

    private LocalDateTime createdAt;
}