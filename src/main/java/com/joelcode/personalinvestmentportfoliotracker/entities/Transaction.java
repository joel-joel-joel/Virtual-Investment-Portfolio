package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table (name = "transactions")
public class Transaction {

    // Constructor

    public Transaction(BigDecimal shareQuantity, BigDecimal pricePerShare, BigDecimal commission, Stock stock, Account account) {
        this.stock = stock;
        this.shareQuantity = shareQuantity;
        this.pricePerShare = pricePerShare;
        this.commission = commission;
        this.account = account;
    }

    public Transaction() {}

    // Key fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID transactionId;

    @Column (nullable = false)
    private BigDecimal shareQuantity;

    @Column (nullable = false)
    private BigDecimal pricePerShare;

    @Column (nullable = false)
    private BigDecimal commission;  // For broker

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockId", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    // Transaction type

    public enum type {
        BUY,
        SELL
    }


    // Getters and setters

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public LocalDateTime getUpdatedAt() {return updatedAt;}

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getPricePerShare() {
        return pricePerShare;
    }

    public void setPricePerShare(BigDecimal pricePerShare) {
        this.pricePerShare = pricePerShare;
    }

    public BigDecimal getShareQuantity() {
        return shareQuantity;
    }

    public void setShareQuantity(BigDecimal shareQuantity) {
        this.shareQuantity = shareQuantity;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public Account getAccount() {return account;}

    public void setAccount(Account account) {this.account = account;}

    // Helper Functions

    public BigDecimal getTotalCost() {
        return pricePerShare.multiply(shareQuantity).add(commission);
    }



}
