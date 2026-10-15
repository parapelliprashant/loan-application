package com.loanapplication.exception;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.helper.ErrorResponse;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatusCode;


@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Object> handleResourceNotFound(ResourceNotFoundException e)
    {
        ErrorResponse errorResponse = new ErrorResponse("NOT FOUND ",e.getMessage(), LocalDateTime.now());
        return new ApiResponse<>(false,"An ERROR OOCURED",null,errorResponse);
    }
//    siya
@ExceptionHandler(LoanEligibilityException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public ApiResponse<Object> handleLoanEligibility(
        LoanEligibilityException exception) {

    ErrorResponse errorResponse =
            new ErrorResponse(
                    "LOAN_ELIGIBILITY_ERROR",
                    exception.getMessage(),
                    LocalDateTime.now()
            );

    return new ApiResponse<>(
            false,
            "Loan eligibility failed",
            null,
            errorResponse
    );
}

    @ExceptionHandler(LoanValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleLoanValidation(
            LoanValidationException exception) {

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "LOAN_VALIDATION_ERROR",
                        exception.getMessage(),
                        LocalDateTime.now()
                );

        return new ApiResponse<>(
                false,
                "Loan validation failed",
                null,
                errorResponse
        );
    }

    @ExceptionHandler(InvalidLoanStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Object> handleInvalidLoanState(
            InvalidLoanStateException exception) {

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "INVALID_LOAN_STATE",
                        exception.getMessage(),
                        LocalDateTime.now()
                );

        return new ApiResponse<>(
                false,
                "Operation not allowed for current loan state",
                null,
                errorResponse
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Object> handleDuplicateResource(
            DuplicateResourceException exception) {

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "DUPLICATE_RESOURCE",
                        exception.getMessage(),
                        LocalDateTime.now()
                );

        return new ApiResponse<>(
                false,
                "Resource already exists",
                null,
                errorResponse
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationException(
            MethodArgumentNotValidException exception) {

        String errors =
                exception
                        .getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .collect(
                                Collectors.joining(", ")
                        );

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "VALIDATION_ERROR",
                        errors,
                        LocalDateTime.now()
                );

        return new ApiResponse<>(
                false,
                "Request validation failed",
                null,
                errorResponse
        );
    }


    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<ApiResponse<Object>>
    handleResponseStatusException(ResponseStatusException exception) {

        HttpStatusCode status = exception.getStatusCode();

        ErrorResponse errorResponse = new ErrorResponse(
                status.toString(),
                exception.getReason(),
                LocalDateTime.now()
        );

        ApiResponse<Object> response = new ApiResponse<>(
                false,
                exception.getReason(),
                null,
                errorResponse
        );

        return org.springframework.http.ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> handleGeneralException(
            Exception exception) {

        ErrorResponse errorResponse =
                new ErrorResponse(
                        "INTERNAL_SERVER_ERROR",
                        exception.getMessage(),
                        LocalDateTime.now()
                );

        return new ApiResponse<>(
                false,
                "Something went wrong",
                null,
                errorResponse
        );
    }
}
