package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table (name = "dividends")
public class Dividend {

    // This entity represents the dividend payout for a particular stock

    // Constructor

    public Dividend(BigDecimal amountPerShare, LocalDateTime payDate, Stock stock) {
        this.stock  = stock;
        this.amountPerShare = amountPerShare;
        this.payDate = payDate;
    }

    public Dividend() {}

    // Key fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID dividendId;

    @Column (nullable = false)
    private BigDecimal amountPerShare;

    @Column (nullable = false)
    private LocalDateTime payDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockId", nullable = false)
    private Stock stock;


    // Getters and setters

    public UUID getDividendId() {
        return dividendId;
    }

    public void setDividendId(UUID dividendId) {
        this.dividendId = dividendId;
    }

    public BigDecimal getAmountPerShare() {
        return amountPerShare;
    }

    public void setAmountPerShare(BigDecimal amountPerShare) {
        this.amountPerShare = amountPerShare;
    }

    public LocalDateTime getPayDate() {
        return payDate;
    }

    public void setPayDate(LocalDateTime payDate) {
        this.payDate = payDate;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    // Helper functions
    public BigDecimal getTotalDividend(BigDecimal sharesOwned) {
        return amountPerShare.multiply(sharesOwned);
    }

}
