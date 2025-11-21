package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.TransactionMapper;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    // Define key fields
    private final TransactionRepository transactionRepository;
    private final TransactionValidationService transactionValidationService;


    // Constructor
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  TransactionValidationService transactionValidationService) {
        this.transactionRepository = transactionRepository;
        this.transactionValidationService = transactionValidationService;
    }


    // Interface functions

    // Create a new transaction and show essential information
    @Override
    public TransactionDTO createTransaction(TransactionCreateRequest request) {
        transactionValidationService.validateTransactionType(request.getTransactionType());

        // Validate transaction type
        if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
            double requiredAmount = request.getPricePerShare().doubleValue() * request.getShareQuantity().doubleValue();
            transactionValidationService.validateSufficientBalance(request.getAccountId(), requiredAmount);
        }

        // Map transaction creation request to enttiy
        Transaction transaction = TransactionMapper.toEntity(request);

        // Save transaction to db
        transaction = transactionRepository.save(transaction);

        // Map entity back dto
        return TransactionMapper.toDTO(transaction);
    }

    // Find transaction by ID
    @Override
    public TransactionDTO getTransactionById(UUID transactionId) {
        Transaction transaction = transactionValidationService.validateTransactionExists(transactionId);
        return TransactionMapper.toDTO(transaction);
    }

    // Generate a list of all the transactions inclusive of their information
    @Override
    public List<TransactionDTO> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Delete transaction
    @Override
    public void deleteTransaction(UUID transactionId) {
        Transaction transaction = transactionValidationService.validateTransactionExists(transactionId);
        transactionRepository.delete(transaction);
    }
}