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

    public Earnings() {}

    public Earnings(Stock stock, LocalDate earningsDate, BigDecimal estimatedEPS, String reportTime) {
        this.stock = stock;
        this.earningsDate = earningsDate;
        this.estimatedEPS = estimatedEPS;
        this.reportTime = reportTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "earning_id")
    private UUID earningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "earnings_date", nullable = false)
    private LocalDate earningsDate;

    @Column(name = "estimated_eps", precision = 19, scale = 4)
    private BigDecimal estimatedEPS;

    @Column(name = "actual_eps", precision = 19, scale = 4)
    private BigDecimal actualEPS;

    @Column(name = "report_time", length = 20)
    private String reportTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UUID getEarningId() { return earningId; }
    public void setEarningId(UUID earningId) { this.earningId = earningId; }

    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }

    public LocalDate getEarningsDate() { return earningsDate; }
    public void setEarningsDate(LocalDate earningsDate) { this.earningsDate = earningsDate; }

    public BigDecimal getEstimatedEPS() { return estimatedEPS; }
    public void setEstimatedEPS(BigDecimal estimatedEPS) { this.estimatedEPS = estimatedEPS; }

    public BigDecimal getActualEPS() { return actualEPS; }
    public void setActualEPS(BigDecimal actualEPS) { this.actualEPS = actualEPS; }

    public String getReportTime() { return reportTime; }
    public void setReportTime(String reportTime) { this.reportTime = reportTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
