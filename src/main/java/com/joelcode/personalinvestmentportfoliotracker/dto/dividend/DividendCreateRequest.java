package com.joelcode.personalinvestmentportfoliotracker.dto.dividend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DividendCreateRequest {

    // Stock-level dividend announcement
    // Dividend creation request DTO (input)
    @NotNull(message = "Stock ID is required")
    private final UUID stockId;

    @NotNull(message = "Amount per share is required")
    @PositiveOrZero(message = "Amount per share must be positive or zero")
    private final BigDecimal dividendPerShare;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "Pay date is required")
    private final LocalDateTime payDate;

    // Constructor
    @JsonCreator
    public DividendCreateRequest(
            @JsonProperty("stockId") UUID stockId,
            @JsonProperty("dividendPerShare") BigDecimal dividendPerShare,
            @JsonProperty("payDate") LocalDateTime payDate) {
        this.stockId = stockId;
        this.dividendPerShare = dividendPerShare;
        this.payDate = payDate;
    }

    // Getters
    public UUID getStockId() {return stockId;}

    public BigDecimal getDividendPerShare() {return dividendPerShare;}

    public LocalDateTime getPayDate() {return payDate;}
}