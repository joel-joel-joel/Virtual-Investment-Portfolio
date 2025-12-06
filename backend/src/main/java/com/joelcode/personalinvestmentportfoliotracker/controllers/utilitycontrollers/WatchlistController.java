package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.watchlist.WatchlistItemDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.entities.Watchlist;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.WatchlistRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<WatchlistItemDTO>> getWatchlist(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Watchlist> watchlist = watchlistRepository.findByUserId(user.getUserId());
        List<WatchlistItemDTO> items = watchlist.stream()
                .map(w -> {
                    Stock stock = w.getStock();
                    BigDecimal currentPrice = finnhubApiClient.getCurrentPrice(stock.getStockCode());
                    if (currentPrice == null) {
                        currentPrice = stock.getStockValue();
                    }
                    BigDecimal previousPrice = stock.getStockValue();
                    BigDecimal change = currentPrice.subtract(previousPrice);
                    BigDecimal changePercent = previousPrice.compareTo(BigDecimal.ZERO) > 0
                        ? change.divide(previousPrice, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                        : BigDecimal.ZERO;

                    return new WatchlistItemDTO(
                            w.getWatchlistId(),
                            user.getUserId(),
                            stock.getStockId(),
                            stock.getStockCode(),
                            stock.getCompanyName(),
                            currentPrice,
                            change,
                            changePercent,
                            w.getAddedAt()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(items);
    }

    // POST /api/watchlist - Add to watchlist
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addToWatchlist(@RequestBody Map<String, String> request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        String stockIdStr = request.get("stockId");
        if (stockIdStr == null) {
            return ResponseEntity.badRequest().body("stockId is required");
        }

        UUID stockId = UUID.fromString(stockIdStr);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (watchlistRepository.existsByUserIdAndStockId(user.getUserId(), stockId)) {
            return ResponseEntity.badRequest().body("Stock already in watchlist");
        }

        Watchlist watchlist = new Watchlist(user, stock);
        watchlistRepository.save(watchlist);

        BigDecimal currentPrice = finnhubApiClient.getCurrentPrice(stock.getStockCode());
        if (currentPrice == null) {
            currentPrice = stock.getStockValue();
        }
        BigDecimal previousPrice = stock.getStockValue();
        BigDecimal change = currentPrice.subtract(previousPrice);
        BigDecimal changePercent = previousPrice.compareTo(BigDecimal.ZERO) > 0
            ? change.divide(previousPrice, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            : BigDecimal.ZERO;

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
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable UUID stockId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        if (!watchlistRepository.existsByUserIdAndStockId(user.getUserId(), stockId)) {
            return ResponseEntity.notFound().build();
        }

        watchlistRepository.deleteByUserIdAndStockId(user.getUserId(), stockId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/watchlist/check/{stockId} - Check if in watchlist
    @GetMapping("/check/{stockId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> isInWatchlist(@PathVariable UUID stockId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean inWatchlist = watchlistRepository.existsByUserIdAndStockId(user.getUserId(), stockId);
        return ResponseEntity.ok(Map.of("inWatchlist", inWatchlist));
    }
}
