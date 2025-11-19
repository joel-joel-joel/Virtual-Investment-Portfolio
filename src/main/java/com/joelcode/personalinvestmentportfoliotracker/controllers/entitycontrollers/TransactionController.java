package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.TransactionMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    // Get all transactions
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    // Get a transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable UUID id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new transaction
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody TransactionCreateRequest request) {
        TransactionDTO created = transactionService.createTransaction(request);
        return ResponseEntity.ok(created);
    }

    // Delete a transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    // Get all transactions for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsForAccount(@PathVariable UUID accountId) {
        // Fetch transactions from repository
        List<Transaction> transactions = transactionRepository.findByAccount_AccountId(accountId);

        // Map each Transaction entity to TransactionDTO
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionMapper::toDTO) // assuming you have a mapper
                .collect(Collectors.toList());

        return ResponseEntity.ok(transactionDTOs);
    }

}