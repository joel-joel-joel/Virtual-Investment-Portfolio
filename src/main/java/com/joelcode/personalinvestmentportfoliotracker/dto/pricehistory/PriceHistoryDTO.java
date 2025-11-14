package com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PriceHistoryDTO {

    // Price history response DTO (output)
    private final UUID priceHistoryId;
    private final LocalDateTime closeDate;
    private final BigDecimal closePrice;
    private final UUID stockId;

    // Constructors
    public PriceHistoryDTO(UUID priceHistoryId, LocalDateTime closeDate, BigDecimal closePrice, UUID stockId) {
        this.priceHistoryId = priceHistoryId;
        this.closeDate = closeDate;
        this.closePrice = closePrice;
        this.stockId = stockId;
    }

    public PriceHistoryDTO(PriceHistory priceHistory) {
        this.priceHistoryId = priceHistory.getPriceHistoryId();
        this.closeDate = priceHistory.getCloseDate();
        this.closePrice = priceHistory.getClosePrice();
        this.stockId = priceHistory.getStock().getStockId();
    }

    // Getters
    public UUID getPriceHistoryId() {return priceHistoryId;}

    public LocalDateTime getCloseDate() {return closeDate;}

    public BigDecimal getClosePrice() {return closePrice;}

    public UUID getStockCode() {return stockId;}

}
