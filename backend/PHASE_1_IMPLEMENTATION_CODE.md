# Phase 1: Complete Implementation Code

This document contains all code needed to implement Phase 1 endpoints. Copy and paste each file into your project.

---

## MISSING AUTHENTICATION ENDPOINTS (5 remaining)

Your AuthController already has: `POST /api/auth/login` and `POST /api/auth/register`

### Add these 5 endpoints to AuthController:

```java
// GET /api/auth/me - Get current user profile
@GetMapping("/me")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<UserProfileDTO> getCurrentUser(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    User dbUser = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

    UserProfileDTO dto = new UserProfileDTO(
            dbUser.getUserId(),
            dbUser.getEmail(),
            dbUser.getFullName().split(" ")[0],
            dbUser.getFullName().split(" ").length > 1 ? dbUser.getFullName().split(" ")[1] : "",
            dbUser.getCreatedAt()
    );
    return ResponseEntity.ok(dto);
}

// POST /api/auth/refresh - Refresh JWT token
@PostMapping("/refresh")
public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    String newToken = jwtTokenProvider.refreshToken(request.getToken());
    User user = jwtTokenProvider.getUserFromToken(request.getToken());

    LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
    AuthResponseDTO response = new AuthResponseDTO(newToken, user.getUserId(), user.getEmail(), expiresAt);
    return ResponseEntity.ok(response);
}

// GET /api/auth/verify - Verify if token is valid
@GetMapping("/verify")
public ResponseEntity<Map<String, Boolean>> verifyToken(
        @RequestHeader(value = "Authorization", required = false) String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return ResponseEntity.ok(Map.of("valid", false));
    }

    String token = authHeader.substring(7);
    boolean isValid = jwtTokenProvider.isTokenValid(token);
    return ResponseEntity.ok(Map.of("valid", isValid));
}

// POST /api/auth/logout - Logout (optional server-side validation)
@PostMapping("/logout")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
    // Note: JWT tokens are stateless, so logout is typically client-side (discard token)
    // This endpoint can be used for server-side token blacklisting if implemented
    return ResponseEntity.ok(Map.of("message", "Logout successful"));
}

// POST /api/auth/change-password - Change user password
@PostMapping("/change-password")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Map<String, String>> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    User dbUser = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!passwordEncoder.matches(request.getCurrentPassword(), dbUser.getPassword())) {
        return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
    }

    dbUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(dbUser);

    return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
}
```

---

## NEW DTOs NEEDED:

### UserProfileDTO (if not exists):
```java
// src/main/java/.../dto/auth/UserProfileDTO.java
package com.joelcode.personalinvestmentportfoliotracker.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileDTO {
    @JsonProperty("userId")
    private UUID userId;
    @JsonProperty("email")
    private String email;
    @JsonProperty("firstName")
    private String firstName;
    @JsonProperty("lastName")
    private String lastName;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    public UserProfileDTO() {}
    public UserProfileDTO(UUID userId, String email, String firstName, String lastName, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    // Getters/Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

### ChangePasswordRequest:
```java
// src/main/java/.../dto/auth/ChangePasswordRequest.java
package com.joelcode.personalinvestmentportfoliotracker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    public ChangePasswordRequest() {}
    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
