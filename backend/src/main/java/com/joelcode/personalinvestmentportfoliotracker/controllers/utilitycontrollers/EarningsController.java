package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.earnings.EarningsDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Earnings;
import com.joelcode.personalinvestmentportfoliotracker.repositories.EarningsRepository;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import com.joelcode.personalinvestmentportfoliotracker.services.earnings.EarningsService;
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
    private EarningsService earningsService;

    @Autowired
    private EarningsRepository earningsRepository;

    // GET /api/earnings/upcoming - Get upcoming earnings for holdings in the next 90 days
    @GetMapping("/upcoming")
    public ResponseEntity<List<EarningsDTO>> getUpcomingEarnings() {
        try {
            // Get current user
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            UUID userId = userDetails.getUser().getUserId();

            // Use service to fetch earnings from Finnhub API
            List<EarningsDTO> earnings = earningsService.getUpcomingEarningsForUser(userId);

            return ResponseEntity.ok(earnings);
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

            // Use service to fetch earnings from Finnhub API
            List<EarningsDTO> earnings = earningsService.getUpcomingEarningsForAccount(accountId, userId);

            return ResponseEntity.ok(earnings);
        } catch (Exception e) {
            System.err.println("❌ [EarningsController] Error fetching earnings for account: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
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
