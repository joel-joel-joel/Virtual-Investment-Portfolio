package com.joelcode.personalinvestmentportfoliotracker.exceptions;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}