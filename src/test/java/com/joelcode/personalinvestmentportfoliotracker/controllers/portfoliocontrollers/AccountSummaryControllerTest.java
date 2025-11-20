package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AccountSummaryDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.summary.AccountSummaryService;
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
class AccountSummaryControllerTest {

    @Mock
    private AccountSummaryService accountSummaryService;

    private AccountSummaryController accountSummaryController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        accountSummaryController = new AccountSummaryController();
        accountSummaryController.accountSummaryService = accountSummaryService;
    }

    // Test retrieving account summary for an account when record exists
    @Test
    void testGetAccountSummary_Success() {
        // Setup sample account summary data
        UUID accountId = UUID.randomUUID();
        AccountSummaryDTO summary = new AccountSummaryDTO(UUID.randomUUID(), accountId, "Checking", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(15000), BigDecimal.valueOf(800), BigDecimal.valueOf(400), BigDecimal.valueOf(300), new ArrayList<>());
        when(accountSummaryService.getAccountSummary(accountId)).thenReturn(summary);

        // Run method
        ResponseEntity<AccountSummaryDTO> response = accountSummaryController.getAccountSummary(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountId, response.getBody().getAccountId());
        assertEquals(BigDecimal.valueOf(15000), response.getBody().getTotalMarketValue());
        verify(accountSummaryService, times(1)).getAccountSummary(accountId);
    }

    // Test retrieving account summary for an account when record does not exist
    @Test
    void testGetAccountSummary_NotFound() {
        // Setup account with null return
        UUID accountId = UUID.randomUUID();
        when(accountSummaryService.getAccountSummary(accountId)).thenReturn(null);

        // Run method
        ResponseEntity<AccountSummaryDTO> response = accountSummaryController.getAccountSummary(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(accountSummaryService, times(1)).getAccountSummary(accountId);
    }

    // Test retrieving all account summaries for a user when records exist
    @Test
    void testGetAccountSummariesForUser_Success() {
        // Setup sample account summaries list
        UUID userId = UUID.randomUUID();
        List<AccountSummaryDTO> summaries = new ArrayList<>();
        summaries.add(new AccountSummaryDTO(userId, UUID.randomUUID(), "Checking", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(15000), BigDecimal.valueOf(800), BigDecimal.valueOf(400), BigDecimal.valueOf(300), new ArrayList<>()));
        summaries.add(new AccountSummaryDTO(userId, UUID.randomUUID(), "Saving", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(15000), BigDecimal.valueOf(800), BigDecimal.valueOf(400), BigDecimal.valueOf(300), new ArrayList<>()));
        when(accountSummaryService.getAccountSummariesForUser(userId)).thenReturn(summaries);

        // Run method
        ResponseEntity<List<AccountSummaryDTO>> response = accountSummaryController.getAccountSummariesForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(accountSummaryService, times(1)).getAccountSummariesForUser(userId);
    }

    // Test retrieving all account summaries for a user when no records exist
    @Test
    void testGetAccountSummariesForUser_Empty() {
        // Setup user with empty list
        UUID userId = UUID.randomUUID();
        when(accountSummaryService.getAccountSummariesForUser(userId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<AccountSummaryDTO>> response = accountSummaryController.getAccountSummariesForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(accountSummaryService, times(1)).getAccountSummariesForUser(userId);
    }

    // Test retrieving all account summaries for a user when multiple accounts exist
    @Test
    void testGetAccountSummariesForUser_MultipleAccounts() {
        // Setup sample account summaries list with multiple accounts
        UUID userId = UUID.randomUUID();
        List<AccountSummaryDTO> summaries = new ArrayList<>();
        summaries.add(new AccountSummaryDTO(userId, UUID.randomUUID(), "Checking", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(15000), BigDecimal.valueOf(800), BigDecimal.valueOf(400), BigDecimal.valueOf(300), new ArrayList<>()));
        summaries.add(new AccountSummaryDTO(userId, UUID.randomUUID(), "Checking", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(15000), BigDecimal.valueOf(800), BigDecimal.valueOf(400), BigDecimal.valueOf(300), new ArrayList<>()));
        summaries.add(new AccountSummaryDTO(userId, UUID.randomUUID(), "Checking", BigDecimal.valueOf(1000),
                BigDecimal.valueOf(15000), BigDecimal.valueOf(800), BigDecimal.valueOf(400), BigDecimal.valueOf(300), new ArrayList<>()));
        when(accountSummaryService.getAccountSummariesForUser(userId)).thenReturn(summaries);

        // Run method
        ResponseEntity<List<AccountSummaryDTO>> response = accountSummaryController.getAccountSummariesForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(accountSummaryService, times(1)).getAccountSummariesForUser(userId);
    }
}
