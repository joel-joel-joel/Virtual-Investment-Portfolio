package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_alerts")
public class PriceAlert {

    // This entity stores price alert thresholds for stocks
    // Users can have multiple alerts, and we track whether each alert has been triggered

    // Constructors
    public PriceAlert(UUID priceAlertId, User user, Stock stock, BigDecimal targetPrice,
                     AlertType alertType, Boolean isTriggered, LocalDateTime triggeredAt) {
        this.priceAlertId = priceAlertId;
        this.user = user;
        this.stock = stock;
        this.targetPrice = targetPrice;
        this.alertType = alertType;
        this.isTriggered = isTriggered;
        this.triggeredAt = triggeredAt;
    }

    public PriceAlert() {}


    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID priceAlertId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal targetPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Column(nullable = false)
    private Boolean isTriggered = false;

    private LocalDateTime triggeredAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AlertType {
        ABOVE,  // Alert when price goes above target
        BELOW   // Alert when price goes below target
    }


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;


    // Getters and Setters
    public UUID getPriceAlertId() {
        return priceAlertId;
    }

    public void setPriceAlertId(UUID priceAlertId) {
        this.priceAlertId = priceAlertId;
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

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Boolean getIsTriggered() {
        return isTriggered;
    }

    public void setIsTriggered(Boolean isTriggered) {
        this.isTriggered = isTriggered;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    // Helper Functions
    @PrePersist
    public void prePersist() {
        if (this.isTriggered == null) {
            this.isTriggered = false;
        }

        if (this.targetPrice == null) {
            this.targetPrice = BigDecimal.ZERO;
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic helper to trigger the alert
    public void triggerAlert() {
        this.isTriggered = true;
        this.triggeredAt = LocalDateTime.now();
    }
}
