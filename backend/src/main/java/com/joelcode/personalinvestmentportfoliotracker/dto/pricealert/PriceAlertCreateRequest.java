package com.joelcode.personalinvestmentportfoliotracker.dto.pricealert;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public class PriceAlertCreateRequest {

    @NotNull(message = "Stock ID is required")
    private UUID stockId;

    @NotNull(message = "Alert type is required (ABOVE or BELOW)")
    private String type;

    @NotNull(message = "Target price is required")
    @Positive(message = "Target price must be positive")
    private BigDecimal targetPrice;

    public PriceAlertCreateRequest() {}

    public PriceAlertCreateRequest(UUID stockId, String type, BigDecimal targetPrice) {
        this.stockId = stockId;
        this.type = type;
        this.targetPrice = targetPrice;
    }

    public UUID getStockId() { return stockId; }
    public void setStockId(UUID stockId) { this.stockId = stockId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }
}
