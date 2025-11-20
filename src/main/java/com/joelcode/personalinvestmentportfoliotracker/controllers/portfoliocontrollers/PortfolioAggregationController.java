package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioAggregationDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation.PortfolioAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/portfolio/aggregate")
public class PortfolioAggregationController {

    @Autowired
    PortfolioAggregationService portfolioAggregationService;

    // Aggregate for a single account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<PortfolioAggregationDTO> getAggregateForAccount(@PathVariable UUID accountId) {
        PortfolioAggregationDTO dto = portfolioAggregationService.aggregateForAccount(accountId);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Aggregate across all accounts for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<PortfolioAggregationDTO> getAggregateForUser(@PathVariable UUID userId) {
        PortfolioAggregationDTO dto = portfolioAggregationService.aggregateForUser(userId);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
