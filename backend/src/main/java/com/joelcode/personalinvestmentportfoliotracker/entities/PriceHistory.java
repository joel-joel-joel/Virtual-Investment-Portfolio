package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table (name = "price_history")
public class PriceHistory {

    // This entity stores the price movement history of a stock from a certain point in time

    // Constructors
    public PriceHistory(LocalDateTime closeDate, BigDecimal closePrice, Stock stock) {
        this.stock = stock;
        this.closeDate = closeDate;
        this.closePrice = closePrice;
    }

    public PriceHistory() {}


    // Column
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "price_history_id")
    private UUID priceHistoryId;

    @Column(name = "close_date", nullable = false)
    private LocalDateTime closeDate;

    @Column(name = "close_price", nullable = false)
    private BigDecimal closePrice;


    // Relationships
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "stock_id", nullable = false)
    private Stock stock;


    // Getter and setters
    public UUID getPriceHistoryId() {
        return priceHistoryId;
    }

    public void setPriceHistoryId(UUID priceHistoryId) {
        this.priceHistoryId = priceHistoryId;
    }

    public LocalDateTime getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDateTime closeDate) {
        this.closeDate = closeDate;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public UUID getStockId() {return stock.getStockId();}

    public void setStockId(UUID stockId) {this.stock.setStockId(stockId);}


    // Helper functions (For filtering)
    public boolean isAbove(BigDecimal threshold) {
        return closePrice.compareTo(threshold) > 0;
    }

    public boolean isBelow(BigDecimal threshold) {
        return closePrice.compareTo(threshold) < 0;
    }

}
