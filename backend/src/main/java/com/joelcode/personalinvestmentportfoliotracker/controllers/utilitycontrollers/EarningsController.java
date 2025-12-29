package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.earnings.EarningsDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Earnings;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.EarningsRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/earnings")
@CrossOrigin(origins = "*")
@Profile("!test")
public class EarningsController {

    @Autowired
    private EarningsRepository earningsRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private AccountRepository accountRepository;

    // GET /api/earnings/upcoming - Get upcoming earnings for holdings in the next 90 days
    @GetMapping("/upcoming")
    public ResponseEntity<List<EarningsDTO>> getUpcomingEarnings() {
        try {
            // Get current user
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = userDetails.getUser().getUserId();
            System.out.println("🎯 [EarningsController] getUpcomingEarnings called for userId: " + userId);

            // Get user's accounts
            List<com.joelcode.personalinvestmentportfoliotracker.entities.Account> allAccounts =
                accountRepository.findByUser_UserId(userId, null);
            System.out.println("📊 [EarningsController] User accounts count: " + allAccounts.size());

            if (allAccounts.isEmpty()) {
                System.out.println("⚠️ [EarningsController] No accounts found for user");
                return ResponseEntity.ok(List.of());
            }

            // Get all holdings across all user accounts
            List<Holding> allHoldings = new java.util.ArrayList<>();
            allAccounts.forEach(account -> {
                List<Holding> holdings = holdingRepository.findByAccount_AccountId(account.getAccountId());
                allHoldings.addAll(holdings);
            });
            System.out.println("📋 [EarningsController] Total holdings: " + allHoldings.size());

            if (allHoldings.isEmpty()) {
                System.out.println("⚠️ [EarningsController] No holdings found for user");
                return ResponseEntity.ok(List.of());
            }

            // Extract stock IDs from holdings
            List<UUID> stockIds = allHoldings.stream()
                    .map(h -> h.getStock().getStockId())
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("🔍 [EarningsController] Distinct stock IDs: " + stockIds.size());
            stockIds.forEach(id -> System.out.println("   - " + id));

            // Fetch earnings for these stocks
            LocalDate today = LocalDate.now();
            LocalDate in90Days = today.plusDays(90);
            System.out.println("📅 [EarningsController] Date range: " + today + " to " + in90Days);

            List<Earnings> earnings = earningsRepository.findUpcomingEarningsForStocks(stockIds, today, in90Days);
            System.out.println("✅ [EarningsController] Earnings found: " + earnings.size());

            List<EarningsDTO> earningsDTOs = earnings.stream()
                    .map(e -> new EarningsDTO(
                            e.getEarningId(),
                            e.getStock().getStockId(),
                            e.getStock().getStockCode(),
                            e.getStock().getCompanyName(),
                            e.getEarningsDate(),
                            e.getEstimatedEPS(),
                            e.getActualEPS(),
                            e.getReportTime()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(earningsDTOs);
        } catch (Exception e) {
            System.err.println("❌ [EarningsController] Error fetching earnings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    // GET /api/earnings/upcoming/account/{accountId} - Get upcoming earnings for a specific account's holdings
    @GetMapping("/upcoming/account/{accountId}")
    public ResponseEntity<List<EarningsDTO>> getUpcomingEarningsForAccount(@PathVariable UUID accountId) {
        try {
            // Get current user
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = userDetails.getUser().getUserId();
            System.out.println("🎯 [EarningsController] getUpcomingEarningsForAccount called for accountId: " + accountId + ", userId: " + userId);

            // Verify account exists and belongs to user
            var account = accountRepository.findById(accountId);
            if (account.isEmpty()) {
                System.out.println("❌ [EarningsController] Account not found: " + accountId);
                return ResponseEntity.status(404).body(List.of());
            }

            var foundAccount = account.get();
            if (!foundAccount.getUser().getUserId().equals(userId)) {
                System.out.println("⚠️ [EarningsController] Account does not belong to user. AccountId: " + accountId + ", UserId: " + userId);
                return ResponseEntity.status(403).body(List.of());
            }

            System.out.println("✅ [EarningsController] Account verified for user");

            // Get holdings for specific account only
            List<Holding> holdings = holdingRepository.findByAccount_AccountId(accountId);
            System.out.println("📋 [EarningsController] Holdings count for account: " + holdings.size());

            if (holdings.isEmpty()) {
                System.out.println("ℹ️ [EarningsController] No holdings found for account");
                return ResponseEntity.ok(List.of());
            }

            // Extract stock IDs from holdings
            List<UUID> stockIds = holdings.stream()
                    .map(h -> h.getStock().getStockId())
                    .distinct()
                    .collect(Collectors.toList());
            System.out.println("🔍 [EarningsController] Distinct stock IDs for account: " + stockIds.size());
            stockIds.forEach(id -> System.out.println("   - " + id));

            // Fetch earnings for these stocks
            LocalDate today = LocalDate.now();
            LocalDate in90Days = today.plusDays(90);
            System.out.println("📅 [EarningsController] Date range: " + today + " to " + in90Days);

            List<Earnings> earnings = earningsRepository.findUpcomingEarningsForStocks(stockIds, today, in90Days);
            System.out.println("✅ [EarningsController] Earnings found for account: " + earnings.size());

            List<EarningsDTO> earningsDTOs = earnings.stream()
                    .map(e -> new EarningsDTO(
                            e.getEarningId(),
                            e.getStock().getStockId(),
                            e.getStock().getStockCode(),
                            e.getStock().getCompanyName(),
                            e.getEarningsDate(),
                            e.getEstimatedEPS(),
                            e.getActualEPS(),
                            e.getReportTime()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(earningsDTOs);
        } catch (Exception e) {
            System.err.println("❌ [EarningsController] Error fetching earnings for account: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // GET /api/earnings/by-stock/{stockId} - Get earnings for a specific stock
    @GetMapping("/by-stock/{stockId}")
    public ResponseEntity<List<EarningsDTO>> getEarningsByStock(@PathVariable UUID stockId) {
        System.out.println("🎯 [EarningsController] getEarningsByStock called for stockId: " + stockId);
        try {
            List<Earnings> earnings = earningsRepository.findByStock_StockId(stockId);
            System.out.println("✅ [EarningsController] Earnings found for stock: " + earnings.size());

            List<EarningsDTO> earningsDTOs = earnings.stream()
                    .map(e -> new EarningsDTO(
                            e.getEarningId(),
                            e.getStock().getStockId(),
                            e.getStock().getStockCode(),
                            e.getStock().getCompanyName(),
                            e.getEarningsDate(),
                            e.getEstimatedEPS(),
                            e.getActualEPS(),
                            e.getReportTime()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(earningsDTOs);
        } catch (Exception e) {
            System.err.println("❌ [EarningsController] Error fetching earnings for stock: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }
}
