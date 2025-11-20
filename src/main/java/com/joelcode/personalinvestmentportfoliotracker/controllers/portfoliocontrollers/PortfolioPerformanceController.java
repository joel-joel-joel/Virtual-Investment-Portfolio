package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/portfolio/performance")
public class PortfolioPerformanceController {

    @Autowired
    PortfolioPerformanceService portfolioPerformanceService;

    // Get portfolio performance for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<PortfolioPerformanceDTO> getPerformanceForAccount(@PathVariable UUID accountId) {
        PortfolioPerformanceDTO performance = portfolioPerformanceService.getPerformanceForAccount(accountId);
        if (performance != null) {
            return ResponseEntity.ok(performance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get portfolio performance for a user (aggregates across all accounts)
    @GetMapping("/user/{userId}")
    public ResponseEntity<PortfolioPerformanceDTO> getPerformanceForUser(@PathVariable UUID userId) {
        PortfolioPerformanceDTO performance = portfolioPerformanceService.getPerformanceForUser(userId);
        if (performance != null) {
            return ResponseEntity.ok(performance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
