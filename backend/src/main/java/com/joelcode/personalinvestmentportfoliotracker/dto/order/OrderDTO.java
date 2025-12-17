package com.joelcode.personalinvestmentportfoliotracker.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.joelcode.personalinvestmentportfoliotracker.entities.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDTO {

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("stockId")
    private UUID stockId;

    @JsonProperty("stockSymbol")
    private String stockSymbol;

    @JsonProperty("accountId")
    private UUID accountId;

    @JsonProperty("orderType")
    private Order.OrderType orderType;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("limitPrice")
    private BigDecimal limitPrice;

    @JsonProperty("status")
    private Order.OrderStatus status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("executedAt")
    private LocalDateTime executedAt;

    @JsonProperty("cancelledAt")
    private LocalDateTime cancelledAt;

    @JsonProperty("failureReason")
    private String failureReason;

    // Constructor
    public OrderDTO(UUID orderId, UUID stockId, String stockSymbol, UUID accountId,
                   Order.OrderType orderType, BigDecimal quantity, BigDecimal limitPrice,
                   Order.OrderStatus status, LocalDateTime createdAt, LocalDateTime executedAt,
                   LocalDateTime cancelledAt, String failureReason) {
        this.orderId = orderId;
        this.stockId = stockId;
        this.stockSymbol = stockSymbol;
        this.accountId = accountId;
        this.orderType = orderType;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.executedAt = executedAt;
        this.cancelledAt = cancelledAt;
        this.failureReason = failureReason;
    }

    // Getters and Setters
    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getStockId() {
        return stockId;
    }

    public void setStockId(UUID stockId) {
        this.stockId = stockId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public Order.OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(Order.OrderType orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(BigDecimal limitPrice) {
        this.limitPrice = limitPrice;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
