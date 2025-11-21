package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("!test")
public class AccountValidationService {

    // Define key field
    private AccountRepository accountRepository;


    // Constructor
    public AccountValidationService(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }


    // Validation functions

    // Check account exists
    public Account validateAccountExistsByName(String accountName){
        return accountRepository.findByAccountName(accountName)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
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
