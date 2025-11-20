package com.joelcode.personalinvestmentportfoliotracker.dto.portfolio;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PortfolioPerformanceDTO {

    // Define key fields
    private UUID userId;
    private UUID accountId;
    private BigDecimal totalPortfolioValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalRealizedGain;
    private BigDecimal totalUnrealizedGain;
    private BigDecimal totalDividends;
    private BigDecimal cashBalance;
    private BigDecimal roiPercentage;
    private BigDecimal dailyGain;
    private BigDecimal monthlyGain;

    // Constructors
    public PortfolioPerformanceDTO(UUID userId, UUID accountId, BigDecimal totalPortfolioValue, BigDecimal totalCostBasis,
                                   BigDecimal totalRealizedGain, BigDecimal totalUnrealizedGain,
                                   BigDecimal totalDividends, BigDecimal cashBalance,
                                   BigDecimal roiPercentage, BigDecimal dailyGain, BigDecimal monthlyGain) {
        this.userId = userId;
        this.accountId = accountId;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalCostBasis = totalCostBasis;
        this.totalRealizedGain = totalRealizedGain;
        this.totalUnrealizedGain = totalUnrealizedGain;
        this.totalDividends = totalDividends;
        this.cashBalance = cashBalance;
        this.roiPercentage = roiPercentage;
        this.dailyGain = dailyGain;
        this.monthlyGain = monthlyGain;
    }

    public PortfolioPerformanceDTO(UUID userId, BigDecimal totalPortfolioValue, BigDecimal totalCostBasis,
                                   BigDecimal totalRealizedGain, BigDecimal totalUnrealizedGain,
                                   BigDecimal totalDividends, BigDecimal cashBalance,
                                   BigDecimal roiPercentage, BigDecimal dailyGain, BigDecimal monthlyGain) {
        this.userId = userId;
        this.accountId = null;
        this.totalPortfolioValue = totalPortfolioValue;
        this.totalCostBasis = totalCostBasis;
        this.totalRealizedGain = totalRealizedGain;
        this.totalUnrealizedGain = totalUnrealizedGain;
        this.totalDividends = totalDividends;
        this.cashBalance = cashBalance;
        this.roiPercentage = roiPercentage;
        this.dailyGain = dailyGain;
        this.monthlyGain = monthlyGain;
    }



    public PortfolioPerformanceDTO() {}


    // Getters and Setters
    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public UUID getAccountId() {return accountId;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public BigDecimal getTotalPortfolioValue() {return totalPortfolioValue;}

    public void setTotalPortfolioValue(BigDecimal totalPortfolioValue) {this.totalPortfolioValue = totalPortfolioValue;}

    public BigDecimal getTotalCostBasis() {return totalCostBasis;}

    public void setTotalCostBasis(BigDecimal totalCostBasis) {this.totalCostBasis = totalCostBasis;}

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
