package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.StockController;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    // Define key variables
    @Mock
    private StockService stockService;

    private StockController stockController;

    @BeforeEach
    void setUp() {
        stockController = new StockController();
        stockController.stockService = stockService;
    }

    // Test for retrieving all stocks
    @Test
    void testGetAllStocks_Success() {
        // Setup array of stocks
        List<StockDTO> stocks = new ArrayList<>();
        stocks.add(new StockDTO(UUID.randomUUID(), "AAPL", "Apple Inc", BigDecimal.valueOf(150)));
        stocks.add(new StockDTO(UUID.randomUUID(), "MSFT", "Microsoft Corp", BigDecimal.valueOf(300)));

        // Map method return value to setup
        when(stockService.getAllStocks()).thenReturn(stocks);

        // Run method
        ResponseEntity<List<StockDTO>> response = stockController.getAllStocks();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(stockService, times(1)).getAllStocks();
    }

    // Test null case
    @Test
    void testGetAllStocks_Empty() {
        // Map method return value to setup
        when(stockService.getAllStocks()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<StockDTO>> response = stockController.getAllStocks();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(stockService, times(1)).getAllStocks();
    }

    // Test retrieving a stock by id
    @Test
    void testGetStockById_Success() {
        // Setup a stock
        UUID stockId = UUID.randomUUID();
        StockDTO stock = new StockDTO(stockId, "AAPL", "Apple Inc", BigDecimal.valueOf(150));

        // Map method return value to setup
        when(stockService.getStockById(stockId)).thenReturn(stock);

        // Run method
        ResponseEntity<StockDTO> response = stockController.getStockById(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AAPL", response.getBody().getStockCode());
        verify(stockService, times(1)).getStockById(stockId);
    }

    // Test null case
    @Test
    void testGetStockById_NotFound() {
        // Initialize stock if
        UUID stockId = UUID.randomUUID();

        // Map method return value to setup
        when(stockService.getStockById(stockId)).thenReturn(null);

        // Run method
        ResponseEntity<StockDTO> response = stockController.getStockById(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(stockService, times(1)).getStockById(stockId);
    }

    // Test creating a stock
    @Test
    void testCreateStock_Success() {
        // Setup create request and identical control stock
        UUID stockId = UUID.randomUUID();
        StockCreateRequest request = new StockCreateRequest(stockId, "Apple Inc", "AAPL", BigDecimal.valueOf(150),
                "Tech");
        StockDTO created = new StockDTO(stockId, "AAPL", "Apple Inc", BigDecimal.valueOf(150));

        // Map method return value to setup
        when(stockService.createStock(request)).thenReturn(created);

        // Run method
        ResponseEntity<StockDTO> response = stockController.createStock(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("AAPL", response.getBody().getStockCode());
        verify(stockService, times(1)).createStock(request);
    }

    // Test updating a stock
    @Test
    void testUpdateStock_Success() {
        // Setup update request and identical control updated stock
        UUID stockId = UUID.randomUUID();
        StockUpdateRequest request = new StockUpdateRequest("AAPL", "Apple Incorporated", stockId,
                "Tech");
        StockDTO updated = new StockDTO(stockId, "AAPL", "Apple Incorporated", BigDecimal.valueOf(155));

        // Map method return value to setup
        when(stockService.updateStock(stockId, request)).thenReturn(updated);

        // Run method
        ResponseEntity<StockDTO> response = stockController.updateStock(stockId, request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Apple Incorporated", response.getBody().getCompanyName());
        verify(stockService, times(1)).updateStock(stockId, request);
    }

    // Test null case
    @Test
    void testUpdateStock_NotFound() {
        // Setup stock id and update request
        UUID stockId = UUID.randomUUID();
        StockUpdateRequest request = new StockUpdateRequest("AAPL", "Apple Incorporated", stockId,
                "Tech");

        // Map method return value to setup
        when(stockService.updateStock(stockId, request)).thenReturn(null);

        // Run method
        ResponseEntity<StockDTO> response = stockController.updateStock(stockId, request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(stockService, times(1)).updateStock(stockId, request);
    }

    // Test deleting a stock
    @Test
    void testDeleteStock_Success() {
        // Arrange
        UUID stockId = UUID.randomUUID();
        doNothing().when(stockService).deleteStock(stockId);

        // Run method
        ResponseEntity<Void> response = stockController.deleteStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(stockService, times(1)).deleteStock(stockId);
    }

    // Test retrieving current stock price
    @Test
    void testGetCurrentPrice_Success() {
        // Arrange
        UUID stockId = UUID.randomUUID();
        BigDecimal price = BigDecimal.valueOf(150.0);
        when(stockService.getCurrentPrice(stockId)).thenReturn(price);

        // Run method
        ResponseEntity<?> response = stockController.getCurrentPrice(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(price, response.getBody());
        verify(stockService, times(1)).getCurrentPrice(stockId);
    }
}