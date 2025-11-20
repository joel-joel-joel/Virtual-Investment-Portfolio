package com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PortfolioSnapshotCreateRequest {

    // Portfolio snapshot creation request DTO (input)
    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Snapshot date is required")
    private LocalDate snapshotDate;

    @NotNull(message = "Total value is required")
    @DecimalMin(value = "0.00", message = "Total value cannot be negative")
    private BigDecimal totalValue;

    @NotNull(message = "Cash balance is required")
    @DecimalMin(value = "0.00", message = "Cash balance cannot be negative")
    private BigDecimal cashBalance;

    @NotNull(message = "Total invested is required")
    @DecimalMin(value = "0.00", message = "Total invested cannot be negative")
    private BigDecimal totalCostBasis;

    @NotNull(message = "Total gain is required")
    @DecimalMin(value = "0.00", message = "Total gain cannot be negative")
    private BigDecimal totalGain;

    @NotNull(message = "Day change is required")
    @DecimalMin(value = "0.00", message = "Day change cannot be negative")
    private BigDecimal dayChange;

    @NotNull(message = "Day change percent is required")
    @DecimalMin(value = "0.00", message = "Day change percent cannot be negative")
    @DecimalMin(value = "0.00", message = "Day change percent cannot be greater than 100")
    private BigDecimal dayChangePercent;

    // Constructor
    public PortfolioSnapshotCreateRequest(UUID accountId, LocalDate snapshotDate, BigDecimal totalValue,
                                          BigDecimal cashBalance, BigDecimal totalCostBasis, BigDecimal totalGain,
                                          BigDecimal dayChange) {
        this.accountId = accountId;
        this.snapshotDate = snapshotDate;
        this.totalValue = totalValue;
        this.cashBalance = cashBalance;
        this.totalCostBasis = totalCostBasis;
        this.totalGain = totalGain;
        this.dayChange = dayChange;
    }

    public PortfolioSnapshotCreateRequest() {}

    // Getters and setters
    public UUID getAccountId() {return accountId;}

    public LocalDate getSnapshotDate() {return snapshotDate;}

    public BigDecimal getTotalValue() {return totalValue;}

    public BigDecimal getCashBalance() {return cashBalance;}

    public BigDecimal getTotalCostBasis() {return totalCostBasis;}

    public BigDecimal getTotalGain() {return totalGain;}

    public BigDecimal getDayChange() {return dayChange;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public void setSnapshotDate(LocalDate snapshotDate) {this.snapshotDate = snapshotDate;}

    public void setTotalValue(BigDecimal totalValue) {this.totalValue = totalValue;}

    public void setCashBalance(BigDecimal cashBalance) {this.cashBalance = cashBalance;}

    public void setTotalCostBasis(BigDecimal totalCostBasis) {this.totalCostBasis = totalCostBasis;}

    public void setTotalGain(BigDecimal totalGain) {this.totalGain = totalGain;}

    public void setDayChangePercent(BigDecimal dayChangePercent) {this.dayChangePercent = dayChangePercent;}
}