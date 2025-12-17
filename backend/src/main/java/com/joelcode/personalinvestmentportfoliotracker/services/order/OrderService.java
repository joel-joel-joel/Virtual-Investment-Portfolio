package com.joelcode.personalinvestmentportfoliotracker.services.order;

import com.joelcode.personalinvestmentportfoliotracker.dto.order.CreateOrderRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.order.OrderDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    /**
     * Create a new limit order
     */
    OrderDTO createOrder(CreateOrderRequest request);

    /**
     * Get a specific order by ID
     */
    OrderDTO getOrderById(UUID orderId);

    /**
     * Get all orders for an account (all statuses)
     */
    List<OrderDTO> getAllOrdersForAccount(UUID accountId);

    /**
     * Get all pending orders for an account
     */
    List<OrderDTO> getPendingOrdersForAccount(UUID accountId);

    /**
     * Cancel a pending order
     */
    OrderDTO cancelOrder(UUID orderId);

    /**
     * Get all pending orders across all accounts (for scheduler)
     */
    List<Order> getAllPendingOrders();

    /**
     * Mark an order as executed after transaction is created
     */
    void markOrderAsExecuted(UUID orderId, UUID transactionId);

    /**
     * Mark an order as failed due to insufficient balance/shares
     */
    void markOrderAsFailed(UUID orderId, String reason);
}
