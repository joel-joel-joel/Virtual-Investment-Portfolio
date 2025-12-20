package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dividends")
public class Dividend {

    // This entity represents a dividend announcement at the STOCK level
    // It does NOT track which accounts received it - that's DividendPayment's job


    // Constructors
    public Dividend(BigDecimal dividendPerShare, LocalDateTime payDate, Stock stock) {
        this.stock = stock;
        this.dividendPerShare = dividendPerShare;
        this.payDate = payDate;
        this.announcementDate = LocalDateTime.now();
    }

    public Dividend() {}


    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "dividend_id")
    private UUID dividendId;

    @Column(name = "dividend_per_share", nullable = false)
    private BigDecimal dividendPerShare;

    @Column(name = "pay_date", nullable = false)
    private LocalDateTime payDate;

    @Column(name = "announcement_date", nullable = false)
    private LocalDateTime announcementDate = LocalDateTime.now();


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // One dividend can have many payments to different accounts
    @OneToMany(mappedBy = "dividend", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DividendPayment> payments = new ArrayList<>();


    // Getters and Setters
    public UUID getDividendId() {return dividendId;}

    public void setDividendId(UUID dividendId) {this.dividendId = dividendId;}

    public BigDecimal getDividendAmountPerShare() {return dividendPerShare;}

    public void setDividendAmountPerShare(BigDecimal dividendPerShare) {this.dividendPerShare = dividendPerShare;}

    public LocalDateTime getPayDate() {return payDate;}

    public void setPayDate(LocalDateTime payDate) {this.payDate = payDate;}

    public LocalDateTime getAnnouncementDate() {return announcementDate;}

    public void setAnnouncementDate(LocalDateTime announcementDate) {this.announcementDate = announcementDate;}

    public Stock getStock() {return stock;}

    public void setStock(Stock stock) {this.stock = stock;}

    public UUID getStockId() {return stock != null ? stock.getStockId() : null;}

    public List<DividendPayment> getPayments() {return payments;}

    public void setPayments(List<DividendPayment> payments) {this.payments = payments;}
}