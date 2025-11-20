package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.TransactionController;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.TransactionMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service and repository
        transactionController = new TransactionController();
        transactionController.transactionService = transactionService;
        transactionController.transactionRepository = transactionRepository;
    }

    // Test retrieving all transactions when transactions exist
    @Test
    void testGetAllTransactions_Success() {
        // Setup transaction list with sample transactions
        List<TransactionDTO> transactions = new ArrayList<>();
        transactions.add(new TransactionDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(50), BigDecimal.valueOf(20), Transaction.TransactionType.BUY));
        transactions.add(new TransactionDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(10), BigDecimal.valueOf(30), Transaction.TransactionType.SELL));

        // Map method return value to setup
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        // Run method
        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(transactionService, times(1)).getAllTransactions();
    }

    // Test retrieving all transactions when no transactions exist
    @Test
    void testGetAllTransactions_Empty() {
        // Setup empty transaction list
        when(transactionService.getAllTransactions()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(transactionService, times(1)).getAllTransactions();
    }

    // Test retrieving a single transaction by ID when transaction exists
    @Test
    void testGetTransactionById_Success() {
        // Setup transaction DTO with sample data
        UUID transactionId = UUID.randomUUID();
        TransactionDTO transaction = new TransactionDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(50), BigDecimal.valueOf(20), Transaction.TransactionType.BUY);

        // Map method return value to setup
        when(transactionService.getTransactionById(transactionId)).thenReturn(transaction);

        // Run method
        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById(transactionId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Transaction.TransactionType.BUY, response.getBody().getTransactionType());
        verify(transactionService, times(1)).getTransactionById(transactionId);
    }

    // Test retrieving a single transaction by ID when transaction does not exist
    @Test
    void testGetTransactionById_NotFound() {
        // Setup transaction ID with null return
        UUID transactionId = UUID.randomUUID();
        when(transactionService.getTransactionById(transactionId)).thenReturn(null);

        // Run method
        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById(transactionId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(transactionService, times(1)).getTransactionById(transactionId);
    }

    // Test creating a transaction successfully
    @Test
    void testCreateTransaction_Success() {
        // Setup create request and expected transaction DTO
        UUID transactionId = UUID.randomUUID();
        TransactionCreateRequest request = new TransactionCreateRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(50), BigDecimal.valueOf(20), Transaction.TransactionType.BUY);
        TransactionDTO created = new TransactionDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(50), BigDecimal.valueOf(20), Transaction.TransactionType.BUY);

        // Map method return value to setup
        when(transactionService.createTransaction(request)).thenReturn(created);

        // Run method
        ResponseEntity<TransactionDTO> response = transactionController.createTransaction(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(50), response.getBody().getShareQuantity());
        verify(transactionService, times(1)).createTransaction(request);
    }

    // Test deleting a transaction successfully
    @Test
    void testDeleteTransaction_Success() {
        // Setup transaction ID and mock void service
        UUID transactionId = UUID.randomUUID();
        doNothing().when(transactionService).deleteTransaction(transactionId);

        // Run method
        ResponseEntity<Void> response = transactionController.deleteTransaction(transactionId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(transactionService, times(1)).deleteTransaction(transactionId);
    }

    // Test retrieving transactions for a specific account when no transactions exist
    @Test
    void testGetTransactionsForAccount_Success() {
        // Setup account ID and empty transaction list
        UUID accountId = UUID.randomUUID();
        List<Transaction> transactions = new ArrayList<>();
        when(transactionRepository.findByAccount_AccountId(accountId)).thenReturn(transactions);

        // Run method
        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(transactionRepository, times(1)).findByAccount_AccountId(accountId);
    }

    // Test retrieving transactions for a specific account when transactions exist
    @Test
    void testGetTransactionsForAccount_WithTransactions() {
        // Setup account ID and transaction list
        UUID accountId = UUID.randomUUID();
        List<Transaction> transactions = new ArrayList<>();
        // Mock transaction entities (actual implementation depends on Transaction entity structure)
        when(transactionRepository.findByAccount_AccountId(accountId)).thenReturn(transactions);

        // Run method
        ResponseEntity<List<TransactionDTO>> response = transactionController.getTransactionsForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(transactionRepository, times(1)).findByAccount_AccountId(accountId);
    }
}
