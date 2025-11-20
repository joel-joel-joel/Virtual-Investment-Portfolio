package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stock")
public class Stock {

    // This entity stores information of the stock including the ticker, company and current value

    // Constructor

    public Stock(String stockCode, String companyName, BigDecimal stockValue, BigDecimal dividendPerShare) {
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
        this.dividendPerShare = dividendPerShare;
    }

    public Stock() {}

    // Key fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID stockId;

    @Column(nullable = false, unique = true, length = 20)
    private String stockCode;

    @Column (nullable = false, unique = true, length = 100)
    private String companyName;

    @Column (nullable = false)
    private BigDecimal stockValue;

    @Column (nullable = false)
    private BigDecimal dividendPerShare;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // Mapping to other entities (single stock to multiple dividends, transactions and histories,
    // multiple stocks to multiple users and accounts)

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Dividend> dividends = new ArrayList<>();

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistories = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // Getters and setters

    public UUID getStockId() {return stockId;}

    public void setStockId(UUID stockId) {this.stockId = stockId;}

    public String getStockCode() {return stockCode;}

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }

    public void setStockValue(BigDecimal stockValue) {this.stockValue = stockValue;}

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}

    public BigDecimal getDividendPerShare() {return dividendPerShare;}

    public void setDividendPerShare(BigDecimal dividendPerShare) {this.dividendPerShare = dividendPerShare;}

    // Helper Functions

    // Adding and removing key fields

    public void addDividend(Dividend dividend) {
        if (dividend != null) {
            dividends.add(dividend);
            dividend.setStock(this);
        }
    }

    public void removeDividend(Dividend dividend) {
        if (dividend != null) {
            dividends.remove(dividend);
            dividend.setStock(null);
        }
    }

    public void addPriceHistory(PriceHistory priceHistory) {
        if (priceHistory != null) {
            priceHistories.add(priceHistory);
            priceHistory.setStock(this);
        }
    }

    public void removePriceHistory(PriceHistory priceHistory) {
        if (priceHistory != null) {
            priceHistories.remove(priceHistory);
            priceHistory.setStock(null);
        }
    }

    public void addTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.add(transaction);
            transaction.setStock(this);
        }
    }

    public void removeTransaction(Transaction transaction) {
        if (transaction != null) {
            transactions.remove(transaction);
            transaction.setStock(null);
        }
    }

    // Retrieving and setting information

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Dividend> getDividends() {
        return dividends;
    }

    public void setDividends(List<Dividend> dividends) {
        this.dividends = dividends;
    }

    public List<PriceHistory> getPriceHistories() {
        return priceHistories;
    }

    public void setPriceHistories(List<PriceHistory> priceHistories) {
        this.priceHistories = priceHistories;
    }
}
