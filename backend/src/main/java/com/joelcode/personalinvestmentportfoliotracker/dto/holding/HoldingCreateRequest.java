package com.joelcode.personalinvestmentportfoliotracker.dto.holding;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class HoldingCreateRequest {

    // Holding creation request DTO (input)
    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Stock ID is required")
    private UUID stockId;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be greater than zero")
    private BigDecimal quantity;

    @NotNull(message = "Average cost basis is required")
    @DecimalMin(value = "0.01", message = "Average cost basis must be greater than zero")
    private BigDecimal averageCostBasis;

    @NotNull(message = "Total cost basis is required")
    @DecimalMin(value = "0.01", message = "Total cost basis must be greater than zero")
    private BigDecimal totalCostBasis;

    @NotNull(message = "Industry is required")
    private String sector;

    // Constructor
    public HoldingCreateRequest(UUID accountId, UUID stockId, BigDecimal quantity, BigDecimal averageCostBasis, BigDecimal totalCostBasis, String sector) {
        this.accountId = accountId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.averageCostBasis = averageCostBasis;
        this.totalCostBasis = totalCostBasis;
        this.sector = sector;
    }

    public HoldingCreateRequest() {}

    // Getters and setters
    public UUID getAccountId() {return accountId;}

    public UUID getStockId() {return stockId;}

    public BigDecimal getQuantity() {return quantity;}

    public BigDecimal getAverageCostBasis() {return averageCostBasis;}

    public BigDecimal getTotalCostBasis() {return totalCostBasis;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public void setStockId(UUID stockId) {this.stockId = stockId;}

    public void setQuantity(BigDecimal quantity) {this.quantity = quantity;}

    public void setAverageCostBasis(BigDecimal averageCostBasis) {this.averageCostBasis = averageCostBasis;}

    public void setTotalCostBasis(BigDecimal totalCostBasis) {this.totalCostBasis = totalCostBasis;}

    public HoldingCreateRequest setAccountId(String accountId) {this.accountId = UUID.fromString(accountId); return this;}

    public String getSector() {return sector;}

    public void setSector(String sector) {this.sector = sector;}

}