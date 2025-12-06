package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
public class Activity {

    // This entity tracks user actions (buy, sell, dividend, etc.)
    // Activities are created automatically when users perform actions

    // Constructors
    public Activity(UUID activityId, User user, Stock stock, ActivityType activityType,
                   String description, BigDecimal amount, LocalDateTime timestamp) {
        this.activityId = activityId;
        this.user = user;
        this.stock = stock;
        this.activityType = activityType;
        this.description = description;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public Activity() {}


    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID activityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(length = 500)
    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public enum ActivityType {
        BUY,
        SELL,
        DIVIDEND,
        DEPOSIT,
        WITHDRAWAL,
        WATCHLIST_ADD,
        WATCHLIST_REMOVE,
        ALERT_TRIGGERED
    }


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;


    // Getters and Setters
    public UUID getActivityId() {
        return activityId;
    }

    public void setActivityId(UUID activityId) {
        this.activityId = activityId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }


    // Helper Functions
    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }

        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
    }
}
