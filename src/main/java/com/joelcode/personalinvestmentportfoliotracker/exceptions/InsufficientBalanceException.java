package com.joelcode.personalinvestmentportfoliotracker.exceptions;

class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
