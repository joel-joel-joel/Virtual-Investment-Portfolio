package com.joelcode.personalinvestmentportfoliotracker.dto.portfolio;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class AccountSummaryDTO {

    // Getters and setters
    // Define key fields
    private UUID userId;
    private UUID accountId;
    private String accountName;
    private BigDecimal totalCostBasis;
    private BigDecimal totalMarketValue;
    private BigDecimal totalUnrealizedGain;
    private BigDecimal totalDividends;
    private BigDecimal totalCashBalance;
    private List<HoldingSummaryDTO> holdings;

    // Constructor
    public AccountSummaryDTO(UUID userId, UUID accountId, String accountName, BigDecimal totalCostBasis, BigDecimal totalMarketValue,
                             BigDecimal totalUnrealizedGain, BigDecimal totalDividends, BigDecimal totalCashBalance,
                             List<HoldingSummaryDTO> holdings) {

        this.userId = userId;
        this.accountId = accountId;
        this.accountName = accountName;
        this.totalCostBasis = totalCostBasis;
        this.totalMarketValue = totalMarketValue;
        this.totalUnrealizedGain = totalUnrealizedGain;
        this.totalDividends = totalDividends;
        this.totalCashBalance = totalCashBalance;
        this.holdings = holdings;
    }

    public AccountSummaryDTO() {}

    // Getters and setters
    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public UUID getAccountId() {return accountId;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public String getAccountName() {return accountName;}

    public void setAccountName(String accountName) {this.accountName = accountName;}

    public BigDecimal getTotalCostBasis() {return totalCostBasis;}

    public void setTotalCostBasis(BigDecimal totalCostBasis) {this.totalCostBasis = totalCostBasis;}

    public BigDecimal getTotalMarketValue() {return totalMarketValue;}

    public void setTotalMarketValue(BigDecimal totalMarketValue) {this.totalMarketValue = totalMarketValue;}

    public BigDecimal getTotalUnrealizedGain() {return totalUnrealizedGain;}

    public void setTotalUnrealizedGain(BigDecimal totalUnrealizedGain) {this.totalUnrealizedGain = totalUnrealizedGain;}

    public BigDecimal getTotalDividends() {return totalDividends;}

    public void setTotalDividends(BigDecimal totalDividends) {this.totalDividends = totalDividends;}

    public BigDecimal getTotalCashBalance() {return totalCashBalance;}

    public void setTotalCashBalance(BigDecimal totalCashBalance) {this.totalCashBalance = totalCashBalance;}

    public List<HoldingSummaryDTO> getHoldings() {return holdings;}

    public void setHoldings(List<HoldingSummaryDTO> holdings) {this.holdings = holdings;}
}
