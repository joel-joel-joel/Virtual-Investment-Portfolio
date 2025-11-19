package com.joelcode.personalinvestmentportfoliotracker.exceptions;

class DuplicateEntityException extends RuntimeException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}