package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.model;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PortfolioOverviewDTO {

    // Portfolio overview response DTO (output)
    private UUID accountId;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalInvested;
    private BigDecimal totalUnrealizedGain;
    private BigDecimal totalRealizedGain;
    private BigDecimal totalDividends;
    private BigDecimal cashBalance;
    private List<HoldingDTO> holdings;

    // Constructor
    public PortfolioOverviewDTO (UUID accountId, BigDecimal totalPortfolioValue, BigDecimal totalInvested,
                                 BigDecimal totalUnrealizedGain, BigDecimal totalRealizedGain,
                                 BigDecimal totalDividends, BigDecimal cashBalance, List<HoldingDTO> holdings) {
        this.accountId = accountId;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalInvested = totalInvested;
        this.totalUnrealizedGain = totalUnrealizedGain;
        this.totalRealizedGain = totalRealizedGain;
        this.totalDividends = totalDividends;
        this.cashBalance = cashBalance;
        this.holdings = holdings;
    }

    public PortfolioOverviewDTO() {}

    // Getters and setters
    public UUID getAccountId() {return accountId;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public BigDecimal getTotalPortfolioValue() {return totalPortfolioValue;}

    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {this.totalPortfolioValue = totalPortfolioValue;}

    public BigDecimal getTotalInvested() {return totalInvested;}

    public void setTotalInvested(BigDecimal totalInvested) {this.totalInvested = totalInvested;}

    public BigDecimal getTotalUnrealizedGain() {return totalUnrealizedGain;}

    public void setTotalUnrealizedGain(BigDecimal totalUnrealizedGain) {this.totalUnrealizedGain = totalUnrealizedGain;}

    public BigDecimal getTotalRealizedGain() {return totalRealizedGain;}

    public void setTotalRealizedGain(BigDecimal totalRealizedGain) {this.totalRealizedGain = totalRealizedGain;}

    public BigDecimal getTotalDividends() {return totalDividends;}

    public void setTotalDividends(BigDecimal totalDividends) {this.totalDividends = totalDividends;}

    public BigDecimal getCashBalance() {return cashBalance;}

    public void setCashBalance(BigDecimal cashBalance) {this.cashBalance = cashBalance;}

    public List<HoldingDTO> getHoldings() {return holdings;}

    public void setHoldings(List<HoldingDTO> holdings) {this.holdings = holdings;}
}
