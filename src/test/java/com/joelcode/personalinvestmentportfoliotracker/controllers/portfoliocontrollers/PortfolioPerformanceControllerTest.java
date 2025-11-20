package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioPerformanceControllerTest {

    @Mock
    private PortfolioPerformanceService portfolioPerformanceService;

    private PortfolioPerformanceController performanceController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        performanceController = new PortfolioPerformanceController();
        performanceController.portfolioPerformanceService = portfolioPerformanceService;
    }

    // Test retrieving portfolio performance for an account when record exists
    @Test
    void testGetPerformanceForAccount_Success() {
        // Setup sample performance data
        UUID accountId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalPortfolioValue());
        assertEquals(BigDecimal.valueOf(55000), response.getBody().getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(5000), response.getBody().getTotalRealizedGain());
        assertEquals(BigDecimal.valueOf(4000), response.getBody().getTotalUnrealizedGain());
        assertEquals(BigDecimal.valueOf(10), response.getBody().getTotalDividends());
        assertEquals(BigDecimal.valueOf(10), response.getBody().getCashBalance());
        assertEquals(BigDecimal.valueOf(14), response.getBody().getRoiPercentage());
        assertEquals(BigDecimal.valueOf(88), response.getBody().getDailyGain());
        assertEquals(BigDecimal.valueOf(9), response.getBody().getMonthlyGain());
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    // Test retrieving portfolio performance for an account when record does not exist
    @Test
    void testGetPerformanceForAccount_NotFound() {
        // Setup account with null return
        UUID accountId = UUID.randomUUID();
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(null);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    // Test retrieving portfolio performance for an account with negative return values
    @Test
    void testGetPerformanceForAccount_NegativeReturn() {
        // Setup performance data with negative realized/unrealized gains
        UUID accountId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(-5000), BigDecimal.valueOf(-1000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(-14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(-5000), response.getBody().getTotalRealizedGain());
        assertEquals(BigDecimal.valueOf(-1000), response.getBody().getTotalUnrealizedGain());
        assertEquals(BigDecimal.valueOf(-14), response.getBody().getRoiPercentage());
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    // Test retrieving portfolio performance for an account with zero return values
    @Test
    void testGetPerformanceForAccount_ZeroReturn() {
        // Setup performance data with zero realized/unrealized gains
        UUID accountId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(0), BigDecimal.valueOf(0), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(0), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(0), response.getBody().getTotalRealizedGain());
        assertEquals(BigDecimal.valueOf(0), response.getBody().getTotalUnrealizedGain());
        assertEquals(BigDecimal.valueOf(0), response.getBody().getRoiPercentage());
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    // Test retrieving portfolio performance for a user when record exists
    @Test
    void testGetPerformanceForUser_Success() {
        // Setup sample performance data for user
        UUID userId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(userId, UUID.randomUUID(), BigDecimal.valueOf(50000),
                BigDecimal.valueOf(1500000), BigDecimal.valueOf(1000000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(performance);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(1500000), response.getBody().getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalPortfolioValue());
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }

    // Test retrieving portfolio performance for a user when record does not exist
    @Test
    void testGetPerformanceForUser_NotFound() {
        // Setup user with null return
        UUID userId = UUID.randomUUID();
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(null);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }

    // Test retrieving portfolio performance for a user with a large portfolio
    @Test
    void testGetPerformanceForUser_LargePortfolio() {
        // Setup performance data for user with aggregated account values
        UUID userId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(userId, BigDecimal.valueOf(1000000),
                BigDecimal.valueOf(1500000), BigDecimal.valueOf(1000000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(performance);

        // Run method
        ResponseEntity<PortfolioPerformanceDTO> response = performanceController.getPerformanceForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(1500000), response.getBody().getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(1000000), response.getBody().getTotalPortfolioValue());
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }
}
