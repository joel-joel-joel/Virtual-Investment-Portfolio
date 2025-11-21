package com.joelcode.personalinvestmentportfoliotracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

// WebSocket controller for real-time portfolio updates
//Client sends to: /app/portfolio/subscribe
//Server broadcasts to: /topic/portfolio/{accountId}
@Controller
public class WebSocketController {

    @Autowired
    public SimpMessagingTemplate messagingTemplate;

     // Handle portfolio subscription requests from client. When client sends message to /app/portfolio/subscribe,
     //Server broadcasts response to /topic/portfolio/updates

    @MessageMapping("/portfolio/subscribe")
    @SendTo("/topic/portfolio/updates")
    public PortfolioUpdateMessage subscribeToPortfolio(PortfolioSubscriptionRequest request) {
        return new PortfolioUpdateMessage(
                request.getAccountId(),
                "Subscribed to portfolio updates",
                LocalDateTime.now()
        );
    }

    // Send real-time portfolio value update to specific account. Called from service layer when portfolio value changes
    // via /topic/portfolio/{accountId}
    public void sendPortfolioUpdate(UUID accountId, BigDecimal newValue, BigDecimal change) {
        PortfolioUpdateMessage message = new PortfolioUpdateMessage(
                accountId,
                newValue,
                change,
                LocalDateTime.now()
        );

        // Send to specific account topic
        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + accountId,
                message
        );
    }
    // Broadcast stock price update to all subscribers, called when stock prices are updated
    public void broadcastStockPriceUpdate(UUID stockId, String stockCode, BigDecimal newPrice) {
        StockPriceUpdateMessage message = new StockPriceUpdateMessage(
                stockId,
                stockCode,
                newPrice,
                LocalDateTime.now()
        );

        // Broadcast to all subscribers on this topic
        messagingTemplate.convertAndSend("/topic/stocks/prices", message);
    }

    //Send user-specific notification. Only the specific user will receive this message
    public void sendUserNotification(String username, String notificationMessage) {
        UserNotification notification = new UserNotification(
                notificationMessage,
                LocalDateTime.now()
        );

        // Send to specific user's queue
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
        );
    }

    // Message dtos

    public static class PortfolioSubscriptionRequest {
        private UUID accountId;

        public PortfolioSubscriptionRequest() {}

        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
    }

    public static class PortfolioUpdateMessage {
        private UUID accountId;
        private String message;
        private BigDecimal value;
        private BigDecimal change;
        private LocalDateTime timestamp;

        public PortfolioUpdateMessage(UUID accountId, String message, LocalDateTime timestamp) {
            this.accountId = accountId;
            this.message = message;
            this.timestamp = timestamp;
        }

        public PortfolioUpdateMessage(UUID accountId, BigDecimal value, BigDecimal change, LocalDateTime timestamp) {
            this.accountId = accountId;
            this.value = value;
            this.change = change;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public UUID getAccountId() { return accountId; }
        public void setAccountId(UUID accountId) { this.accountId = accountId; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public BigDecimal getChange() { return change; }
        public void setChange(BigDecimal change) { this.change = change; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class StockPriceUpdateMessage {
        private UUID stockId;
        private String stockCode;
        private BigDecimal price;
        private LocalDateTime timestamp;

        public StockPriceUpdateMessage(UUID stockId, String stockCode, BigDecimal price, LocalDateTime timestamp) {
            this.stockId = stockId;
            this.stockCode = stockCode;
            this.price = price;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public UUID getStockId() { return stockId; }
        public void setStockId(UUID stockId) { this.stockId = stockId; }
        public String getStockCode() { return stockCode; }
        public void setStockCode(String stockCode) { this.stockCode = stockCode; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class UserNotification {
        private String message;
        private LocalDateTime timestamp;

        public UserNotification(String message, LocalDateTime timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class HoldingUpdateMessage {
        public UUID accountId;
        public UUID stockId;
        public BigDecimal quantity;
        public BigDecimal totalCostBasis;
        public BigDecimal averageCostBasis;
        public BigDecimal realizedGain;
        public LocalDateTime timestamp;

        public HoldingUpdateMessage(UUID accountId, UUID stockId, BigDecimal quantity,
                                    BigDecimal totalCostBasis, BigDecimal averageCostBasis,
                                    BigDecimal realizedGain, LocalDateTime timestamp) {
            this.accountId = accountId;
            this.stockId = stockId;
            this.quantity = quantity;
            this.totalCostBasis = totalCostBasis;
            this.averageCostBasis = averageCostBasis;
            this.realizedGain = realizedGain;
            this.timestamp = timestamp;
        }
    }

}