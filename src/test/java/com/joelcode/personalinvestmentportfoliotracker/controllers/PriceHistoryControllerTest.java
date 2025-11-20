package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.PriceHistoryController;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceHistoryControllerTest {

    @Mock
    private PriceHistoryService priceHistoryService;

    private PriceHistoryController priceHistoryController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        priceHistoryController = new PriceHistoryController();
        priceHistoryController.priceHistoryService = priceHistoryService;
    }

    // Test retrieving all price histories when records exist
    @Test
    void testGetAllPriceHistory_Success() {
        // Setup price history list with sample data
        List<PriceHistoryDTO> priceHistories = new ArrayList<>();
        priceHistories.add(new PriceHistoryDTO(UUID.randomUUID(), LocalDateTime.now(), BigDecimal.valueOf(150), UUID.randomUUID()));
        priceHistories.add(new PriceHistoryDTO(UUID.randomUUID(), LocalDateTime.now().minusDays(2), BigDecimal.valueOf(155), UUID.randomUUID()));

        // Map method return value to setup
        when(priceHistoryService.getAllPriceHistories()).thenReturn(priceHistories);

        // Run method
        ResponseEntity<List<PriceHistoryDTO>> response = priceHistoryController.getAllPriceHistory();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(priceHistoryService, times(1)).getAllPriceHistories();
    }

    // Test retrieving all price histories when no records exist
    @Test
    void testGetAllPriceHistory_Empty() {
        // Setup empty price history list
        when(priceHistoryService.getAllPriceHistories()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<PriceHistoryDTO>> response = priceHistoryController.getAllPriceHistory();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(priceHistoryService, times(1)).getAllPriceHistories();
    }

    // Test retrieving a single price history by ID when record exists
    @Test
    void testGetPriceHistoryById_Success() {
        // Setup price history DTO with sample data
        UUID priceHistoryId = UUID.randomUUID();
        PriceHistoryDTO priceHistory = new PriceHistoryDTO(UUID.randomUUID(), LocalDateTime.now(), BigDecimal.valueOf(150), UUID.randomUUID());

        // Map method return value to setup
        when(priceHistoryService.getPriceHistoryById(priceHistoryId)).thenReturn(priceHistory);

        // Run method
        ResponseEntity<PriceHistoryDTO> response = priceHistoryController.getPriceHistoryById(priceHistoryId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(150), response.getBody().getClosePrice());
        verify(priceHistoryService, times(1)).getPriceHistoryById(priceHistoryId);
    }

    // Test retrieving a single price history by ID when record does not exist
    @Test
    void testGetPriceHistoryById_NotFound() {
        // Setup price history ID with null return
        UUID priceHistoryId = UUID.randomUUID();
        when(priceHistoryService.getPriceHistoryById(priceHistoryId)).thenReturn(null);

        // Run method
        ResponseEntity<PriceHistoryDTO> response = priceHistoryController.getPriceHistoryById(priceHistoryId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(priceHistoryService, times(1)).getPriceHistoryById(priceHistoryId);
    }

    // Test creating a price history record successfully
    @Test
    void testCreatePriceHistory_Success() {
        // Setup create request and expected DTO
        UUID priceHistoryId = UUID.randomUUID();
        PriceHistoryCreateRequest request = new PriceHistoryCreateRequest(LocalDateTime.now(), priceHistoryId, BigDecimal.valueOf(150));
        PriceHistoryDTO created = new PriceHistoryDTO(priceHistoryId, LocalDateTime.now(), BigDecimal.valueOf(150), UUID.randomUUID());

        // Map method return value to setup
        when(priceHistoryService.createPriceHistory(request)).thenReturn(created);

        // Run method
        ResponseEntity<PriceHistoryDTO> response = priceHistoryController.createPriceHistory(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(150), response.getBody().getClosePrice());
        verify(priceHistoryService, times(1)).createPriceHistory(request);
    }

    // Test deleting a price history record successfully
    @Test
    void testDeletePriceHistory_Success() {
        // Setup price history ID and mock void service
        UUID priceHistoryId = UUID.randomUUID();
        doNothing().when(priceHistoryService).deletePriceHistory(priceHistoryId);

        // Run method
        ResponseEntity<Void> response = priceHistoryController.deletePriceHistory(priceHistoryId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(priceHistoryService, times(1)).deletePriceHistory(priceHistoryId);
    }

    // Test retrieving price histories for a specific stock when records exist
    @Test
    void testGetPriceHistoryForStock_Success() {
        // Setup stock ID and price history list
        UUID stockId = UUID.randomUUID();
        List<PriceHistoryDTO> priceHistories = new ArrayList<>();
        priceHistories.add(new PriceHistoryDTO(UUID.randomUUID(), LocalDateTime.now(), BigDecimal.valueOf(150), stockId));
        priceHistories.add(new PriceHistoryDTO(UUID.randomUUID(), LocalDateTime.now().minusDays(1), BigDecimal.valueOf(155), stockId));

        // Map method return value to setup
        when(priceHistoryService.getPriceHistoryForStock(stockId)).thenReturn(priceHistories);

        // Run method
        ResponseEntity<List<PriceHistoryDTO>> response = priceHistoryController.getPriceHistoryForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(priceHistoryService, times(1)).getPriceHistoryForStock(stockId);
    }

    // Test retrieving price histories for a specific stock when no records exist
    @Test
    void testGetPriceHistoryForStock_Empty() {
        // Setup stock ID and empty list
        UUID stockId = UUID.randomUUID();
        when(priceHistoryService.getPriceHistoryForStock(stockId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<PriceHistoryDTO>> response = priceHistoryController.getPriceHistoryForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(priceHistoryService, times(1)).getPriceHistoryForStock(stockId);
    }

    // Test retrieving latest price for a stock successfully
    @Test
    void testGetLatestPriceForStock_Success() {
        // Setup stock ID and latest price DTO
        UUID stockId = UUID.randomUUID();
        PriceHistoryDTO latestPrice = new PriceHistoryDTO(UUID.randomUUID(), LocalDateTime.now(), BigDecimal.valueOf(160), stockId);

        // Map method return value to setup
        when(priceHistoryService.getLatestPriceForStock(stockId)).thenReturn(latestPrice);

        // Run method
        ResponseEntity<PriceHistoryDTO> response = priceHistoryController.getLatestPriceForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(160), response.getBody().getClosePrice());
        verify(priceHistoryService, times(1)).getLatestPriceForStock(stockId);
    }

    // Test retrieving latest price for a stock when record does not exist
    @Test
    void testGetLatestPriceForStock_NotFound() {
        // Setup stock ID with null return
        UUID stockId = UUID.randomUUID();
        when(priceHistoryService.getLatestPriceForStock(stockId)).thenReturn(null);

        // Run method
        ResponseEntity<PriceHistoryDTO> response = priceHistoryController.getLatestPriceForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(priceHistoryService, times(1)).getLatestPriceForStock(stockId);
    }
}
