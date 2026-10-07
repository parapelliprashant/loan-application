package com.loanapplication.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private String code;

    private String details;

    private LocalDateTime timestamp;
}
