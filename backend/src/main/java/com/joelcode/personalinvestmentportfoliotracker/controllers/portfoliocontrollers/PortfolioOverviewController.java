package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Port;
import java.util.UUID;

@RestController
@RequestMapping("/portfolio/overview")
@Profile("!test")
public class PortfolioOverviewController {

    @Autowired
    PortfolioOverviewService portfolioOverviewService;

    // Get portfolio overview for a specific account
    @GetMapping("/portfolio/overview/account/{accountId}")
    public ResponseEntity<PortfolioOverviewDTO> getPortfolioOverview(@PathVariable UUID accountId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForAccount(accountId);
        if (overview != null) {
            return ResponseEntity.ok(overview);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get portfolio performance for a user (aggregates across all accounts)
    @GetMapping("/portfolio/overview/user/{userId}")
    public ResponseEntity<PortfolioOverviewDTO> getPortfolioOverviewForUser(@PathVariable UUID userId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForUser(userId);
        if (overview != null) {
            return ResponseEntity.ok(overview);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
