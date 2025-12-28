package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCompanyProfileDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubQuoteDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.watchlist.WatchlistItemDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.entities.Watchlist;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.WatchlistRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/watchlist")
@CrossOrigin(origins = "*")
@Profile("!test")
public class WatchlistController {

    private final WatchlistRepository watchlistRepository;
    private final StockRepository stockRepository;
    private final FinnhubApiClient finnhubApiClient;

    public WatchlistController(WatchlistRepository watchlistRepository, StockRepository stockRepository,
                               FinnhubApiClient finnhubApiClient) {
        this.watchlistRepository = watchlistRepository;
        this.stockRepository = stockRepository;
        this.finnhubApiClient = finnhubApiClient;
    }

    // GET /api/watchlist - Get user's watchlist
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<List<WatchlistItemDTO>> getWatchlist(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        List<Watchlist> watchlist = watchlistRepository.findByUser_UserId(user.getUserId());
        List<WatchlistItemDTO> items = watchlist.stream()
                .map(w -> {
                    Stock stock = w.getStock();

                    // Get full quote from Finnhub (includes change and changePercent)
                    BigDecimal currentPrice = stock.getStockValue();
                    BigDecimal change = BigDecimal.ZERO;
                    BigDecimal changePercent = BigDecimal.ZERO;

                    try {
                        var quote = finnhubApiClient.getQuote(stock.getStockCode());
                        if (quote != null) {
                            // Use Finnhub's current price if available
                            if (quote.getCurrentPrice() != null) {
                                currentPrice = quote.getCurrentPrice();
                            }
                            // Use Finnhub's change data directly (already calculated)
                            if (quote.getChange() != null) {
                                change = quote.getChange();
                            }
                            if (quote.getChangePercent() != null) {
                                changePercent = quote.getChangePercent();
                            }
                        }
                    } catch (Exception e) {
                        // Fallback: use stored price if API fails
                        currentPrice = stock.getStockValue();
                    }

                    // ✅ Get sector from Finnhub profile
                    String sector = "Other";
                    try {
                        FinnhubCompanyProfileDTO profile = finnhubApiClient.getCompanyProfile(stock.getStockCode());
                        if (profile != null && profile.getIndustry() != null) {
                            sector = profile.getIndustry();
                        }
                    } catch (Exception e) {
                        // Fallback to "Other" if profile fetch fails
                    }

                    return new WatchlistItemDTO(
                            w.getWatchlistId(),
                            user.getUserId(),
                            stock.getStockId(),
                            stock.getStockCode(),
                            stock.getCompanyName(),
                            currentPrice,
                            change,
                            changePercent,
                            w.getAddedAt(),
                            sector  // ✅ Include sector
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    // POST /api/watchlist - Add to watchlist
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<?> addToWatchlist(@RequestBody Map<String, String> request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String stockIdStr = request.get("stockId");
        if (stockIdStr == null) {
            return ResponseEntity.badRequest().body("stockId is required");
        }

        UUID stockId = UUID.fromString(stockIdStr);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (watchlistRepository.existsByUser_UserIdAndStock_StockId(user.getUserId(), stockId)) {
            return ResponseEntity.badRequest().body("Stock already in watchlist");
        }

        Watchlist watchlist = new Watchlist(user, stock);
        watchlistRepository.save(watchlist);

        // Get full quote from Finnhub (includes change and changePercent)
        BigDecimal currentPrice = stock.getStockValue();
        BigDecimal change = BigDecimal.ZERO;
        BigDecimal changePercent = BigDecimal.ZERO;

        try {
            var quote = finnhubApiClient.getQuote(stock.getStockCode());
            if (quote != null) {
                // Use Finnhub's current price if available
                if (quote.getCurrentPrice() != null) {
                    currentPrice = quote.getCurrentPrice();
                }
                // Use Finnhub's change data directly (already calculated)
                if (quote.getChange() != null) {
                    change = quote.getChange();
                }
                if (quote.getChangePercent() != null) {
                    changePercent = quote.getChangePercent();
                }
            }
        } catch (Exception e) {
            // Fallback: use stored price if API fails
            currentPrice = stock.getStockValue();
        }

        WatchlistItemDTO dto = new WatchlistItemDTO(
                watchlist.getWatchlistId(),
                user.getUserId(),
                stock.getStockId(),
                stock.getStockCode(),
                stock.getCompanyName(),
                currentPrice,
                change,
                changePercent,
                watchlist.getAddedAt()
        );

        return ResponseEntity.ok(dto);
    }

    // DELETE /api/watchlist/{stockId} - Remove from watchlist
    @DeleteMapping("/{stockId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional  // ✅ ADD THIS - Critical for delete operations
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable UUID stockId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!watchlistRepository.existsByUser_UserIdAndStock_StockId(user.getUserId(), stockId)) {
            return ResponseEntity.notFound().build();
        }

        watchlistRepository.deleteByUser_UserIdAndStock_StockId(user.getUserId(), stockId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/watchlist/check/{stockId} - Check if in watchlist
    @GetMapping("/check/{stockId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> isInWatchlist(@PathVariable UUID stockId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        boolean inWatchlist = watchlistRepository.existsByUser_UserIdAndStock_StockId(user.getUserId(), stockId);
        return ResponseEntity.ok(Map.of("inWatchlist", inWatchlist));
    }
}