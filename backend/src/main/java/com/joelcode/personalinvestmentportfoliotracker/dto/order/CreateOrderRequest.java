package com.joelcode.personalinvestmentportfoliotracker.dto.order;

import com.joelcode.personalinvestmentportfoliotracker.entities.Order;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateOrderRequest {

    @NotNull(message = "Stock ID is required")
    private UUID stockId;

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotNull(message = "Limit price is required")
    @Positive(message = "Limit price must be positive")
    private BigDecimal limitPrice;

    @NotNull(message = "Order type is required")
    private Order.OrderType orderType;

    // Constructor
    public CreateOrderRequest() {}

    public CreateOrderRequest(UUID stockId, UUID accountId, BigDecimal quantity, BigDecimal limitPrice, Order.OrderType orderType) {
        this.stockId = stockId;
        this.accountId = accountId;
        this.quantity = quantity;
        this.limitPrice = limitPrice;
        this.orderType = orderType;
    }

    // Getters and Setters
    public UUID getStockId() {
        return stockId;
    }

    public void setStockId(UUID stockId) {
        this.stockId = stockId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
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

    public Order.OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(Order.OrderType orderType) {
        this.orderType = orderType;
    }
}
