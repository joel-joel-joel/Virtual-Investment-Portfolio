package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionValidationService {

    // Define key fields
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // Constructor
    public TransactionValidationService(TransactionRepository transactionRepository,
                                        AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // Checks transaction exists
    public Transaction validateTransactionExists(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    // Check if account has sufficient balance before operation
    public void validateSufficientBalance(UUID accountId, double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getAccountBalance().doubleValue() < amount) {
            throw new IllegalArgumentException("Insufficient account balance");
        }
    }

    // Checks that the transaction type is either BUY or SELL
    public void validateTransactionType(String type) {
        if (!type.equalsIgnoreCase("BUY") && !type.equalsIgnoreCase("SELL")) {
            throw new IllegalArgumentException("Invalid transaction type. Must be BUY or SELL.");
        }
    }
}
