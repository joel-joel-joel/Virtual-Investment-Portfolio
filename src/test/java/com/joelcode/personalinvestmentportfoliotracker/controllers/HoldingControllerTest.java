package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.HoldingController;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
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
class HoldingControllerTest {

    @Mock
    private HoldingService holdingService;

    private HoldingController holdingController;

    @BeforeEach
    void setUp() {
        holdingController = new HoldingController();
        holdingController.holdingService = holdingService;
    }

    @Test
    void testGetAllHoldings_Success() {
        // Arrange
        List<HoldingDTO> holdings = new ArrayList<>();
        holdings.add(new HoldingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "APPL",
                BigDecimal.valueOf(20), null ,null, null, LocalDateTime.now(),
                BigDecimal.valueOf(10), BigDecimal.valueOf(1000), null, null));
        holdings.add(new HoldingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "APPL",
                BigDecimal.valueOf(50), null ,null, null, LocalDateTime.now(),
                BigDecimal.valueOf(20), BigDecimal.valueOf(1500), null, null));
        when(holdingService.getAllHoldings()).thenReturn(holdings);

        // Act
        ResponseEntity<List<HoldingDTO>> response = holdingController.getAllHoldings();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(holdingService, times(1)).getAllHoldings();
    }

    @Test
    void testGetAllHoldings_Empty() {
        // Arrange
        when(holdingService.getAllHoldings()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<HoldingDTO>> response = holdingController.getAllHoldings();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(holdingService, times(1)).getAllHoldings();
    }

    @Test
    void testGetHoldingById_Success() {
        // Arrange
        UUID holdingId = UUID.randomUUID();
        HoldingDTO holding = new HoldingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "APPL",
                BigDecimal.valueOf(100), null ,null, null, LocalDateTime.now(),
                BigDecimal.valueOf(150), BigDecimal.valueOf(1200), null, null);
        when(holdingService.getHoldingById(holdingId)).thenReturn(holding);

        // Act
        ResponseEntity<HoldingDTO> response = holdingController.getHoldingById(holdingId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(100), response.getBody().getQuantity());
        verify(holdingService, times(1)).getHoldingById(holdingId);
    }

    @Test
    void testGetHoldingById_NotFound() {
        // Arrange
        UUID holdingId = UUID.randomUUID();
        when(holdingService.getHoldingById(holdingId)).thenReturn(null);

        // Act
        ResponseEntity<HoldingDTO> response = holdingController.getHoldingById(holdingId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(holdingService, times(1)).getHoldingById(holdingId);
    }

    @Test
    void testCreateHolding_Success() {
        // Arrange
        UUID holdingId = UUID.randomUUID();
        HoldingCreateRequest request = new HoldingCreateRequest(UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.valueOf(100), BigDecimal.valueOf(20) ,BigDecimal.valueOf(1000));
        HoldingDTO created = new HoldingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "APPL",
                BigDecimal.valueOf(100), BigDecimal.valueOf(20) ,BigDecimal.valueOf(1000), null, LocalDateTime.now(),
                BigDecimal.valueOf(10), BigDecimal.valueOf(1000), null, null);
        when(holdingService.createHolding(request)).thenReturn(created);

        // Act
        ResponseEntity<HoldingDTO> response = holdingController.createHolding(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(100), response.getBody().getQuantity());
        assertEquals(BigDecimal.valueOf(20), response.getBody().getAverageCostBasis());
        assertEquals(BigDecimal.valueOf(1000), response.getBody().getTotalCostBasis());
        verify(holdingService, times(1)).createHolding(request);
    }

    @Test
    void testUpdateHolding_Success() {
        // Arrange
        UUID holdingId = UUID.randomUUID();
        HoldingUpdateRequest request = new HoldingUpdateRequest(BigDecimal.valueOf(150), BigDecimal.valueOf(20),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100));

        HoldingDTO updated = new HoldingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "APPL",
                BigDecimal.valueOf(150), BigDecimal.valueOf(20) ,BigDecimal.valueOf(1000), BigDecimal.valueOf(100), LocalDateTime.now(),
                BigDecimal.valueOf(10), BigDecimal.valueOf(1000), null, null);
        when(holdingService.updateHolding(holdingId, request)).thenReturn(updated);

        // Act
        ResponseEntity<HoldingDTO> response = holdingController.updateHolding(holdingId, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(150), response.getBody().getQuantity());
        assertEquals(BigDecimal.valueOf(20), response.getBody().getAverageCostBasis());
        assertEquals(BigDecimal.valueOf(1000), response.getBody().getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(100), response.getBody().getRealizedGain());
        verify(holdingService, times(1)).updateHolding(holdingId, request);
    }

    @Test
    void testUpdateHolding_NotFound() {
        // Arrange
        UUID holdingId = UUID.randomUUID();
        HoldingUpdateRequest request = new HoldingUpdateRequest(BigDecimal.valueOf(150), BigDecimal.valueOf(20),
                BigDecimal.valueOf(1000), BigDecimal.valueOf(100));
        when(holdingService.updateHolding(holdingId, request)).thenReturn(null);

        // Act
        ResponseEntity<HoldingDTO> response = holdingController.updateHolding(holdingId, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(holdingService, times(1)).updateHolding(holdingId, request);
    }

    @Test
    void testDeleteHolding_Success() {
        // Arrange
        UUID holdingId = UUID.randomUUID();
        doNothing().when(holdingService).deleteHolding(holdingId);

        // Act
        ResponseEntity<Void> response = holdingController.deleteHolding(holdingId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(holdingService, times(1)).deleteHolding(holdingId);
    }

    @Test
    void testGetHoldingsForAccount_Success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<HoldingDTO> holdings = new ArrayList<>();
        holdings.add(new HoldingDTO(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "APPL",
                BigDecimal.valueOf(100), BigDecimal.valueOf(20) ,BigDecimal.valueOf(1000), null, LocalDateTime.now(),
                BigDecimal.valueOf(10), BigDecimal.valueOf(1000), null, null));
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(holdings);

        // Act
        ResponseEntity<List<HoldingDTO>> response = holdingController.getHoldingsForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(holdingService, times(1)).getHoldingsForAccount(accountId);
    }

    @Test
    void testGetHoldingsForAccount_Empty() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<HoldingDTO>> response = holdingController.getHoldingsForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(holdingService, times(1)).getHoldingsForAccount(accountId);
    }
}