package com.loanapplication.exception;

import com.loanapplication.helper.ApiResponse;
import com.loanapplication.helper.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleResourceNotFound(ResourceNotFoundException e) {

        ErrorResponse errorResponse = new ErrorResponse(
                "NOT FOUND",
                e.getMessage(),
                LocalDateTime.now()
        );

        return new ApiResponse<>(false, "An ERROR OCCURRED", null, errorResponse);
    }
}
