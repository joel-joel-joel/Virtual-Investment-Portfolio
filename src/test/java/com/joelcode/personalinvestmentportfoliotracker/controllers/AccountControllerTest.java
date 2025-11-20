package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.AccountController;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    private AccountController accountController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        accountController = new AccountController();
        accountController.accountService = accountService;
    }

    // Test retrieving all accounts when accounts exist
    @Test
    void testGetAllAccounts_Success() {
        // Setup account list and user
        List<AccountDTO> accounts = new ArrayList<>();
        UserDTO user = new UserDTO(UUID.randomUUID(), "testuser", "<EMAIL>");
        accounts.add(new AccountDTO( "Checking", UUID.randomUUID(), user, BigDecimal.valueOf(5000)));
        accounts.add(new AccountDTO( "Savings", UUID.randomUUID(), user, BigDecimal.valueOf(10000)));

        // Map method return value to setup
        when(accountService.getAllAccounts()).thenReturn(accounts);

        // Run method
        ResponseEntity<List<AccountDTO>> response = accountController.getAllAccounts();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(accountService, times(1)).getAllAccounts();
    }

    // Test retrieving all accounts when no accounts exist
    @Test
    void testGetAllAccounts_Empty() {
        // Setup empty account list
        when(accountService.getAllAccounts()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<AccountDTO>> response = accountController.getAllAccounts();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(accountService, times(1)).getAllAccounts();
    }

    // Test retrieving a single account by ID when account exists
    @Test
    void testGetAccountById_Success() {
        // Setup account and user
        UUID accountId = UUID.randomUUID();
        UserDTO user = new UserDTO(UUID.randomUUID(), "testuser", "<EMAIL>");
        AccountDTO account = new AccountDTO( "Checking", accountId, user, BigDecimal.valueOf(5000));

        // Map method return value to setup
        when(accountService.getAccountById(accountId)).thenReturn(account);

        // Run method
        ResponseEntity<AccountDTO> response = accountController.getAccountById(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountId, response.getBody().getAccountId());
        verify(accountService, times(1)).getAccountById(accountId);
    }

    // Test retrieving a single account by ID when account does not exist
    @Test
    void testGetAccountById_NotFound() {
        // Setup account ID with null return
        UUID accountId = UUID.randomUUID();
        when(accountService.getAccountById(accountId)).thenReturn(null);

        // Run method
        ResponseEntity<AccountDTO> response = accountController.getAccountById(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService, times(1)).getAccountById(accountId);
    }

    // Test creating an account successfully
    @Test
    void testCreateAccount_Success() {
        // Setup create request and expected account
        UUID accountId = UUID.randomUUID();
        UserDTO user = new UserDTO(UUID.randomUUID(), "testuser", "<EMAIL>");
        AccountCreateRequest request = new AccountCreateRequest("Checking",UUID.randomUUID());
        AccountDTO created = new AccountDTO( "Checking", accountId, user, BigDecimal.valueOf(5000));

        // Map method return value to setup
        when(accountService.createAccount(request)).thenReturn(created);

        // Run method
        ResponseEntity<AccountDTO> response = accountController.createAccount(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountId, response.getBody().getAccountId());
        verify(accountService, times(1)).createAccount(request);
    }

    // Test updating an account successfully
    @Test
    void testUpdateAccount_Success() {
        // Setup account update request and expected updated account
        UUID accountId = UUID.randomUUID();
        UserDTO user = new UserDTO(UUID.randomUUID(), "testuser", "<EMAIL>");
        AccountUpdateRequest request = new AccountUpdateRequest("Updated");
        AccountDTO updated = new AccountDTO( "Updated", UUID.randomUUID(), user, BigDecimal.valueOf(5000));

        // Map method return value to setup
        when(accountService.updateAccount(accountId, request)).thenReturn(updated);

        // Run method
        ResponseEntity<AccountDTO> response = accountController.updateAccount(accountId, request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getAccountName());
        verify(accountService, times(1)).updateAccount(accountId, request);
    }

    // Test updating an account when account does not exist
    @Test
    void testUpdateAccount_NotFound() {
        // Setup update request with null return
        UUID accountId = UUID.randomUUID();
        UserDTO user = new UserDTO(UUID.randomUUID(), "testuser", "<EMAIL>");
        AccountUpdateRequest request = new AccountUpdateRequest("Updated");
        when(accountService.updateAccount(accountId, request)).thenReturn(null);

        // Run method
        ResponseEntity<AccountDTO> response = accountController.updateAccount(accountId, request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountService, times(1)).updateAccount(accountId, request);
    }

    // Test deleting an account successfully
    @Test
    void testDeleteAccount_Success() {
        // Setup account ID and mock void service
        UUID accountId = UUID.randomUUID();
        doNothing().when(accountService).deleteAccount(accountId);

        // Run method
        ResponseEntity<Void> response = accountController.deleteAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService, times(1)).deleteAccount(accountId);
    }

    // Test retrieving transactions for an account successfully
    @Test
    void testGetAccountTransactions_Success() {
        // Setup account ID and empty transaction list
        UUID accountId = UUID.randomUUID();
        List<TransactionDTO> transactions = new ArrayList<>();
        when(accountService.getTransactionsForAccount(accountId)).thenReturn(transactions);

        // Run method
        ResponseEntity<?> response = accountController.getAccountTransactions(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).getTransactionsForAccount(accountId);
    }

    // Test retrieving holdings for an account successfully
    @Test
    void testGetAccountHoldings_Success() {
        // Setup account ID and empty holdings list
        UUID accountId = UUID.randomUUID();
        List<HoldingDTO> holdings = new ArrayList<>();
        when(accountService.getHoldingsForAccount(accountId)).thenReturn(holdings);

        // Run method
        ResponseEntity<?> response = accountController.getAccountHoldings(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(accountService, times(1)).getHoldingsForAccount(accountId);
    }
}
