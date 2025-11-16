package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.model;

import java.math.BigDecimal;
import java.util.UUID;

public class PortfolioPerformanceDTO {

    // Define key fields
    private UUID accountId;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalInvested;
    private BigDecimal totalRealizedGain;
    private BigDecimal totalUnrealizedGain;
    private BigDecimal totalDividends;
    private BigDecimal cashBalance;
    private BigDecimal roiPercentage;
    private BigDecimal dailyGain;
    private BigDecimal monthlyGain;

    // Constructors
    public PortfolioPerformanceDTO(UUID accountId, BigDecimal totalPortfolioValue, BigDecimal totalInvested,
                                   BigDecimal totalRealizedGain, BigDecimal totalUnrealizedGain,
                                   BigDecimal totalDividends, BigDecimal cashBalance, BigDecimal roiPercentage,
                                   BigDecimal dailyGain, BigDecimal monthlyGain) {
        this.accountId = accountId;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalInvested = totalInvested;
        this.totalRealizedGain = totalRealizedGain;
        this.totalUnrealizedGain = totalUnrealizedGain;
        this.totalDividends = totalDividends;
    }

    public PortfolioPerformanceDTO() {}

    // Getters and Setters
    public UUID getAccountId() {return accountId;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public BigDecimal getTotalPortfolioValue() {return totalPortfolioValue;}

    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {this.totalPortfolioValue = totalPortfolioValue;}

    public BigDecimal getTotalInvested() {return totalInvested;}

    public void setTotalInvested(BigDecimal totalInvested) {this.totalInvested = totalInvested;}

    public BigDecimal getTotalRealizedGain() {return totalRealizedGain;}

    public void setTotalRealizedGain(BigDecimal totalRealizedGain) {this.totalRealizedGain = totalRealizedGain;}

    public BigDecimal getTotalUnrealizedGain() {return totalUnrealizedGain;}

    public void setTotalUnrealizedGain(BigDecimal totalUnrealizedGain) {this.totalUnrealizedGain = totalUnrealizedGain;}

    public BigDecimal getTotalDividends() {return totalDividends;}

    public void setTotalDividends(BigDecimal totalDividends) {this.totalDividends = totalDividends;}

    public BigDecimal getCashBalance() {return cashBalance;}

    public void setCashBalance(BigDecimal cashBalance) {this.cashBalance = cashBalance;}

    public BigDecimal getRoiPercentage() {return roiPercentage;}

    public void setRoiPercentage(BigDecimal roiPercentage) {this.roiPercentage = roiPercentage;}

    public BigDecimal getMonthlyGain() {return monthlyGain;}

    public void setMonthlyGain(BigDecimal monthlyGain) {this.monthlyGain = monthlyGain;}

    public BigDecimal getDailyGain() {return dailyGain;}

    public void setDailyGain(BigDecimal dailyGain) {this.dailyGain = dailyGain;}
}
