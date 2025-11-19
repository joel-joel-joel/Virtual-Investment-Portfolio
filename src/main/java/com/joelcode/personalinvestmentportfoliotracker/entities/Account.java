package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    // This entity represents the investment account for the user, storing the account balance and portofolio.
    // A user can have multiple accounts

    // Constructor

    public Account(UUID accountId, String accountName, BigDecimal accountBalance, User user) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.accountBalance = accountBalance;
        this.user = user;
    }

    public Account() {}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID accountId;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holding> holdings = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioSnapshot> snapshots = new ArrayList<>();


    // Getters and Setters

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {this.user = user;}

    public void getUserid(){this.user.getUserId();}

    public void setUserId(UUID userId){
        this.user.setUserId(userId);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    // Helper Methods
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.setAccount(this);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
        transaction.setAccount(null);
    }
}
