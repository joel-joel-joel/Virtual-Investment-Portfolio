package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.DashboardDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceService;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.TransactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@Profile("!test")
public class DashboardController {

    @Autowired
    PortfolioOverviewService portfolioOverviewService;

    @Autowired
    PortfolioPerformanceService portfolioPerformanceService;

    @Autowired
    AllocationBreakdownService allocationBreakdownService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AccountRepository accountRepository;

    // Get dashboard data for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<DashboardDTO> getDashboardForAccount(@PathVariable UUID accountId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForAccount(accountId);
        PortfolioPerformanceDTO performance = portfolioPerformanceService.getPerformanceForAccount(accountId);

        // Get allocations for account
        var allocations = allocationBreakdownService.getAllocationForAccount(accountId);

        // Get recent transactions (last 10) for account
        List<Transaction> recentTransactions = transactionRepository.findByAccount_AccountId(accountId);
        List<TransactionDTO> transactionDTOs = recentTransactions.stream()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(10)
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        DashboardDTO dashboard = new DashboardDTO(
                overview,
                performance,
                allocations,
                transactionDTOs
        );
        return ResponseEntity.ok(dashboard);
    }

    // Get dashboard data for a user (aggregates all accounts)
    @GetMapping("/user/{userId}")
    public ResponseEntity<DashboardDTO> getDashboardForUser(@PathVariable UUID userId) {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForUser(userId);
        PortfolioPerformanceDTO performance = portfolioPerformanceService.getPerformanceForUser(userId);

        // Get allocations for user (across all accounts)
        var allocations = allocationBreakdownService.getAllocationForUser(userId);

        // Get recent transactions (last 10) for all user's accounts
        List<Account> userAccounts = accountRepository.findByUser_UserId(userId, Pageable.unpaged());
        List<TransactionDTO> transactionDTOs = userAccounts.stream()
                .flatMap(account -> transactionRepository.findByAccount_AccountId(account.getAccountId()).stream())
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .limit(10)
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        DashboardDTO dashboard = new DashboardDTO(
                overview,
                performance,
                allocations,
                transactionDTOs
        );
        return ResponseEntity.ok(dashboard);
    }
}


