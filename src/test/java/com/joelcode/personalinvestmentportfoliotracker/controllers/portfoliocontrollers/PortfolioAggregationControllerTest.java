package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioAggregationDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation.PortfolioAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioAggregationControllerTest {

    @Mock
    private PortfolioAggregationService portfolioAggregationService;

    private PortfolioAggregationController aggregationController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        aggregationController = new PortfolioAggregationController();
        aggregationController.portfolioAggregationService = portfolioAggregationService;
    }

    // Test retrieving aggregated portfolio for an account when record exists
    @Test
    void testGetAggregateForAccount_Success() {
        // Setup sample aggregation data
        UUID accountId = UUID.randomUUID();
        PortfolioAggregationDTO aggregation = new PortfolioAggregationDTO(accountId, UUID.randomUUID(),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(50000), 5000, new ArrayList<>());
        when(portfolioAggregationService.aggregateForAccount(accountId)).thenReturn(aggregation);

        // Run method
        ResponseEntity<PortfolioAggregationDTO> response = aggregationController.getAggregateForAccount(accountId);

        // Assert returned variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalValue());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalDividends());
        assertEquals(5000, response.getBody().getNumberOfHoldings());
        verify(portfolioAggregationService, times(1)).aggregateForAccount(accountId);
    }

    // Test retrieving aggregated portfolio for an account when record does not exist
    @Test
    void testGetAggregateForAccount_NotFound() {
        // Setup account with null return
        UUID accountId = UUID.randomUUID();
        when(portfolioAggregationService.aggregateForAccount(accountId)).thenReturn(null);

        // Run method
        ResponseEntity<PortfolioAggregationDTO> response = aggregationController.getAggregateForAccount(accountId);

        // Assert returned status is NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(portfolioAggregationService, times(1)).aggregateForAccount(accountId);
    }

    // Test retrieving aggregated portfolio for an account with negative values
    @Test
    void testGetAggregateForAccount_NegativeReturn() {
        // Setup aggregation data with negative values
        UUID accountId = UUID.randomUUID();
        PortfolioAggregationDTO aggregation = new PortfolioAggregationDTO(accountId, UUID.randomUUID(),
                BigDecimal.valueOf(50000), BigDecimal.valueOf(50000), 5000, new ArrayList<>());
        when(portfolioAggregationService.aggregateForAccount(accountId)).thenReturn(aggregation);

        // Run method
        ResponseEntity<PortfolioAggregationDTO> response = aggregationController.getAggregateForAccount(accountId);

        // Assert returned variables are correct (still positive in this mock)
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalValue());
        assertEquals(5000, response.getBody().getNumberOfHoldings());
        verify(portfolioAggregationService, times(1)).aggregateForAccount(accountId);
    }

    // Test retrieving aggregated portfolio for a user when record exists
    @Test
    void testGetAggregateForUser_Success() {
        // Setup sample aggregation data for user
        UUID userId = UUID.randomUUID();
        PortfolioAggregationDTO aggregation = new PortfolioAggregationDTO(UUID.randomUUID(), userId,
                BigDecimal.valueOf(50000), BigDecimal.valueOf(50000), 5000, new ArrayList<>());
        when(portfolioAggregationService.aggregateForUser(userId)).thenReturn(aggregation);

        // Run method
        ResponseEntity<PortfolioAggregationDTO> response = aggregationController.getAggregateForUser(userId);

        // Assert returned variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalDividends());
        assertEquals(5000, response.getBody().getNumberOfHoldings());
        verify(portfolioAggregationService, times(1)).aggregateForUser(userId);
    }

    // Test retrieving aggregated portfolio for a user when record does not exist
    @Test
    void testGetAggregateForUser_NotFound() {
        // Setup user with null return
        UUID userId = UUID.randomUUID();
        when(portfolioAggregationService.aggregateForUser(userId)).thenReturn(null);

        // Run method
        ResponseEntity<PortfolioAggregationDTO> response = aggregationController.getAggregateForUser(userId);

        // Assert returned status is NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(portfolioAggregationService, times(1)).aggregateForUser(userId);
    }

    // Test retrieving aggregated portfolio for a user with zero totals
    @Test
    void testGetAggregateForUser_ZeroReturn() {
        // Setup aggregation data with zero totals
        UUID userId = UUID.randomUUID();
        PortfolioAggregationDTO aggregation = new PortfolioAggregationDTO(UUID.randomUUID(), userId,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, new ArrayList<>());
        when(portfolioAggregationService.aggregateForUser(userId)).thenReturn(aggregation);

        // Run method
        ResponseEntity<PortfolioAggregationDTO> response = aggregationController.getAggregateForUser(userId);

        // Assert returned variables are zero as expected
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalDividends());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalValue());
        verify(portfolioAggregationService, times(1)).aggregateForUser(userId);
    }
}
