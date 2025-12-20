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

    // This entity stores the transaction of an account, inclusive of information regarding how many shares are bought
    // and for how much

    // Constructors
    public Transaction(BigDecimal shareQuantity, BigDecimal pricePerShare, BigDecimal commission, Stock stock, Account account) {
        this.stock = stock;
        this.shareQuantity = shareQuantity;
        this.pricePerShare = pricePerShare;
        this.commission = commission;
        this.account = account;
    }

    public Transaction() {}


    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "share_quantity", nullable = false)
    private BigDecimal shareQuantity;

    @Column(name = "price_per_share", nullable = false)
    private BigDecimal pricePerShare;

    @Column (nullable = false)
    private BigDecimal commission;  // For broker

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransactionType {
        BUY,
        SELL,
        BUY_LIMIT,
        SELL_LIMIT
    }


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;


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

    public void setStockId(UUID stockId) {this.stock.setStockId(stockId);}

    public void setAccountId(UUID accountId) {
        if (this.account == null) {
            this.account = new Account();
        }
        this.account.setAccountId(accountId);
    }

    public TransactionType getTransactionType() {return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {this.transactionType = transactionType;}


    // Helper Functions
    public BigDecimal getTotalCost() {
        return pricePerShare.multiply(shareQuantity).add(commission);
    }



}
