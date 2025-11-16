package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_snapshots")
public class PortfolioSnapshot {
    // This entity is a snapshot of the state of a portfolio at a certain point in time. Storing performance metrics,
    // values, investments etc...

    // Constructor

    public PortfolioSnapshot(UUID snapshotId, Account account, LocalDate snapshotDate, BigDecimal totalValue,
                             BigDecimal cashBalance, BigDecimal totalInvested, BigDecimal totalGain,
                             BigDecimal realizedGain, BigDecimal unrealizedGain,
                             BigDecimal totalDividends, BigDecimal roiPercentage,
                             BigDecimal dayChange, BigDecimal dayChangePercent) {
        this.snapshotId = snapshotId;
        this.account = account;
        this.snapshotDate = snapshotDate;
        this.totalValue = totalValue;
        this.cashBalance = cashBalance;
        this.totalInvested = totalInvested;
        this.totalGain = totalGain;
        this.realizedGain = realizedGain;
        this.unrealizedGain = unrealizedGain;
        this.totalDividends = totalDividends;
        this.roiPercentage = roiPercentage;
        this.dayChange = dayChange;
        this.dayChangePercent = dayChangePercent;
    }

    public PortfolioSnapshot() {}

    // Key fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID snapshotId;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalValue;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalInvested;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalGain;

    @Column(precision = 19, scale = 2)
    private BigDecimal dayChange;

    @Column(precision = 10, scale = 4)
    private BigDecimal dayChangePercent;

    @Column(precision = 19, scale = 2)
    private BigDecimal realizedGain = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal unrealizedGain = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalDividends = BigDecimal.ZERO;

    @Column(precision = 10, scale = 4)
    private BigDecimal roiPercentage = BigDecimal.ZERO;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public void setTotalInvested(BigDecimal totalInvested) {
        this.totalInvested = totalInvested;
    }

    public BigDecimal getTotalGain() {
        return totalGain;
    }

    public void setTotalGain(BigDecimal totalGain) {
        this.totalGain = totalGain;
    }

    public BigDecimal getDayChange() {
        return dayChange;
    }

    public void setDayChange(BigDecimal dayChange) {
        this.dayChange = dayChange;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public void setDayChangePercent(BigDecimal dayChangePercent) {
        this.dayChangePercent = dayChangePercent;
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

    public BigDecimal getRealizedGain() {return realizedGain;}

    public void setRealizedGain(BigDecimal realizedGain) {this.realizedGain = realizedGain;}

    public BigDecimal getUnrealizedGain() {return unrealizedGain;}

    public void setUnrealizedGain(BigDecimal unrealizedGain) {this.unrealizedGain = unrealizedGain;}

    public BigDecimal getTotalDividends() {return totalDividends;}

    public void setTotalDividends(BigDecimal totalDividends) {this.totalDividends = totalDividends;}

    public BigDecimal getRoiPercentage() {return roiPercentage;}

    public void setRoiPercentage(BigDecimal roiPercentage) {this.roiPercentage = roiPercentage;}

    // Helper Functions

    public BigDecimal getTotalGainPercent() {
        if (totalInvested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalGain
                .divide(totalInvested, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getMarketValue() {
        return totalValue.subtract(cashBalance);
    }
}