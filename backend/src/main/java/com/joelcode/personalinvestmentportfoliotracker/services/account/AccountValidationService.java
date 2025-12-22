package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountValidationService {

    // Define key field
    private AccountRepository accountRepository;


    // Constructor
    public AccountValidationService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }


    // Validation functions

    // Check account name does not exist for this specific user
    public void validateAccountDoesNotExistByName(UUID userId, String accountName){
        if(accountRepository.existsByUser_UserIdAndAccountNameIgnoreCase(userId, accountName)) {
            throw new IllegalArgumentException("You already have an account with this name");
        }
    }

    // Check account exists
    public Account validateAccountExistsById(UUID accountId){
        return accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }


    // Check if account has sufficient balance before operation
    public void validateSufficientBalance(Account account, double amount){
        if (account.getAccountBalance().doubleValue() < amount) {
            throw new IllegalArgumentException("Insufficient balance");
        }
    }

}
