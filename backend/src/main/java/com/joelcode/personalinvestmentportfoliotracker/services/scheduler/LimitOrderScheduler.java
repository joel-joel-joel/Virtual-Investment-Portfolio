package com.joelcode.personalinvestmentportfoliotracker.services.scheduler;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Order;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.services.order.OrderService;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import com.joelcode.personalinvestmentportfoliotracker.services.transaction.TransactionProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("!test")
public class LimitOrderScheduler {

    private final OrderService orderService;
    private final PriceHistoryService priceHistoryService;
    private final TransactionProcessorService transactionProcessorService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public LimitOrderScheduler(
            OrderService orderService,
            PriceHistoryService priceHistoryService,
            TransactionProcessorService transactionProcessorService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.orderService = orderService;
        this.priceHistoryService = priceHistoryService;
        this.transactionProcessorService = transactionProcessorService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Check and execute limit orders every minute
     * Cron: "0 * * * * *" means: at 0 seconds, every minute
     */
    @Scheduled(cron = "0 * * * * *")
    public void checkAndExecuteLimitOrders() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("⏰ LimitOrderScheduler: Checking for executable orders at " + LocalDateTime.now());

        try {
            List<Order> pendingOrders = orderService.getAllPendingOrders();
            System.out.println("📊 Found " + pendingOrders.size() + " pending orders");

            int executedCount = 0;
            int failedCount = 0;

            for (Order order : pendingOrders) {
                try {
                    BigDecimal currentPrice = priceHistoryService.getCurrentPrice(order.getStock().getStockId());

                    boolean shouldExecute = false;
                    String reason = "";

                    // BUY_LIMIT executes when current price <= limit price
                    if (order.getOrderType() == Order.OrderType.BUY_LIMIT) {
                        if (currentPrice.compareTo(order.getLimitPrice()) <= 0) {
                            shouldExecute = true;
                            reason = "Current price " + currentPrice + " <= Limit price " + order.getLimitPrice();
                        }
                    }
                    // SELL_LIMIT executes when current price >= limit price
                    else if (order.getOrderType() == Order.OrderType.SELL_LIMIT) {
                        if (currentPrice.compareTo(order.getLimitPrice()) >= 0) {
                            shouldExecute = true;
                            reason = "Current price " + currentPrice + " >= Limit price " + order.getLimitPrice();
                        }
                    }

                    if (shouldExecute) {
                        System.out.println("🎯 Order " + order.getOrderId() + " (" + order.getOrderType() + "): " + reason);
                        executeLimitOrder(order, currentPrice);
                        executedCount++;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Error processing order " + order.getOrderId() + ": " + e.getMessage());
                    orderService.markOrderAsFailed(order.getOrderId(), e.getMessage());
                    failedCount++;
                }
            }

            System.out.println("✅ Scheduler completed - Executed: " + executedCount + ", Failed: " + failedCount);
            System.out.println("=".repeat(70) + "\n");

        } catch (Exception e) {
            System.out.println("❌ LimitOrderScheduler error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Execute a limit order by creating a transaction
     */
    private void executeLimitOrder(Order order, BigDecimal executionPrice) {
        System.out.println("  Processing order: " + order.getStock().getStockId() + " - " + order.getOrderType() + " " + order.getQuantity() + " @ " + executionPrice);

        // Create transaction request
        TransactionCreateRequest request = new TransactionCreateRequest();
        request.setStockId(order.getStock().getStockId());
        request.setAccountId(order.getAccount().getAccountId());
        request.setShareQuantity(order.getQuantity());
        request.setPricePerShare(executionPrice);

        // Convert order type to transaction type
        if (order.getOrderType() == Order.OrderType.BUY_LIMIT) {
            request.setTransactionType(Transaction.TransactionType.BUY);
        } else {
            request.setTransactionType(Transaction.TransactionType.SELL);
        }

        try {
            // Execute transaction
            TransactionDTO transaction = transactionProcessorService.processTransaction(request);

            // Mark order as executed
            orderService.markOrderAsExecuted(order.getOrderId(), transaction.getTransactionId());

            // Send WebSocket notification to user
            OrderExecutionMessage message = new OrderExecutionMessage(
                    order.getOrderId(),
                    order.getAccount().getAccountId(),
                    order.getStock().getStockCode(),
                    order.getOrderType(),
                    order.getQuantity(),
                    executionPrice,
                    LocalDateTime.now()
            );

            messagingTemplate.convertAndSend(
                    "/topic/orders/" + order.getAccount().getAccountId(),
                    message
            );

            System.out.println("  ✅ Order executed successfully");

        } catch (Exception e) {
            System.out.println("  ⚠️ Order execution failed: " + e.getMessage());
            orderService.markOrderAsFailed(order.getOrderId(), e.getMessage());
            throw new RuntimeException("Failed to execute order: " + e.getMessage(), e);
        }
    }

    /**
     * WebSocket message sent when an order is executed
     */
    public static class OrderExecutionMessage {
        public String orderId;
        public String accountId;
        public String stockSymbol;
        public String orderType;
        public BigDecimal quantity;
        public BigDecimal executionPrice;
        public LocalDateTime executedAt;

        public OrderExecutionMessage(
                Object orderId,
                Object accountId,
                String stockSymbol,
                Order.OrderType orderType,
                BigDecimal quantity,
                BigDecimal executionPrice,
                LocalDateTime executedAt
        ) {
            this.orderId = orderId != null ? orderId.toString() : null;
            this.accountId = accountId != null ? accountId.toString() : null;
            this.stockSymbol = stockSymbol;
            this.orderType = orderType != null ? orderType.toString() : null;
            this.quantity = quantity;
            this.executionPrice = executionPrice;
            this.executedAt = executedAt;
        }
    }
}
