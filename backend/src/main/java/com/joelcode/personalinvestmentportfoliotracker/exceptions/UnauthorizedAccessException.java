package com.joelcode.personalinvestmentportfoliotracker.exceptions;

// Runtime exception for unauthorized access to resources
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