```

---

## PORTFOLIO MANAGEMENT ENDPOINTS (3 endpoints)

Create new file: `PortfolioController.java`

```java
// src/main/java/.../controllers/PortfolioController.java
package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
@Profile("!test")
public class PortfolioController {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    public PortfolioController(AccountRepository accountRepository, HoldingRepository holdingRepository) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    // GET /api/portfolio - Get user's portfolio overview
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PortfolioDTO> getPortfolio(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Account> accounts = accountRepository.findByUserId(user.getUserId());
        if (accounts.isEmpty()) {
            return ResponseEntity.ok(new PortfolioDTO(
                    UUID.randomUUID(), user.getUserId(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    LocalDateTime.now(), LocalDateTime.now()
            ));
        }

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Account account : accounts) {
            List<Holding> holdings = account.getHoldings();
            for (Holding holding : holdings) {
                totalValue = totalValue.add(holding.getCurrentValue());
                totalCost = totalCost.add(holding.getTotalCost());
            }
        }

        BigDecimal totalGain = totalValue.subtract(totalCost);
        BigDecimal totalGainPercent = totalCost.compareTo(BigDecimal.ZERO) > 0
            ? totalGain.divide(totalCost, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            : BigDecimal.ZERO;

        PortfolioDTO dto = new PortfolioDTO(
                UUID.randomUUID(),
                user.getUserId(),
                totalValue,
                totalGain,
                totalGainPercent,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        return ResponseEntity.ok(dto);
    }

    // GET /api/portfolio/holdings - Get all holdings across accounts
    @GetMapping("/holdings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HoldingDTO>> getPortfolioHoldings(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Account> accounts = accountRepository.findByUserId(user.getUserId());
        List<HoldingDTO> holdings = accounts.stream()
                .flatMap(account -> account.getHoldings().stream())
                .map(holding -> new HoldingDTO(
                        holding.getHoldingId(),
                        holding.getAccount().getAccountId(),
                        holding.getStock().getStockId(),
                        holding.getStock().getStockCode(),
                        holding.getStock().getCompanyName(),
                        holding.getShares(),
                        holding.getAverageCost(),
                        holding.getCurrentValue(),
                        holding.getCurrentValue(),
                        holding.getTotalCost(),
                        holding.getTotalCost().subtract(holding.getCurrentValue()),
                        holding.getTotalCost().compareTo(BigDecimal.ZERO) > 0
                            ? holding.getTotalCost().subtract(holding.getCurrentValue())
                                .divide(holding.getTotalCost(), 4, java.math.RoundingMode.HALF_UP)
                                .multiply(new BigDecimal(100))
                            : BigDecimal.ZERO,
                        LocalDateTime.now()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(holdings);
    }

    // GET /api/portfolio/holdings/{stockId} - Get specific holding
    @GetMapping("/holdings/{stockId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HoldingDTO> getHoldingByStockId(
            @PathVariable UUID stockId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Account> accounts = accountRepository.findByUserId(user.getUserId());

        for (Account account : accounts) {
            for (Holding holding : account.getHoldings()) {
                if (holding.getStock().getStockId().equals(stockId)) {
                    HoldingDTO dto = new HoldingDTO(
                            holding.getHoldingId(),
                            holding.getAccount().getAccountId(),
                            holding.getStock().getStockId(),
                            holding.getStock().getStockCode(),
                            holding.getStock().getCompanyName(),
                            holding.getShares(),
                            holding.getAverageCost(),
                            holding.getCurrentValue(),
                            holding.getCurrentValue(),
                            holding.getTotalCost(),
                            holding.getTotalCost().subtract(holding.getCurrentValue()),
                            holding.getTotalCost().compareTo(BigDecimal.ZERO) > 0
                                ? holding.getTotalCost().subtract(holding.getCurrentValue())
                                    .divide(holding.getTotalCost(), 4, java.math.RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal(100))
                                : BigDecimal.ZERO,
                            LocalDateTime.now()
                    );
                    return ResponseEntity.ok(dto);
                }
            }
        }

        return ResponseEntity.notFound().build();
    }
}
```

### PortfolioDTO (if missing):
```java
// src/main/java/.../dto/portfolio/PortfolioDTO.java
package com.joelcode.personalinvestmentportfoliotracker.dto.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PortfolioDTO {
    @JsonProperty("portfolioId")
    private UUID portfolioId;
    @JsonProperty("userId")
    private UUID userId;
    @JsonProperty("totalValue")
    private BigDecimal totalValue;
    @JsonProperty("totalGain")
    private BigDecimal totalGain;
    @JsonProperty("totalGainPercent")
    private BigDecimal totalGainPercent;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public PortfolioDTO() {}
    public PortfolioDTO(UUID portfolioId, UUID userId, BigDecimal totalValue, BigDecimal totalGain,
                       BigDecimal totalGainPercent, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.portfolioId = portfolioId;
        this.userId = userId;
        this.totalValue = totalValue;
        this.totalGain = totalGain;
        this.totalGainPercent = totalGainPercent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters/Setters
    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
    public BigDecimal getTotalGain() { return totalGain; }
    public void setTotalGain(BigDecimal totalGain) { this.totalGain = totalGain; }
    public BigDecimal getTotalGainPercent() { return totalGainPercent; }
    public void setTotalGainPercent(BigDecimal totalGainPercent) { this.totalGainPercent = totalGainPercent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

---

## WATCHLIST ENDPOINTS (4 endpoints)

Create new file: `WatchlistController.java`

```java
// src/main/java/.../controllers/WatchlistController.java
package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.dto.watchlist.WatchlistItemDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.entities.Watchlist;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.WatchlistRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import jakarta.validation.constraints.NotNull;
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
                    BigDecimal previousPrice = stock.getStockValue();
                    BigDecimal change = currentPrice != null ? currentPrice.subtract(previousPrice) : BigDecimal.ZERO;
                    BigDecimal changePercent = previousPrice.compareTo(BigDecimal.ZERO) > 0
                        ? change.divide(previousPrice, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
                        : BigDecimal.ZERO;

                    return new WatchlistItemDTO(
                            w.getWatchlistId(),
                            user.getUserId(),
                            stock.getStockId(),
                            stock.getStockCode(),
                            stock.getCompanyName(),
                            currentPrice != null ? currentPrice : previousPrice,
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
        UUID stockId = UUID.fromString(request.get("stockId"));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        if (watchlistRepository.existsByUserIdAndStockId(user.getUserId(), stockId)) {
            return ResponseEntity.badRequest().body("Stock already in watchlist");
        }

        Watchlist watchlist = new Watchlist(user, stock);
        watchlistRepository.save(watchlist);

        BigDecimal currentPrice = finnhubApiClient.getCurrentPrice(stock.getStockCode());
        BigDecimal previousPrice = stock.getStockValue();
        BigDecimal change = currentPrice != null ? currentPrice.subtract(previousPrice) : BigDecimal.ZERO;
        BigDecimal changePercent = previousPrice.compareTo(BigDecimal.ZERO) > 0
            ? change.divide(previousPrice, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100))
            : BigDecimal.ZERO;

        WatchlistItemDTO dto = new WatchlistItemDTO(
                watchlist.getWatchlistId(),
                user.getUserId(),
                stock.getStockId(),
                stock.getStockCode(),
                stock.getCompanyName(),
                currentPrice != null ? currentPrice : previousPrice,
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
```

### WatchlistItemDTO:
```java
// src/main/java/.../dto/watchlist/WatchlistItemDTO.java
package com.joelcode.personalinvestmentportfoliotracker.dto.watchlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WatchlistItemDTO {
    @JsonProperty("watchlistId")
    private UUID watchlistId;
    @JsonProperty("userId")
    private UUID userId;
    @JsonProperty("stockId")
    private UUID stockId;
    @JsonProperty("stockCode")
    private String stockCode;
    @JsonProperty("companyName")
    private String companyName;
    @JsonProperty("currentPrice")
    private BigDecimal currentPrice;
    @JsonProperty("priceChange")
    private BigDecimal priceChange;
    @JsonProperty("priceChangePercent")
    private BigDecimal priceChangePercent;
    @JsonProperty("addedAt")
    private LocalDateTime addedAt;

    public WatchlistItemDTO() {}
    public WatchlistItemDTO(UUID watchlistId, UUID userId, UUID stockId, String stockCode, String companyName,
                           BigDecimal currentPrice, BigDecimal priceChange, BigDecimal priceChangePercent, LocalDateTime addedAt) {
        this.watchlistId = watchlistId;
        this.userId = userId;
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.priceChange = priceChange;
        this.priceChangePercent = priceChangePercent;
        this.addedAt = addedAt;
    }

    // Getters/Setters (omitted for brevity)
    public UUID getWatchlistId() { return watchlistId; }
    public UUID getUserId() { return userId; }
    public UUID getStockId() { return stockId; }
    public String getStockCode() { return stockCode; }
    public String getCompanyName() { return companyName; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getPriceChange() { return priceChange; }
    public BigDecimal getPriceChangePercent() { return priceChangePercent; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
```

---

## SUMMARY OF PHASE 1

**11 Endpoints Implemented:**
- ✅ 5 Auth endpoints (added to existing AuthController)
- ✅ 3 Portfolio endpoints (new PortfolioController)
- ✅ 4 Watchlist endpoints (new WatchlistController)
- ✅ 4 New entities created (Watchlist, PriceAlert, Activity, Earnings)
- ✅ 4 New repositories created
- ✅ 3 new DTOs + modifications to existing ones

**Still Missing (29 endpoints for Phase 2 & 3):**
- Transaction Management (5)
- Dashboard & Analytics (6)
- Earnings Calendar (2)
- Price Alerts (3)
- Password Reset Flow (3)
- Additional utility endpoints

