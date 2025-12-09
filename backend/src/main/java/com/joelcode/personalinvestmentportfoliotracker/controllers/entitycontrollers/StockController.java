package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCompanyProfileDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubQuoteDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching quote for symbol: " + symbol);
        }
    }

    // Get company profile from FinnHub by symbol
    @GetMapping("/finnhub/profile/{symbol}")
    public ResponseEntity<?> getFinnhubCompanyProfile(@PathVariable String symbol) {
        try {
            FinnhubCompanyProfileDTO profile = finnhubApiClient.getCompanyProfile(symbol);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching profile for symbol: " + symbol);
        }
    }
}