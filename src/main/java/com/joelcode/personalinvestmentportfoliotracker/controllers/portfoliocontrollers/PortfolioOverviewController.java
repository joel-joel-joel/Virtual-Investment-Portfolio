package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/portfolio/overview")
public class PortfolioOverviewController {

    @Autowired
    PortfolioOverviewService portfolioOverviewService;

    // Get portfolio overview for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<PortfolioOverviewDTO> getPortfolioOverview(@PathVariable UUID accountId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForAccount(accountId);
        if (overview != null) {
            return ResponseEntity.ok(overview);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
