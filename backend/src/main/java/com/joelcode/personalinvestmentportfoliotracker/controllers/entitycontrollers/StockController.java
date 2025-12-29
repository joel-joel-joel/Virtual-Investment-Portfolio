package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCompanyProfileDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubMetricsDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubQuoteDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubSearchResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import com.joelcode.personalinvestmentportfoliotracker.services.yahoofinance.YahooFinanceApiClient;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
@Profile("!test")
public class StockController {

    @Autowired
    public StockService stockService;

    @Autowired
    public FinnhubApiClient finnhubApiClient;

    @Autowired
    public YahooFinanceApiClient yahooFinanceApiClient;

    // Get all stocks
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    // Get stock by ID
    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(@PathVariable UUID id) {
        StockDTO stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get or create stock by symbol
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<StockDTO> getOrCreateStockBySymbol(@PathVariable String symbol) {
        StockDTO stock = stockService.getStockBySymbol(symbol);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            // Stock doesn't exist, create it
            try {
                FinnhubCompanyProfileDTO profile = finnhubApiClient.getCompanyProfile(symbol);
                FinnhubQuoteDTO quote = finnhubApiClient.getQuote(symbol);

                StockCreateRequest request = new StockCreateRequest();
                request.setStockCode(symbol.toUpperCase());
                request.setCompanyName(profile.getCompanyName());
                request.setStockValue(quote.getCurrentPrice());
                request.setIndustry(profile.getIndustry());

                StockDTO created = stockService.createStock(request);
                return ResponseEntity.ok(created);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    // Create new stock
    @PostMapping
    public ResponseEntity<StockDTO> createStock(@Valid @RequestBody StockCreateRequest request) {
        StockDTO created = stockService.createStock(request);
        return ResponseEntity.ok(created);
    }

    // Update stock
    @PutMapping("/{id}")
    public ResponseEntity<StockDTO> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        StockDTO updated = stockService.updateStock(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete stock
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    // Get current price for a stock
    @GetMapping("/{id}/price")
    public ResponseEntity<?> getCurrentPrice(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.getCurrentPrice(id));
    }

    // Get real-time quote from FinnHub by symbol
    @GetMapping("/finnhub/quote/{symbol}")
    public ResponseEntity<?> getFinnhubQuote(@PathVariable String symbol) {
        try {
            FinnhubQuoteDTO quote = finnhubApiClient.getQuote(symbol);
            return ResponseEntity.ok(quote);
        } catch (org.springframework.web.client.RestClientException e) {
            // Check for rate limiting or specific API errors
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("rate limit"))) {
                System.err.println("⚠️ [StockController] Rate limit exceeded for symbol: " + symbol);
                return ResponseEntity.status(429).body("Rate limit exceeded for symbol: " + symbol);
            }
            System.err.println("⚠️ [StockController] API error for " + symbol + ": " + errorMsg);
            return ResponseEntity.badRequest().body("Error fetching quote for symbol: " + symbol + " - " + errorMsg);
        } catch (Exception e) {
            System.err.println("❌ [StockController] Unexpected error for " + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body("Unexpected error for symbol: " + symbol);
        }
    }

    // Get company profile from FinnHub by symbol
    @GetMapping("/finnhub/profile/{symbol}")
    public ResponseEntity<?> getFinnhubCompanyProfile(@PathVariable String symbol) {
        try {
            FinnhubCompanyProfileDTO profile = finnhubApiClient.getCompanyProfile(symbol);
            return ResponseEntity.ok(profile);
        } catch (org.springframework.web.client.RestClientException e) {
            // Check for rate limiting or specific API errors
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("429") || errorMsg.contains("rate limit"))) {
                System.err.println("⚠️ [StockController] Rate limit exceeded for profile: " + symbol);
                return ResponseEntity.status(429).body("Rate limit exceeded for symbol: " + symbol);
            }
            System.err.println("⚠️ [StockController] API error fetching profile for " + symbol + ": " + errorMsg);
            return ResponseEntity.badRequest().body("Error fetching profile for symbol: " + symbol + " - " + errorMsg);
        } catch (Exception e) {
            System.err.println("❌ [StockController] Unexpected error fetching profile for " + symbol + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body("Unexpected error for symbol: " + symbol);
        }
    }

    // Get stock metrics from FinnHub by symbol (P/E, EPS, 52W high/low, etc.)
    @GetMapping("/finnhub/metrics/{symbol}")
    public ResponseEntity<?> getFinnhubMetrics(@PathVariable String symbol) {
        try {
            FinnhubMetricsDTO metrics = finnhubApiClient.getMetrics(symbol);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching metrics for symbol: " + symbol);
        }
    }

    // Get historical candle data from Yahoo Finance
    @GetMapping("/finnhub/candles/{symbol}")
    public ResponseEntity<?> getFinnhubCandles(
            @PathVariable String symbol,
            @RequestParam String resolution,
            @RequestParam long from,
            @RequestParam long to) {
        try {
            FinnhubCandleDTO candles = yahooFinanceApiClient.getCandles(symbol, resolution, from, to);
            return ResponseEntity.ok(candles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching candles for symbol: " + symbol);
        }
    }

    // ✅ FIXED: Search for companies by name from FinnHub
    @GetMapping("/finnhub/search")
    public ResponseEntity<?> searchCompanies(@RequestParam String query) {
        System.out.println("\n🔍 FINNHUB SEARCH: Incoming request");
        System.out.println("  Query: " + query);

        try {
            if (query == null || query.trim().isEmpty()) {
                System.out.println("  ❌ Query is empty or null");
                return ResponseEntity.badRequest().body("Search query cannot be empty");
            }

            System.out.println("  ✅ Query validated: " + query.trim());
            System.out.println("  🚀 Calling finnhubApiClient.searchCompanies()...");

            FinnhubSearchResponseDTO results = finnhubApiClient.searchCompanies(query);

            System.out.println("  ✅ Search successful!");

            if (results == null || results.getResult() == null) {
                System.out.println("  ⚠️ Results object or result list is null");
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }

            System.out.println("  📊 Results count: " + results.getResult().size());

            for (int i = 0; i < results.getResult().size(); i++) {
                var item = results.getResult().get(i);
                System.out.println("    [" + i + "] " + item.getSymbol() + " - " + item.getDescription());
            }

            // ✅ Return just the result array
            return ResponseEntity.ok(results.getResult());

        } catch (Exception e) {
            System.out.println("  ❌ Error occurred: " + e.getClass().getSimpleName());
            System.out.println("  📝 Error message: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(
                    String.format("Error searching companies for query: %s - %s", query, e.getMessage())
            );
        }
    }
}