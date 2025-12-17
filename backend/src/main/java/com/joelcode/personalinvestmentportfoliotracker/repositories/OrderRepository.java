package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // By account queries
    List<Order> findByAccount_AccountId(UUID accountId);

    List<Order> findByAccount_AccountIdAndStatus(UUID accountId, Order.OrderStatus status);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId);

    Optional<Order> findByOrderId(UUID orderId);

    // Get all pending orders (for scheduler)
    List<Order> findByStatusOrderByCreatedAtAsc(Order.OrderStatus status);

    // By stock
    List<Order> findByStock_StockId(UUID stockId);

    List<Order> findByStock_StockIdAndStatus(UUID stockId, Order.OrderStatus status);
}
