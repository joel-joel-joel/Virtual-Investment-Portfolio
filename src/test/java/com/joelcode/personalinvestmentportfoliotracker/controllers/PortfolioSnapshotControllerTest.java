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
        // Initialize controller and inject mocked service
        snapshotController = new PortfolioSnapshotController();
        snapshotController.snapshotService = snapshotService;
    }

    // Test retrieving all snapshots when records exist
    @Test
    void testGetAllSnapshots_Success() {
        // Setup snapshot list with sample data
        List<PortfolioSnapshotDTO> snapshots = new ArrayList<>();
        snapshots.add(new PortfolioSnapshotDTO(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2)));

        snapshots.add(new PortfolioSnapshotDTO(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now().minusDays(1),
                BigDecimal.valueOf(20000), BigDecimal.valueOf(8000), BigDecimal.valueOf(3000), BigDecimal.valueOf(3639),
                BigDecimal.valueOf(22), BigDecimal.valueOf(32.3), BigDecimal.valueOf(1.2)));

        // Map method return value to setup
        when(snapshotService.getAllSnapshots()).thenReturn(snapshots);

        // Run method
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getAllSnapshots();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(snapshotService, times(1)).getAllSnapshots();
    }

    // Test retrieving all snapshots when no records exist
    @Test
    void testGetAllSnapshots_Empty() {
        // Setup empty snapshot list
        when(snapshotService.getAllSnapshots()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getAllSnapshots();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(snapshotService, times(1)).getAllSnapshots();
    }

    // Test retrieving a single snapshot by ID when record exists
    @Test
    void testGetSnapshotById_Success() {
        // Setup snapshot DTO with sample data
        UUID snapshotId = UUID.randomUUID();
        PortfolioSnapshotDTO snapshot = new PortfolioSnapshotDTO(snapshotId, UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2));

        // Map method return value to setup
        when(snapshotService.getSnapshotById(snapshotId)).thenReturn(snapshot);

        // Run method
        ResponseEntity<PortfolioSnapshotDTO> response = snapshotController.getSnapshotById(snapshotId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(10000), response.getBody().getTotalValue());
        assertEquals(snapshotId, response.getBody().getSnapshotId());
        verify(snapshotService, times(1)).getSnapshotById(snapshotId);
    }

    // Test retrieving a single snapshot by ID when record does not exist
    @Test
    void testGetSnapshotById_NotFound() {
        // Setup snapshot ID with null return
        UUID snapshotId = UUID.randomUUID();
        when(snapshotService.getSnapshotById(snapshotId)).thenReturn(null);

        // Run method
        ResponseEntity<PortfolioSnapshotDTO> response = snapshotController.getSnapshotById(snapshotId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(snapshotService, times(1)).getSnapshotById(snapshotId);
    }

    // Test creating a snapshot successfully
    @Test
    void testCreateSnapshot_Success() {
        // Setup create request and expected DTO
        UUID snapshotId = UUID.randomUUID();
        PortfolioSnapshotCreateRequest request = new PortfolioSnapshotCreateRequest(UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38.3));

        PortfolioSnapshotDTO created = new PortfolioSnapshotDTO(snapshotId, UUID.randomUUID(), LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2));

        // Map method return value to setup
        when(snapshotService.createSnapshot(request)).thenReturn(created);

        // Run method
        ResponseEntity<PortfolioSnapshotDTO> response = snapshotController.createSnapshot(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(10000), response.getBody().getTotalValue());
        assertEquals(snapshotId, response.getBody().getSnapshotId());
        assertEquals(BigDecimal.valueOf(7000), response.getBody().getCashBalance());
        verify(snapshotService, times(1)).createSnapshot(request);
    }

    // Test deleting a snapshot successfully
    @Test
    void testDeleteSnapshot_Success() {
        // Setup snapshot ID and mock void service
        UUID snapshotId = UUID.randomUUID();
        doNothing().when(snapshotService).deleteSnapshot(snapshotId);

        // Run method
        ResponseEntity<Void> response = snapshotController.deleteSnapshot(snapshotId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(snapshotService, times(1)).deleteSnapshot(snapshotId);
    }

    // Test retrieving snapshots for a specific account when records exist
    @Test
    void testGetSnapshotsForAccount_Success() {
        // Setup account ID and snapshot list
        UUID accountId = UUID.randomUUID();
        List<PortfolioSnapshotDTO> snapshots = new ArrayList<>();
        snapshots.add(new PortfolioSnapshotDTO(UUID.randomUUID(), accountId, LocalDate.now(),
                BigDecimal.valueOf(10000), BigDecimal.valueOf(7000), BigDecimal.valueOf(4000), BigDecimal.valueOf(3939),
                BigDecimal.valueOf(38), BigDecimal.valueOf(38.3), BigDecimal.valueOf(3.2)));

        // Map method return value to setup
        when(snapshotService.getSnapshotsForAccount(accountId)).thenReturn(snapshots);

        // Run method
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getSnapshotsForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(snapshotService, times(1)).getSnapshotsForAccount(accountId);
    }

    // Test retrieving snapshots for a specific account when no records exist
    @Test
    void testGetSnapshotsForAccount_Empty() {
        // Setup empty result for account
        UUID accountId = UUID.randomUUID();
        when(snapshotService.getSnapshotsForAccount(accountId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<PortfolioSnapshotDTO>> response = snapshotController.getSnapshotsForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(snapshotService, times(1)).getSnapshotsForAccount(accountId);
    }
}
