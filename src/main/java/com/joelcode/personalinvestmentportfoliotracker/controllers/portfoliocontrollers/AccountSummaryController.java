package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AccountSummaryDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.summary.AccountSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accountsummary")
public class AccountSummaryController {

    @Autowired
    private AccountSummaryService accountSummaryService;

    // Get summary for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<AccountSummaryDTO> getAccountSummary(@PathVariable UUID accountId) {
        AccountSummaryDTO summary = accountSummaryService.getAccountSummary(accountId);
        if (summary != null) {
            return ResponseEntity.ok(summary);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Optional: Get summaries for all accounts of a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountSummaryDTO>> getAccountSummariesForUser(@PathVariable UUID userId) {
        List<AccountSummaryDTO> summaries = accountSummaryService.getAccountSummariesForUser(userId);
        return ResponseEntity.ok(summaries);
    }
}
