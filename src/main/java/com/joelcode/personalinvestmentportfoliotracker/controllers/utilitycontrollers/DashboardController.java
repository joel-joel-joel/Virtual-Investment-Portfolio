package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.DashboardDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    PortfolioOverviewService portfolioOverviewService;

    @Autowired
    PortfolioPerformanceService portfolioPerformanceService;

    /**
     * Get dashboard data for a specific account
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<DashboardDTO> getDashboardForAccount(@PathVariable UUID accountId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForAccount(accountId);
        PortfolioPerformanceDTO performance = portfolioPerformanceService.getPerformanceForAccount(accountId);

        DashboardDTO dashboard = new DashboardDTO(
                overview,
                performance,
                new ArrayList<>(), // allocations
                new ArrayList<>()  // recent transactions
        );
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get dashboard data for a user (aggregates all accounts)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<DashboardDTO> getDashboardForUser(@PathVariable UUID userId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForUser(userId);
        PortfolioPerformanceDTO performance = portfolioPerformanceService.getPerformanceForUser(userId);

        DashboardDTO dashboard = new DashboardDTO(
                overview,
                performance,
                new ArrayList<>(), // allocations
                new ArrayList<>()  // recent transactions
        );
        return ResponseEntity.ok(dashboard);
    }
}
