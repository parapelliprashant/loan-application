package com.loanapplication.exception;

public class LoanValidationException
        extends RuntimeException {

    public LoanValidationException(String message) {
        super(message);
    }
}