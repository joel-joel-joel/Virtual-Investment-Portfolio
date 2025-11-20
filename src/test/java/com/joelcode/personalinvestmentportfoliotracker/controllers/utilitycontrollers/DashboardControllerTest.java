package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

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
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(
                50000.0,
                55000.0,
                5000.0,
                10.0,
                25
        );
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(
                50000.0,
                55000.0,
                5000.0,
                10.0,
                0.75
        );
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(overview, response.getBody().getOverview());
        assertEquals(performance, response.getBody().getPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    @Test
    void testGetDashboardForAccount_WithNullOverview() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(
                50000.0,
                55000.0,
                5000.0,
                10.0,
                0.75
        );
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(null);
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody().getOverview());
        assertNotNull(response.getBody().getPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    @Test
    void testGetDashboardForAccount_WithNullPerformance() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(
                50000.0,
                55000.0,
                5000.0,
                10.0,
                25
        );
        when(portfolioOverviewService.getPortfolioOverviewForAccount(accountId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForAccount(accountId)).thenReturn(null);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getOverview());
        assertNull(response.getBody().getPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForAccount(accountId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForAccount(accountId);
    }

    @Test
    void testGetDashboardForUser_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(
                100000.0,
                110000.0,
                10000.0,
                10.0,
                50
        );
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(
                100000.0,
                110000.0,
                10000.0,
                10.0,
                1.0
        );
        when(portfolioOverviewService.getPortfolioOverviewForUser(userId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(overview, response.getBody().getOverview());
        assertEquals(performance, response.getBody().getPerformance());
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
        assertNull(response.getBody().getOverview());
        assertNull(response.getBody().getPerformance());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForUser(userId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }

    @Test
    void testGetDashboardForUser_LargePortfolio() {
        // Arrange
        UUID userId = UUID.randomUUID();
        PortfolioOverviewDTO overview = new PortfolioOverviewDTO(
                500000.0,
                550000.0,
                50000.0,
                10.0,
                100
        );
        PortfolioPerformanceDTO performance = new PortfolioPerformanceDTO(
                500000.0,
                550000.0,
                50000.0,
                10.0,
                1.5
        );
        when(portfolioOverviewService.getPortfolioOverviewForUser(userId)).thenReturn(overview);
        when(portfolioPerformanceService.getPerformanceForUser(userId)).thenReturn(performance);

        // Act
        ResponseEntity<DashboardDTO> response = dashboardController.getDashboardForUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(500000.0, response.getBody().getOverview().getTotalInvested());
        assertEquals(100, response.getBody().getOverview().getHoldingsCount());
        verify(portfolioOverviewService, times(1)).getPortfolioOverviewForUser(userId);
        verify(portfolioPerformanceService, times(1)).getPerformanceForUser(userId);
    }
}