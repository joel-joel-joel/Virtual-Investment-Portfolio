package com.joelcode.personalinvestmentportfoliotracker.services.order;

import com.joelcode.personalinvestmentportfoliotracker.dto.order.CreateOrderRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.order.OrderDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Order;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.OrderRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            AccountRepository accountRepository,
            StockRepository stockRepository
    ) {
        this.orderRepository = orderRepository;
        this.accountRepository = accountRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    @Transactional(readOnly = false)
    public OrderDTO createOrder(CreateOrderRequest request) {
        // Validate account exists
        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Validate stock exists
        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        // Validate quantity and limit price
        if (request.getQuantity() == null || request.getQuantity().signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.getLimitPrice() == null || request.getLimitPrice().signum() <= 0) {
            throw new IllegalArgumentException("Limit price must be positive");
        }

        // Create order entity
        Order order = new Order(account, stock, request.getOrderType(), request.getQuantity(), request.getLimitPrice());

        // Save order
        Order savedOrder = orderRepository.save(order);

        System.out.println("✅ Order created: " + savedOrder.getOrderId() + " - " + request.getOrderType() + " " + request.getQuantity() + " @ " + request.getLimitPrice());

        return OrderMapper.toDTO(savedOrder, stock);
    }

    @Override
    public OrderDTO getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        return OrderMapper.toDTO(order, order.getStock());
    }

    @Override
    public List<OrderDTO> getAllOrdersForAccount(UUID accountId) {
        List<Order> orders = orderRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId);

        return orders.stream()
                .map(order -> OrderMapper.toDTO(order, order.getStock()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getPendingOrdersForAccount(UUID accountId) {
        List<Order> orders = orderRepository.findByAccount_AccountIdAndStatus(accountId, Order.OrderStatus.PENDING);

        return orders.stream()
                .map(order -> OrderMapper.toDTO(order, order.getStock()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = false)
    public OrderDTO cancelOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        System.out.println("✅ Order cancelled: " + orderId);

        return OrderMapper.toDTO(updatedOrder, updatedOrder.getStock());
    }

    @Override
    public List<Order> getAllPendingOrders() {
        return orderRepository.findByStatusOrderByCreatedAtAsc(Order.OrderStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = false)
    public void markOrderAsExecuted(UUID orderId, UUID transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(Order.OrderStatus.EXECUTED);
        order.setExecutedAt(LocalDateTime.now());

        orderRepository.save(order);

        System.out.println("✅ Order executed: " + orderId + " - Transaction: " + transactionId);
    }

    @Override
    @Transactional(readOnly = false)
    public void markOrderAsFailed(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(Order.OrderStatus.FAILED);
        order.setFailureReason(reason);

        orderRepository.save(order);

        System.out.println("❌ Order failed: " + orderId + " - Reason: " + reason);
    }
}
