package com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PortfolioSnapshotDTO {

    // Portfolio snapshot response DTO (output)
    private final UUID snapshotId;
    private final UUID accountId;
    private final LocalDate snapshotDate;
    private final BigDecimal totalValue;
    private final BigDecimal cashBalance;
    private final BigDecimal totalInvested;
    private final BigDecimal totalGain;
    private final BigDecimal totalGainPercent;
    private final BigDecimal dayChange;
    private final BigDecimal dayChangePercent;
    private final BigDecimal marketValue;

    // Constructor
    public PortfolioSnapshotDTO(UUID snapshotId, UUID accountId, LocalDate snapshotDate, BigDecimal totalValue,
                                BigDecimal cashBalance, BigDecimal totalInvested, BigDecimal totalGain,
                                BigDecimal totalGainPercent, BigDecimal dayChange, BigDecimal dayChangePercent,
                                BigDecimal marketValue) {
        this.snapshotId = snapshotId;
        this.accountId = accountId;
        this.snapshotDate = snapshotDate;
        this.totalValue = totalValue;
        this.cashBalance = cashBalance;
        this.totalInvested = totalInvested;
        this.totalGain = totalGain;
        this.totalGainPercent = totalGainPercent;
        this.dayChange = dayChange;
        this.dayChangePercent = dayChangePercent;
        this.marketValue = marketValue;
    }

    public PortfolioSnapshotDTO(PortfolioSnapshot snapshot) {
        this.snapshotId = snapshot.getSnapshotId();
        this.accountId = snapshot.getAccount().getAccountId();
        this.snapshotDate = snapshot.getSnapshotDate();
        this.totalValue = snapshot.getTotalValue();
        this.cashBalance = snapshot.getCashBalance();
        this.totalInvested = snapshot.getTotalInvested();
        this.totalGain = snapshot.getTotalGain();
        this.totalGainPercent = snapshot.getTotalGainPercent();
        this.dayChange = snapshot.getDayChange();
        this.dayChangePercent = snapshot.getDayChangePercent();
        this.marketValue = snapshot.getMarketValue();
    }

    // Getters
    public UUID getSnapshotId() {
        return snapshotId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getTotalInvested() {
        return totalInvested;
    }

    public BigDecimal getTotalGain() {
        return totalGain;
    }

    public BigDecimal getTotalGainPercent() {
        return totalGainPercent;
    }

    public BigDecimal getDayChange() {
        return dayChange;
    }

    public BigDecimal getDayChangePercent() {
        return dayChangePercent;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }
}