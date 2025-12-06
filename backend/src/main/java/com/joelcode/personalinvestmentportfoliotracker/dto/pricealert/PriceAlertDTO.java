package com.joelcode.personalinvestmentportfoliotracker.dto.pricealert;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PriceAlertDTO {

    @JsonProperty("alertId")
    private UUID alertId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("stockId")
    private UUID stockId;

    @JsonProperty("stockCode")
    private String stockCode;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("targetPrice")
    private BigDecimal targetPrice;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("triggeredAt")
    private LocalDateTime triggeredAt;

    public PriceAlertDTO() {}

    public PriceAlertDTO(UUID alertId, UUID userId, UUID stockId, String stockCode, String companyName,
                         String type, BigDecimal targetPrice, Boolean isActive, LocalDateTime createdAt, LocalDateTime triggeredAt) {
        this.alertId = alertId;
        this.userId = userId;
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.type = type;
        this.targetPrice = targetPrice;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.triggeredAt = triggeredAt;
    }

    public UUID getAlertId() { return alertId; }
    public void setAlertId(UUID alertId) { this.alertId = alertId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getStockId() { return stockId; }
    public void setStockId(UUID stockId) { this.stockId = stockId; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getTargetPrice() { return targetPrice; }
    public void setTargetPrice(BigDecimal targetPrice) { this.targetPrice = targetPrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(LocalDateTime triggeredAt) { this.triggeredAt = triggeredAt; }
}
