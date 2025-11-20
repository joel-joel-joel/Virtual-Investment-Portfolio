package com.joelcode.personalinvestmentportfoliotracker.dto.holding;

import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class HoldingDTO {

    // Holding response DTO (output)
    private UUID holdingId;
    private UUID accountId;
    private UUID stockId;
    private String stockSymbol;
    private BigDecimal quantity;
    private BigDecimal averageCostBasis;
    private BigDecimal totalCostBasis;
    private BigDecimal realizedGain;
    private LocalDateTime firstPurchaseDate;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal unrealizedGain;
    private BigDecimal unrealizedGainPercent;

    // Constructor
    public HoldingDTO(UUID holdingId, UUID accountId, UUID stockId, String stockSymbol, BigDecimal quantity,
                      BigDecimal averageCostBasis, BigDecimal totalCostBasis, BigDecimal realizedGain,
                      LocalDateTime firstPurchaseDate, BigDecimal currentPrice, BigDecimal currentValue,
                      BigDecimal unrealizedGain, BigDecimal unrealizedGainPercent) {
        this.holdingId = holdingId;
        this.accountId = accountId;
        this.stockId = stockId;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
        this.totalCostBasis = totalCostBasis;
        this.realizedGain = realizedGain;
        this.firstPurchaseDate = firstPurchaseDate;
        this.currentPrice = currentPrice;
        this.currentValue = currentValue;
        this.unrealizedGain = unrealizedGain;
        this.unrealizedGainPercent = unrealizedGainPercent;
    }

    public HoldingDTO(Holding holding, BigDecimal currentPrice) {
        this.holdingId = holding.getHoldingId();
        this.accountId = holding.getAccount().getAccountId();
        this.stockId = holding.getStock().getStockId();
        this.stockSymbol = holding.getStock().getStockCode();
        this.quantity = holding.getQuantity();
        this.averageCostBasis = holding.getAverageCostBasis();
        this.totalCostBasis = holding.getTotalCostBasis();
        this.realizedGain = holding.getRealizedGain();
        this.firstPurchaseDate = holding.getFirstPurchaseDate();
        this.currentPrice = currentPrice;
        this.currentValue = holding.getCurrentValue(currentPrice);
        this.unrealizedGain = holding.getUnrealizedGain(currentPrice);
        this.unrealizedGainPercent = holding.getUnrealizedGainPercent(currentPrice);
    }

    public HoldingDTO() {}

    // Getters and setters
    public UUID getHoldingId() {
        return holdingId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getStockId() {
        return stockId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCostBasis() {
        return averageCostBasis;
    }

    public BigDecimal getTotalCostBasis() {
        return totalCostBasis;
    }

    public BigDecimal getRealizedGain() {
        return realizedGain;
    }

    public LocalDateTime getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public BigDecimal getUnrealizedGain() {
        return unrealizedGain;
    }

    public BigDecimal getUnrealizedGainPercent() {
        return unrealizedGainPercent;
    }

    public UUID setHoldingId(UUID holdingId) {return this.holdingId = holdingId;}

    public UUID setAccountId(UUID accountId) {return this.accountId = accountId;}

    public UUID setStockId(UUID stockId) {return this.stockId = stockId;}

    public String setStockSymbol(String stockSymbol) {return this.stockSymbol = stockSymbol;}

    public BigDecimal setQuantity(BigDecimal quantity) {return this.quantity = quantity;}

    public BigDecimal setAverageCostBasis(BigDecimal averageCostBasis) {return this.averageCostBasis = averageCostBasis;}

    public BigDecimal setTotalCostBasis(BigDecimal totalCostBasis) {return this.totalCostBasis = totalCostBasis;}

    public BigDecimal setRealizedGain(BigDecimal realizedGain) {return this.realizedGain = realizedGain;}

    public BigDecimal setUnrealizedGain(BigDecimal unrealizedGain) {return this.unrealizedGain = unrealizedGain;}

    public LocalDateTime setFirstPurchaseDate(LocalDateTime firstPurchaseDate) {return this.firstPurchaseDate = firstPurchaseDate;}

    public BigDecimal setCurrentPrice(BigDecimal currentPrice) {return this.currentPrice = currentPrice;}

    public BigDecimal setCurrentValue(BigDecimal currentValue) {return this.currentValue = currentValue;}
}