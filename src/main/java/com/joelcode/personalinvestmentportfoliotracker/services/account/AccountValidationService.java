package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;

import java.util.UUID;

public class AccountValidationService {

    // Define key field
    private AccountRepository accountRepository;

    // Constructor
    public AccountValidationService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    // Check account exists
    public Account validateAccountExists(UUID accountId){
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    // Check if account has sufficient balance before operation
    public void validateSufficientBalance(Account account, double amount){
        if (account.getAccountBalance().doubleValue() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
    }

}
