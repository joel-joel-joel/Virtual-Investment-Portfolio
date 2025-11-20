package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.utility.DashboardDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceService;
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
class DashboardControllerTest {

    @Mock
    private PortfolioOverviewService portfolioOverviewService;

    @Mock
    private PortfolioPerformanceService portfolioPerformanceService;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController();
        dashboardController.portfolioOverviewService = portfolioOverviewService;
        dashboardController.portfolioPerformanceService = portfolioPerformanceService;
    }

    @Test
    void testGetDashboardForAccount_Success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), new ArrayList<>());
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(overview, response.getBody().getPortfolioOverview());
        assertEquals(performance, response.getBody().getPortfolioPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    @Test
    void testGetDashboardForAccount_WithNullOverview() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(null);
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getPortfolioOverview());
        assertNotNull(response.getBody().getPortfolioPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    @Test
    void testGetDashboardForAccount_WithNullPerformance() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(UUID.randomUUID(), accountId, BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), new ArrayList<>());
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(null);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getPortfolioPerformance());
        assertNotNull(response.getBody().getPortfolioOverview());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    @Test
    void testGetDashboardForUser_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(userId, UUID.randomUUID(), BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), new ArrayList<>());
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(userId, UUID.randomUUID(), BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioOverviewService.getPortfolioOverviewForUser(userId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getPortfolioOverview());
        assertNotNull(response.getBody().getPortfolioPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForUser(userId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }

    @Test
    void testGetDashboardForUser_WithBothNull() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(portfolioOverviewService.getPortfolioOverviewForUser(userId)).thenReturn(null);
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(null);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getPortfolioOverview());
        assertNull(response.getBody().getPortfolioPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForUser(userId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }

    @Test
    void testGetDashboardForUser_LargePortfolio() {
        // Arrange
        UUID userId = UUID.randomUUID();

        ArrayList<HoldingDTO> holdings = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            HoldingDTO holding = new HoldingDTO();
            holding.setStockSymbol("STOCK" + i);
            holding.setQuantity(BigDecimal.valueOf(100 + i));
            holding.setCurrentPrice(BigDecimal.valueOf(10 + i));
            holdings.add(holding);
        }

        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(userId, UUID.randomUUID(), BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), holdings);
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(userId, UUID.randomUUID(), BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000), BigDecimal.valueOf(5000), BigDecimal.valueOf(4000), BigDecimal.valueOf(10),
                BigDecimal.valueOf(10), BigDecimal.valueOf(14), BigDecimal.valueOf(88), BigDecimal.valueOf(9));
        when(portfolioOverviewService.getPortfolioOverviewForUser(userId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(55000), response.getBody().getPortfolioOverview().getTotalCostBasis());
        assertEquals(50, response.getBody().getPortfolioOverview().getHoldings().size());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForUser(userId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }
}