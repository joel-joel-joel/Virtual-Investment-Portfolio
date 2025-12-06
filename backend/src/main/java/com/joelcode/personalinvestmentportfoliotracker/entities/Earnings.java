package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "earnings")
public class Earnings {

    // This entity stores upcoming earnings calendar data
    // Can have estimated and actual EPS (actual is null until earnings are reported)

    // Constructors
    public Earnings(UUID earningsId, Stock stock, LocalDate earningsDate, String reportTime,
                   BigDecimal estimatedEPS, BigDecimal actualEPS) {
        this.earningsId = earningsId;
        this.stock = stock;
        this.earningsDate = earningsDate;
        this.reportTime = reportTime;
        this.estimatedEPS = estimatedEPS;
        this.actualEPS = actualEPS;
    }

    public Earnings() {}


    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID earningsId;

    @Column(nullable = false)
    private LocalDate earningsDate;

    @Column(length = 20)
    private String reportTime; // e.g., "BMO" (Before Market Open), "AMC" (After Market Close), "TNS" (Time Not Supplied)

    @Column(precision = 19, scale = 4)
    private BigDecimal estimatedEPS;

    @Column(precision = 19, scale = 4)
    private BigDecimal actualEPS; // null until earnings are reported

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;


    // Getters and Setters
    public UUID getEarningsId() {
        return earningsId;
    }

    public void setEarningsId(UUID earningsId) {
        this.earningsId = earningsId;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public LocalDate getEarningsDate() {
        return earningsDate;
    }

    public void setEarningsDate(LocalDate earningsDate) {
        this.earningsDate = earningsDate;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public BigDecimal getEstimatedEPS() {
        return estimatedEPS;
    }

    public void setEstimatedEPS(BigDecimal estimatedEPS) {
        this.estimatedEPS = estimatedEPS;
    }

    public BigDecimal getActualEPS() {
        return actualEPS;
    }

    public void setActualEPS(BigDecimal actualEPS) {
        this.actualEPS = actualEPS;
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
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic helpers
    public boolean hasReported() {
        return this.actualEPS != null;
    }

    public BigDecimal getEPSSurprise() {
        if (actualEPS == null || estimatedEPS == null) {
            return null;
        }
        return actualEPS.subtract(estimatedEPS);
    }

    public BigDecimal getEPSSurprisePercent() {
        if (actualEPS == null || estimatedEPS == null || estimatedEPS.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return getEPSSurprise()
                .divide(estimatedEPS.abs(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
