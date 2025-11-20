package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
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
class PortfolioOverviewControllerTest {

    @Mock
    private PortfolioOverviewService portfolioOverviewService;

    private PortfolioOverviewController portfolioOverviewController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        portfolioOverviewController = new PortfolioOverviewController();
        portfolioOverviewController.portfolioOverviewService = portfolioOverviewService;
    }

    // Test retrieving portfolio overview for an account when record exists
    @Test
    void testGetPortfolioOverview_Success() {
        // Setup sample portfolio overview data
        UUID accountId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), new ArrayList<>());
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);

        // Run method
        ResponseEntity<PortfolioOverviewDTO> response = portfolioOverviewController.getPortfolioOverview(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(55000), response.getBody().getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(50000), response.getBody().getTotalPortfolioValue());
        assertEquals(BigDecimal.valueOf(4000), response.getBody().getTotalRealizedGain());
        assertEquals(BigDecimal.valueOf(5000), response.getBody().getTotalUnrealizedGain());
        assertEquals(BigDecimal.valueOf(10), response.getBody().getTotalDividends());
        assertEquals(BigDecimal.valueOf(10), response.getBody().getCashBalance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
    }

    // Test retrieving portfolio overview for an account when record does not exist
    @Test
    void testGetPortfolioOverview_NotFound() {
        // Setup account with null return
        UUID accountId = UUID.randomUUID();
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(null);

        // Run method
        ResponseEntity<PortfolioOverviewDTO> response = portfolioOverviewController.getPortfolioOverview(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
    }

    // Test retrieving portfolio overview for an account with an empty portfolio
    @Test
    void testGetPortfolioOverview_EmptyPortfolio() {
        // Setup portfolio overview with zero totals
        UUID accountId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(UUID.randomUUID(), accountId, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null);
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);

        // Run method
        ResponseEntity<PortfolioOverviewDTO> response = portfolioOverviewController.getPortfolioOverview(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalPortfolioValue());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalRealizedGain());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalUnrealizedGain());
        assertEquals(BigDecimal.ZERO, response.getBody().getTotalDividends());
        assertEquals(BigDecimal.ZERO, response.getBody().getCashBalance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
    }

    // Test retrieving portfolio overview for an account with a large number of holdings
    @Test
    void testGetPortfolioOverview_LargePortfolio() {
        // Setup portfolio with 50 holdings
        UUID accountId = UUID.randomUUID();
        ArrayList<HoldingDTO> holdings = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            HoldingDTO holding = new HoldingDTO();
            holding.setStockSymbol("STOCK" + i);
            holding.setQuantity(BigDecimal.valueOf(100 + i));
            holding.setCurrentPrice(BigDecimal.valueOf(10 + i));
            holdings.add(holding);
        }

        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(1000000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), holdings);
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);

        // Run method
        ResponseEntity<PortfolioOverviewDTO> response = portfolioOverviewController.getPortfolioOverview(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(1000000), response.getBody().getTotalCostBasis());
        assertEquals(50, response.getBody().getHoldings().size());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
    }

    // Test retrieving portfolio overview for an account with negative returns
    @Test
    void testGetPortfolioOverview_NegativeReturn() {
        // Setup portfolio overview with negative gains
        UUID accountId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(-5000), BigDecimal.valueOf(-10), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), new ArrayList<>());
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);

        // Run method
        ResponseEntity<PortfolioOverviewDTO> response = portfolioOverviewController.getPortfolioOverview(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(-5000), response.getBody().getTotalUnrealizedGain());
        assertEquals(BigDecimal.valueOf(-10), response.getBody().getTotalRealizedGain());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
    }
}
