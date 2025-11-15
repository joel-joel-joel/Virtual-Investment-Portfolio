package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "holdings")
public class Holding {

    // This entity tracks the current position for each stock in an account inclusive of current quantity, gains/losses
    // and cost basis (price paid for share0

    // Constructor

    public Holding(UUID holdingId, Account account, Stock stock, BigDecimal quantity, BigDecimal averageCostBasis,
                   BigDecimal totalCostBasis, BigDecimal realizedGain, LocalDateTime firstPurchaseDate) {
        this.holdingId = holdingId;
        this.account = account;
        this.stock = stock;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
        this.totalCostBasis = totalCostBasis;
        this.realizedGain = realizedGain;
        this.firstPurchaseDate = firstPurchaseDate;
    }

    public Holding() {}

    // Key fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID holdingId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal averageCostBasis;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalCostBasis;

    @Column(precision = 19, scale = 2)
    private BigDecimal realizedGain = BigDecimal.ZERO;

    private LocalDateTime firstPurchaseDate;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(UUID holdingId) {
        this.holdingId = holdingId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAverageCostBasis() {
        return averageCostBasis;
    }

    public void setAverageCostBasis(BigDecimal averageCostBasis) {
        this.averageCostBasis = averageCostBasis;
    }

    public BigDecimal getTotalCostBasis() {
        return totalCostBasis;
    }

    public void setTotalCostBasis(BigDecimal totalCostBasis) {
        this.totalCostBasis = totalCostBasis;
    }

    public BigDecimal getRealizedGainLoss() {
        return realizedGain;
    }

    public void setRealizedGainLoss(BigDecimal realizedGain) {
        this.realizedGain = realizedGain;
    }

    public LocalDateTime getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public void setFirstPurchaseDate(LocalDateTime firstPurchaseDate) {
        this.firstPurchaseDate = firstPurchaseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper Functions

    public BigDecimal getCurrentValue(BigDecimal currentPrice) {
        return quantity.multiply(currentPrice);
    }

    public BigDecimal getUnrealizedGain(BigDecimal currentPrice) {
        return getCurrentValue(currentPrice).subtract(totalCostBasis);
    }

    public BigDecimal getUnrealizedGainPercent(BigDecimal currentPrice) {
        if (totalCostBasis.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getUnrealizedGain(currentPrice)
                .divide(totalCostBasis, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getTotalGainLoss(BigDecimal currentPrice) {
        return getUnrealizedGain(currentPrice).add(realizedGain);
    }
}