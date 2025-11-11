package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock")
public class Stock {

    // Constructor

    public Stock(Long stockId, String stockCode, String companyName, Double stockValue) {
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
    }

    public Stock() {}

    // Defining key fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stockId;

    @Column(nullable = false, unique = true, length = 20)
    private String stockCode;

    @Column (nullable = false, length = 100)
    private String companyName;

    @Column (nullable = false)
    private Double stockValue;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // Mapping to other entities (single stock to multiple dividends, transactions and histories)

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Transactions> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Dividends> dividends = new ArrayList<>();

    @OneToMany(mappedBy = "stock", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PriceHistory> priceHistories = new ArrayList<>();



    // Getters and setters

    public Long getStockId() {return stockId;}

    public void setStockId(Long stockId) {this.stockId = stockId;}

    public String getStockCode() {return stockCode;}

    public void setStockCode(Long stockCode) {
        this.stockCode = stockCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Double getStockValue() {
        return stockValue;
    }

    public void setStockValue(Double stockValue) {
        this.stockValue = stockValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
