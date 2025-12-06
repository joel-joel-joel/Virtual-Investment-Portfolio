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

    public UUID getWatchlistId() { return watchlistId; }
    public void setWatchlistId(UUID watchlistId) { this.watchlistId = watchlistId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getStockId() { return stockId; }
    public void setStockId(UUID stockId) { this.stockId = stockId; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getPriceChange() { return priceChange; }
    public void setPriceChange(BigDecimal priceChange) { this.priceChange = priceChange; }

    public BigDecimal getPriceChangePercent() { return priceChangePercent; }
    public void setPriceChangePercent(BigDecimal priceChangePercent) { this.priceChangePercent = priceChangePercent; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
