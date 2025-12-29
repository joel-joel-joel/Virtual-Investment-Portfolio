package com.joelcode.personalinvestmentportfoliotracker.services.earnings;

import com.joelcode.personalinvestmentportfoliotracker.dto.earnings.EarningsDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubEarningsCalendarDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubEarningsEventDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for fetching earnings data from Finnhub API
 * Handles fetching earnings calendars for each stock in user's holdings
 */
@Service
public class EarningsServiceImpl implements EarningsService {

    @Autowired
    private FinnhubApiClient finnhubApiClient;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StockRepository stockRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<EarningsDTO> getUpcomingEarningsForAccount(UUID accountId, UUID userId) {
        System.out.println("🎯 [EarningsService] getUpcomingEarningsForAccount called for accountId: " + accountId + ", userId: " + userId);

        // 1. Validate account belongs to user
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getUserId().equals(userId)) {
            System.err.println("⚠️  [EarningsService] Unauthorized access to account");
            throw new RuntimeException("Unauthorized access to account");
        }

        // 2. Get holdings for account
        List<Holding> holdings = holdingRepository.findByAccount_AccountId(accountId);
        System.out.println("📊 [EarningsService] Holdings found: " + holdings.size());

        if (holdings.isEmpty()) {
            System.out.println("ℹ️  [EarningsService] No holdings found for account");
            return Collections.emptyList();
        }

        // 3. Extract unique stock symbols
        Set<String> symbols = holdings.stream()
                .map(h -> h.getStock().getStockCode())
                .collect(Collectors.toSet());

        System.out.println("🔍 [EarningsService] Stock symbols: " + String.join(", ", symbols));

        // 4. Define date range (next 90 days)
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(90);
        String fromDate = today.format(DATE_FORMATTER);
        String toDate = endDate.format(DATE_FORMATTER);

        System.out.println("📅 [EarningsService] Date range: " + fromDate + " to " + toDate);

        // 5. Fetch earnings from Finnhub for each symbol
        List<EarningsDTO> allEarnings = new ArrayList<>();

        for (String symbol : symbols) {
            try {
                System.out.println("📡 [EarningsService] Fetching earnings for: " + symbol);

                FinnhubEarningsCalendarDTO calendar = finnhubApiClient.getEarningsCalendar(
                        fromDate,
                        toDate,
                        symbol
                );

                if (calendar != null && calendar.getEarningsCalendar() != null) {
                    System.out.println("✅ [EarningsService] Got " + calendar.getEarningsCalendar().size() + " earnings for " + symbol);

                    for (FinnhubEarningsEventDTO event : calendar.getEarningsCalendar()) {
                        // Find stock entity to get stockId
                        Optional<Stock> stockOpt = stockRepository.findByStockCode(symbol);
                        if (stockOpt.isPresent()) {
                            Stock stock = stockOpt.get();

                            // Map to EarningsDTO
                            EarningsDTO dto = new EarningsDTO(
                                    UUID.randomUUID().toString(),  // earningId (not persisted)
                                    stock.getStockId(),
                                    stock.getStockCode(),
                                    stock.getCompanyName(),
                                    event.getDate(),
                                    event.getEpsEstimate(),
                                    event.getEpsActual(),
                                    mapReportTime(event.getHour())
                            );

                            allEarnings.add(dto);
                            System.out.println("   ✓ Added earnings for " + symbol + " on " + event.getDate());
                        } else {
                            System.err.println("⚠️  [EarningsService] Stock not found: " + symbol);
                        }
                    }
                } else {
                    System.out.println("ℹ️  [EarningsService] No earnings data for " + symbol);
                }
            } catch (Exception e) {
                System.err.println("❌ [EarningsService] Failed to fetch earnings for " + symbol + ": " + e.getMessage());
                // Continue with other symbols
            }
        }

        // 6. Sort by date and return
        allEarnings.sort(Comparator.comparing(EarningsDTO::getEarningsDate));
        System.out.println("✅ [EarningsService] Returning " + allEarnings.size() + " total earnings");
        return allEarnings;
    }

    @Override
    public List<EarningsDTO> getUpcomingEarningsForUser(UUID userId) {
        System.out.println("🎯 [EarningsService] getUpcomingEarningsForUser called for userId: " + userId);

        // Get all user accounts
        List<com.joelcode.personalinvestmentportfoliotracker.entities.Account> accounts =
                accountRepository.findByUser_UserId(userId, null);

        System.out.println("📊 [EarningsService] User accounts found: " + accounts.size());

        // Combine earnings from all accounts
        List<EarningsDTO> allEarnings = new ArrayList<>();
        for (var account : accounts) {
            allEarnings.addAll(getUpcomingEarningsForAccount(account.getAccountId(), userId));
        }

        // Remove duplicates and sort
        Map<String, EarningsDTO> uniqueEarnings = allEarnings.stream()
                .collect(Collectors.toMap(
                        e -> e.getStockCode() + "_" + e.getEarningsDate(),
                        e -> e,
                        (e1, e2) -> e1
                ));

        List<EarningsDTO> result = new ArrayList<>(uniqueEarnings.values());
        result.sort(Comparator.comparing(EarningsDTO::getEarningsDate));

        System.out.println("✅ [EarningsService] Returning " + result.size() + " unique earnings across all accounts");
        return result;
    }

    /**
     * Map Finnhub report time codes to user-friendly strings
     * @param hour Report time code from Finnhub ("bmo", "amc", "dmh", etc.)
     * @return User-friendly report time string
     */
    private String mapReportTime(String hour) {
        if (hour == null) return "Time TBA";

        switch (hour.toLowerCase()) {
            case "bmo":
                return "Before Market Open";
            case "amc":
                return "After Market Close";
            case "dmh":
                return "During Market Hours";
            default:
                return hour;
        }
    }
}
