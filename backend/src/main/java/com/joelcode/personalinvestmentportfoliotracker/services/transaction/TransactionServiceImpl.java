package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.TransactionMapper;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
@Transactional(readOnly = true)
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
    @Transactional(readOnly = false)
    public TransactionDTO createTransaction(TransactionCreateRequest request) {
        transactionValidationService.validateTransactionType(request.getTransactionType());


        Transaction transaction = TransactionMapper.toEntity(request);
        transaction = transactionRepository.save(transaction);

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
        // SECURITY FIX: Get currently logged-in user
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        User user = userDetails.getUser();

        // Filter transactions by user ID only
        return transactionRepository.findByAccount_User_UserId(user.getUserId())
                .stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Delete transaction
    @Override
    @Transactional(readOnly = false)
    public void deleteTransaction(UUID transactionId) {
        Transaction transaction = transactionValidationService.validateTransactionExists(transactionId);
        transactionRepository.delete(transaction);
    }
}