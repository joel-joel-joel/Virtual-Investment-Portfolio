package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.PortfolioSnapshotController;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot.PortfolioSnapshotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotControllerTest {

    @Mock
    private PortfolioSnapshotService snapshotService;

    private PortfolioSnapshotController snapshotController;

    @BeforeEach
    void setUp() {
        snapshotController = new PortfolioSnapshotController();
        snapshotController.snapshotService = snapshotService;
    }

    @Test
    void testGetAllSnapshots_Success() {
        // Arrange
        List<PortfolioSnapshotDTO> snapshots = new ArrayList<>();
        snapshots.add(new PortfolioSnapshotDTO(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2)));
        snapshots.add(new PortfolioSnapshotDTO(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().minusDays(1) ,
                BigDecimal.valueOf(20000), BigDecimal.valueOf(8000), BigDecimal.valueOf(3000), BigDecimal.valueOf(3639),
                BigDecimal.valueOf(22), BigDecimal.valueOf(32.3), BigDecimal.valueOf(1.2)));
        when(snapshotService.getAllSnapshots()).thenReturn(snapshots);

        // Act
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getAllSnapshots();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(snapshotService, times(1)).getAllSnapshots();
    }

    @Test
    void testGetAllSnapshots_Empty() {
        // Arrange
        when(snapshotService.getAllSnapshots()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getAllSnapshots();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(snapshotService, times(1)).getAllSnapshots();
    }

    @Test
    void testGetSnapshotById_Success() {
        // Arrange
        UUID snapshotId = UUID.randomUUID();
        PortfolioSnapshotDTO snapshot = new PortfolioSnapshotDTO(snapshotId, UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2));
        when(snapshotService.getSnapshotById(snapshotId)).thenReturn(snapshot);

        // Act
        ResponseEntity<PortfolioSnapshotDTO> response = snapshotController.getSnapshotById(snapshotId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(10000), response.getBody().getTotalValue());
        assertEquals(snapshotId, response.getBody().getSnapshotId());
        verify(snapshotService, times(1)).getSnapshotById(snapshotId);
    }

    @Test
    void testGetSnapshotById_NotFound() {
        // Arrange
        UUID snapshotId = UUID.randomUUID();
        when(snapshotService.getSnapshotById(snapshotId)).thenReturn(null);

        // Act
        ResponseEntity<PortfolioSnapshotDTO> response = snapshotController.getSnapshotById(snapshotId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(snapshotService, times(1)).getSnapshotById(snapshotId);
    }

    @Test
    void testCreateSnapshot_Success() {
        // Arrange
        UUID snapshotId = UUID.randomUUID();
        PortfolioSnapshotCreateRequest request = new PortfolioSnapshotCreateRequest(UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38.3));

        PortfolioSnapshotDTO created = new PortfolioSnapshotDTO(snapshotId, UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2));
        when(snapshotService.createSnapshot(request)).thenReturn(created);

        // Act
        ResponseEntity<PortfolioSnapshotDTO> response = snapshotController.createSnapshot(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(10000), response.getBody().getTotalValue());
        assertEquals(snapshotId, response.getBody().getSnapshotId());
        assertEquals(BigDecimal.valueOf(7000), response.getBody().getCashBalance());
        verify(snapshotService, times(1)).createSnapshot(request);
    }

    @Test
    void testDeleteSnapshot_Success() {
        // Arrange
        UUID snapshotId = UUID.randomUUID();
        doNothing().when(snapshotService).deleteSnapshot(snapshotId);

        // Act
        ResponseEntity<Void> response = snapshotController.deleteSnapshot(snapshotId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(snapshotService, times(1)).deleteSnapshot(snapshotId);
    }

    @Test
    void testGetSnapshotsForAccount_Success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<PortfolioSnapshotDTO> snapshots = new ArrayList<>();
        snapshots.add(new PortfolioSnapshotDTO(UUID.randomUUID(), accountId, LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2)));
        when(snapshotService.getSnapshotsForAccount(accountId)).thenReturn(snapshots);

        // Act
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getSnapshotsForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(snapshotService, times(1)).getSnapshotsForAccount(accountId);
    }

    @Test
    void testGetSnapshotsForAccount_Empty() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        when(snapshotService.getSnapshotsForAccount(accountId)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getSnapshotsForAccount(accountId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(snapshotService, times(1)).getSnapshotsForAccount(accountId);
    }
}