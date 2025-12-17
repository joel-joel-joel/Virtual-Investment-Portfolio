package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.order.CreateOrderRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.order.OrderDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.order.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Profile("!test")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Create a new limit order
     * POST /api/orders
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        System.out.println("📨 OrderController: Creating new " + request.getOrderType() + " order");
        OrderDTO created = orderService.createOrder(request);
        return ResponseEntity.ok(created);
    }

    /**
     * Get a specific order by ID
     * GET /api/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID orderId) {
        System.out.println("📨 OrderController: Getting order " + orderId);
        OrderDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    /**
     * Get all orders for an account (all statuses)
     * GET /api/orders/account/{accountId}
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<OrderDTO>> getAllOrdersForAccount(@PathVariable UUID accountId) {
        System.out.println("📨 OrderController: Getting all orders for account " + accountId);
        List<OrderDTO> orders = orderService.getAllOrdersForAccount(accountId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all pending orders for an account
     * GET /api/orders/account/{accountId}/pending
     */
    @GetMapping("/account/{accountId}/pending")
    public ResponseEntity<List<OrderDTO>> getPendingOrders(@PathVariable UUID accountId) {
        System.out.println("📨 OrderController: Getting pending orders for account " + accountId);
        List<OrderDTO> orders = orderService.getPendingOrdersForAccount(accountId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel a pending order
     * DELETE /api/orders/{orderId}
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable UUID orderId) {
        System.out.println("📨 OrderController: Cancelling order " + orderId);
        OrderDTO cancelled = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelled);
    }
}
