package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.DividendController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendService;
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
class DividendControllerTest {

    @Mock
    private DividendService dividendService;

    private DividendController dividendController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        dividendController = new DividendController();
        dividendController.dividendService = dividendService;
    }

    // Test retrieving all dividends when records exist
    @Test
    void testGetAllDividends_Success() {
        // Setup dividend list with sample data
        List<DividendDTO> dividends = new ArrayList<>();
        dividends.add(new DividendDTO(UUID.randomUUID(), UUID.randomUUID(), "APPL", BigDecimal.valueOf(2.25), LocalDateTime.now(), LocalDateTime.now()));
        dividends.add(new DividendDTO(UUID.randomUUID(), UUID.randomUUID(), "TSLA", BigDecimal.valueOf(3.5), LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)));

        // Map method return value to setup
        when(dividendService.getAllDividends()).thenReturn(dividends);

        // Run method
        ResponseEntity<List<DividendDTO>> response = dividendController.getAllDividends();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(dividendService, times(1)).getAllDividends();
    }

    // Test retrieving all dividends when no records exist
    @Test
    void testGetAllDividends_Empty() {
        // Setup empty dividend list
        when(dividendService.getAllDividends()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<DividendDTO>> response = dividendController.getAllDividends();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(dividendService, times(1)).getAllDividends();
    }

    // Test retrieving a single dividend by ID when record exists
    @Test
    void testGetDividendById_Success() {
        // Setup dividend DTO with sample data
        UUID dividendId = UUID.randomUUID();
        DividendDTO dividend = new DividendDTO(dividendId, UUID.randomUUID(), "APPL", BigDecimal.valueOf(2.25), LocalDateTime.now(), LocalDateTime.now());

        // Map method return value to setup
        when(dividendService.getDividendById(dividendId)).thenReturn(dividend);

        // Run method
        ResponseEntity<DividendDTO> response = dividendController.getDividendById(dividendId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(2.25), response.getBody().getDividendPerShare());
        verify(dividendService, times(1)).getDividendById(dividendId);
    }

    // Test retrieving a single dividend by ID when record does not exist
    @Test
    void testGetDividendById_NotFound() {
        // Setup dividend ID with null return
        UUID dividendId = UUID.randomUUID();
        when(dividendService.getDividendById(dividendId)).thenReturn(null);

        // Run method
        ResponseEntity<DividendDTO> response = dividendController.getDividendById(dividendId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dividendService, times(1)).getDividendById(dividendId);
    }

    // Test creating a dividend record successfully
    @Test
    void testCreateDividend_Success() {
        // Setup create request and expected DTO
        UUID dividendId = UUID.randomUUID();
        DividendCreateRequest request = new DividendCreateRequest(UUID.randomUUID(), BigDecimal.valueOf(2.25), LocalDateTime.now());
        DividendDTO created = new DividendDTO(dividendId, UUID.randomUUID(), "APPL", BigDecimal.valueOf(2.25), LocalDateTime.now(), LocalDateTime.now());

        // Map method return value to setup
        when(dividendService.createDividend(request)).thenReturn(created);

        // Run method
        ResponseEntity<DividendDTO> response = dividendController.createDividend(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(2.25), response.getBody().getDividendPerShare());
        assertEquals(dividendId, response.getBody().getDividendId());
        verify(dividendService, times(1)).createDividend(request);
    }

    // Test deleting a dividend record successfully
    @Test
    void testDeleteDividend_Success() {
        // Setup dividend ID and mock void service
        UUID dividendId = UUID.randomUUID();
        doNothing().when(dividendService).deleteDividend(dividendId);

        // Run method
        ResponseEntity<Void> response = dividendController.deleteDividend(dividendId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(dividendService, times(1)).deleteDividend(dividendId);
    }

    // Test retrieving dividends for a specific stock when records exist
    @Test
    void testGetDividendsForStock_Success() {
        // Setup stock ID and dividend list
        UUID stockId = UUID.randomUUID();
        List<DividendDTO> dividends = new ArrayList<>();
        dividends.add(new DividendDTO(UUID.randomUUID(), stockId, "APPL", BigDecimal.valueOf(2.25), LocalDateTime.now(), LocalDateTime.now()));

        // Map method return value to setup
        when(dividendService.getDividendsByStock(stockId)).thenReturn(dividends);

        // Run method
        ResponseEntity<List<DividendDTO>> response = dividendController.getDividendsForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(dividendService, times(1)).getDividendsByStock(stockId);
    }

    // Test retrieving dividends for a specific stock when no records exist
    @Test
    void testGetDividendsForStock_Empty() {
        // Setup stock ID and empty list
        UUID stockId = UUID.randomUUID();
        when(dividendService.getDividendsByStock(stockId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<DividendDTO>> response = dividendController.getDividendsForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(dividendService, times(1)).getDividendsByStock(stockId);
    }
}
