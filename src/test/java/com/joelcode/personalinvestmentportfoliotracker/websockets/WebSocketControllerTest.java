package com.joelcode.personalinvestmentportfoliotracker.websockets;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketController webSocketController;


    @BeforeEach
    void setUp() {
        webSocketController = new WebSocketController();
        webSocketController.messagingTemplate = messagingTemplate;
    }

    @Test
    void testSubscribeToPortfolio_Success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        WebSocketController.PortfolioSubscriptionRequest request = new WebSocketController.PortfolioSubscriptionRequest();
        request.setAccountId(accountId);

        // Act
        WebSocketController.PortfolioUpdateMessage response = webSocketController.subscribeToPortfolio(request);

        // Assert
        assertNotNull(response);
        assertEquals(accountId, response.getAccountId());
        assertEquals("Subscribed to portfolio updates", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSubscribeToPortfolio_TimestampIsSet() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        WebSocketController.PortfolioSubscriptionRequest request = new WebSocketController.PortfolioSubscriptionRequest();
        request.setAccountId(accountId);
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        WebSocketController.PortfolioUpdateMessage response = webSocketController.subscribeToPortfolio(request);
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isAfter(beforeCall.minusSeconds(1)));
        assertTrue(response.getTimestamp().isBefore(afterCall.plusSeconds(1)));
    }

    @Test
    void testSendPortfolioUpdate_ConvertAndSend() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        BigDecimal newValue = BigDecimal.valueOf(100000.0);
        BigDecimal change = BigDecimal.valueOf(5000.0);

        // Act
        webSocketController.sendPortfolioUpdate(accountId, newValue, change);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<WebSocketController.PortfolioUpdateMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketController.PortfolioUpdateMessage.class);

        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        String destination = destinationCaptor.getValue();
        WebSocketController.PortfolioUpdateMessage message = messageCaptor.getValue();

        assertEquals("/topic/portfolio/" + accountId, destination);
        assertEquals(accountId, message.getAccountId());
        assertEquals(newValue, message.getValue());
        assertEquals(change, message.getChange());
    }

    @Test
    void testSendPortfolioUpdate_CorrectTopic() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        BigDecimal newValue = BigDecimal.valueOf(75000.0);
        BigDecimal change = BigDecimal.valueOf(-2500.0);

        // Act
        webSocketController.sendPortfolioUpdate(accountId, newValue, change);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), (Object) any());

        String destination = destinationCaptor.getValue();
        assertTrue(destination.startsWith("/topic/portfolio/"));
        assertTrue(destination.contains(accountId.toString()));
    }

    @Test
    void testSendPortfolioUpdate_MultipleAccounts() {
        // Arrange
        UUID account1 = UUID.randomUUID();
        UUID account2 = UUID.randomUUID();
        BigDecimal value1 = BigDecimal.valueOf(100000.0);
        BigDecimal change1 = BigDecimal.valueOf(5000.0);
        BigDecimal value2 = BigDecimal.valueOf(80000.0);
        BigDecimal change2 = BigDecimal.valueOf(-3000.0);

        // Act
        webSocketController.sendPortfolioUpdate(account1, value1, change1);
        webSocketController.sendPortfolioUpdate(account2, value2, change2);

        // Assert
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(WebSocketController.PortfolioUpdateMessage.class));

        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(2)).convertAndSend(destinationCaptor.capture(), (Object) any());

        java.util.List<String> destinations = destinationCaptor.getAllValues();
        assertTrue(destinations.get(0).contains(account1.toString()));
        assertTrue(destinations.get(1).contains(account2.toString()));
    }

    @Test
    void testBroadcastStockPriceUpdate_Success() {
        // Arrange
        UUID stockId = UUID.randomUUID();
        String stockCode = "AAPL";
        BigDecimal newPrice = BigDecimal.valueOf(150.0);

        // Act
        webSocketController.broadcastStockPriceUpdate(stockId, stockCode, newPrice);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<WebSocketController.StockPriceUpdateMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketController.StockPriceUpdateMessage.class);

        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        String destination = destinationCaptor.getValue();
        WebSocketController.StockPriceUpdateMessage message = messageCaptor.getValue();

        assertEquals("/topic/stocks/prices", destination);
        assertEquals(stockId, message.getStockId());
        assertEquals(stockCode, message.getStockCode());
        assertEquals(newPrice, message.getPrice());
    }

    @Test
    void testBroadcastStockPriceUpdate_BroadcastDestination() {
        // Arrange
        UUID stockId = UUID.randomUUID();
        String stockCode = "MSFT";
        BigDecimal newPrice = BigDecimal.valueOf(300.0);

        // Act
        webSocketController.broadcastStockPriceUpdate(stockId, stockCode, newPrice);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSend(destinationCaptor.capture(), (Object) any());

        assertEquals("/topic/stocks/prices", destinationCaptor.getValue());
    }

    @Test
    void testBroadcastStockPriceUpdate_MultiplePriceUpdates() {
        // Arrange
        UUID stock1 = UUID.randomUUID();
        UUID stock2 = UUID.randomUUID();

        // Act
        webSocketController.broadcastStockPriceUpdate(stock1, "AAPL", BigDecimal.valueOf(155.0));
        webSocketController.broadcastStockPriceUpdate(stock2, "GOOGL", BigDecimal.valueOf(2850.0));

        // Assert
        verify(messagingTemplate, times(2)).convertAndSend(
                eq("/topic/stocks/prices"),
                any(WebSocketController.StockPriceUpdateMessage.class)
        );
    }

    @Test
    void testSendUserNotification_Success() {
        // Arrange
        String username = "trader";
        String notificationMessage = "New dividend payment received";

        // Act
        webSocketController.sendUserNotification(username, notificationMessage);

        // Assert
        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<WebSocketController.UserNotification> messageCaptor = ArgumentCaptor.forClass(WebSocketController.UserNotification.class);

        verify(messagingTemplate, times(1)).convertAndSendToUser(userCaptor.capture(), destinationCaptor.capture(), messageCaptor.capture());

        assertEquals(username, userCaptor.getValue());
        assertEquals("/queue/notifications", destinationCaptor.getValue());
        assertEquals(notificationMessage, messageCaptor.getValue().getMessage());
    }

    @Test
    void testSendUserNotification_UserSpecificQueue() {
        // Arrange
        String username = "investor";
        String message = "Portfolio value updated";

        // Act
        webSocketController.sendUserNotification(username, message);

        // Assert
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate).convertAndSendToUser(anyString(), destinationCaptor.capture(), any());

        assertEquals("/queue/notifications", destinationCaptor.getValue());
    }

    @Test
    void testSendUserNotification_MultipleUsers() {
        // Arrange
        String user1 = "alice";
        String user2 = "bob";
        String message = "Stock price alert";

        // Act
        webSocketController.sendUserNotification(user1, message);
        webSocketController.sendUserNotification(user2, message);

        // Assert
        verify(messagingTemplate, times(2)).convertAndSendToUser(anyString(), anyString(), any(WebSocketController.UserNotification.class));

        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        verify(messagingTemplate, times(2)).convertAndSendToUser(userCaptor.capture(), anyString(), any());

        java.util.List<String> users = userCaptor.getAllValues();
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    void testPortfolioUpdateMessage_ConstructorWithStringMessage() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        String message = "Portfolio updated";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                accountId,
                message,
                timestamp
        );

        // Assert
        assertEquals(accountId, updateMessage.getAccountId());
        assertEquals(message, updateMessage.getMessage());
        assertEquals(timestamp, updateMessage.getTimestamp());
        assertNull(updateMessage.getValue());
        assertNull(updateMessage.getChange());
    }

    @Test
    void testPortfolioUpdateMessage_ConstructorWithValues() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        BigDecimal value = BigDecimal.valueOf(95000.0);
        BigDecimal change = BigDecimal.valueOf(2000.0);
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                accountId,
                value,
                change,
                timestamp
        );

        // Assert
        assertEquals(accountId, updateMessage.getAccountId());
        assertEquals(value, updateMessage.getValue());
        assertEquals(change, updateMessage.getChange());
        assertEquals(timestamp, updateMessage.getTimestamp());
        assertNull(updateMessage.getMessage());
    }

    @Test
    void testStockPriceUpdateMessage_AllFieldsSet() {
        // Arrange
        UUID stockId = UUID.randomUUID();
        String stockCode = "GOOGL";
        BigDecimal price = BigDecimal.valueOf(2800.0);
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        WebSocketController.StockPriceUpdateMessage priceMessage = new WebSocketController.StockPriceUpdateMessage(
                stockId,
                stockCode,
                price,
                timestamp
        );

        // Assert
        assertEquals(stockId, priceMessage.getStockId());
        assertEquals(stockCode, priceMessage.getStockCode());
        assertEquals(price, priceMessage.getPrice());
        assertEquals(timestamp, priceMessage.getTimestamp());
    }

    @Test
    void testUserNotification_AllFieldsSet() {
        // Arrange
        String message = "Transaction completed";
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        WebSocketController.UserNotification notification = new WebSocketController.UserNotification(
                message,
                timestamp
        );

        // Assert
        assertEquals(message, notification.getMessage());
        assertEquals(timestamp, notification.getTimestamp());
    }

    @Test
    void testHoldingUpdateMessage_AllFieldsSet() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        UUID stockId = UUID.randomUUID();
        BigDecimal quantity = BigDecimal.valueOf(100);
        BigDecimal totalCostBasis = BigDecimal.valueOf(15000.0);
        BigDecimal averageCostBasis = BigDecimal.valueOf(150.0);
        BigDecimal realizedGain = BigDecimal.valueOf(500.0);
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        WebSocketController.HoldingUpdateMessage holdingMessage = new WebSocketController.HoldingUpdateMessage(
                accountId,
                stockId,
                quantity,
                totalCostBasis,
                averageCostBasis,
                realizedGain,
                timestamp
        );

        // Assert
        assertEquals(accountId, holdingMessage.accountId);
        assertEquals(stockId, holdingMessage.stockId);
        assertEquals(quantity, holdingMessage.quantity);
        assertEquals(totalCostBasis, holdingMessage.totalCostBasis);
        assertEquals(averageCostBasis, holdingMessage.averageCostBasis);
        assertEquals(realizedGain, holdingMessage.realizedGain);
        assertEquals(timestamp, holdingMessage.timestamp);
    }

    @Test
    void testPortfolioSubscriptionRequest_GettersSetters() {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act
        WebSocketController.PortfolioSubscriptionRequest request = new WebSocketController.PortfolioSubscriptionRequest();
        request.setAccountId(accountId);

        // Assert
        assertEquals(accountId, request.getAccountId());
    }

    @Test
    void testSendPortfolioUpdate_NegativeChange() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        BigDecimal newValue = BigDecimal.valueOf(95000.0);
        BigDecimal change = BigDecimal.valueOf(-5000.0); // Loss

        // Act
        webSocketController.sendPortfolioUpdate(accountId, newValue, change);

        // Assert
        ArgumentCaptor<WebSocketController.PortfolioUpdateMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketController.PortfolioUpdateMessage.class);
        verify(messagingTemplate).convertAndSend(anyString(), messageCaptor.capture());

        WebSocketController.PortfolioUpdateMessage message = messageCaptor.getValue();
        assertEquals(BigDecimal.valueOf(-5000.0), message.getChange());
        assertTrue(message.getChange().signum() < 0);
    }

    @Test
    void testBroadcastStockPriceUpdate_ZeroPrice() {
        // Arrange
        UUID stockId = UUID.randomUUID();
        String stockCode = "DELISTED";
        BigDecimal zeroPrice = BigDecimal.ZERO;

        // Act
        webSocketController.broadcastStockPriceUpdate(stockId, stockCode, zeroPrice);

        // Assert
        ArgumentCaptor<WebSocketController.StockPriceUpdateMessage> messageCaptor = ArgumentCaptor.forClass(WebSocketController.StockPriceUpdateMessage.class);
        verify(messagingTemplate).convertAndSend(anyString(), messageCaptor.capture());

        assertEquals(BigDecimal.ZERO, messageCaptor.getValue().getPrice());
    }

}