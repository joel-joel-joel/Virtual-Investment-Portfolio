package com.joelcode.personalinvestmentportfoliotracker.controllers.portfoliocontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
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
class AllocationBreakdownControllerTest {

    @Mock
    private AllocationBreakdownService allocationService;

    private AllocationBreakdownController allocationController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        allocationController = new AllocationBreakdownController();
        allocationController.allocationService = allocationService;
    }

    // Test retrieving allocation for an account when multiple records exist
    @Test
    void testGetAllocationForAccount_Success() {
        // Setup allocation list with sample data
        UUID accountId = UUID.randomUUID();
        List<AllocationBreakdownDTO> allocations = new ArrayList<>();
        allocations.add(new AllocationBreakdownDTO("AAPL", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));
        allocations.add(new AllocationBreakdownDTO("MSFT", BigDecimal.valueOf(8439), BigDecimal.valueOf(15)));
        allocations.add(new AllocationBreakdownDTO("GOOGL", BigDecimal.valueOf(3929), BigDecimal.valueOf(48)));

        // Map method return value to setup
        when(allocationService.getAllocationForAccount(accountId)).thenReturn(allocations);

        // Run method
        ResponseEntity<List<AllocationBreakdownDTO>> response = allocationController.getAllocationForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().size());
        verify(allocationService, times(1)).getAllocationForAccount(accountId);
    }

    // Test retrieving allocation for an account when no records exist
    @Test
    void testGetAllocationForAccount_Empty() {
        // Setup empty allocation list
        UUID accountId = UUID.randomUUID();
        when(allocationService.getAllocationForAccount(accountId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<AllocationBreakdownDTO>> response = allocationController.getAllocationForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(allocationService, times(1)).getAllocationForAccount(accountId);
    }

    // Test retrieving allocation for an account when a single record exists
    @Test
    void testGetAllocationForAccount_SingleAllocation() {
        // Setup allocation list with single entry
        UUID accountId = UUID.randomUUID();
        List<AllocationBreakdownDTO> allocations = new ArrayList<>();
        allocations.add(new AllocationBreakdownDTO("AAPL", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));

        // Map method return value to setup
        when(allocationService.getAllocationForAccount(accountId)).thenReturn(allocations);

        // Run method
        ResponseEntity<List<AllocationBreakdownDTO>> response = allocationController.getAllocationForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("AAPL", response.getBody().get(0).getStockCode());
        verify(allocationService, times(1)).getAllocationForAccount(accountId);
    }

    // Test retrieving allocation for a user when multiple records exist
    @Test
    void testGetAllocationForUser_Success() {
        // Setup allocation list with sample data
        UUID userId = UUID.randomUUID();
        List<AllocationBreakdownDTO> allocations = new ArrayList<>();
        allocations.add(new AllocationBreakdownDTO("AAPL", BigDecimal.valueOf(5000), BigDecimal.valueOf(50)));
        allocations.add(new AllocationBreakdownDTO("MSFT", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));

        // Map method return value to setup
        when(allocationService.getAllocationForUser(userId)).thenReturn(allocations);

        // Run method
        ResponseEntity<List<AllocationBreakdownDTO>> response = allocationController.getAllocationForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(allocationService, times(1)).getAllocationForUser(userId);
    }

    // Test retrieving allocation for a user when no records exist
    @Test
    void testGetAllocationForUser_Empty() {
        // Setup empty allocation list
        UUID userId = UUID.randomUUID();
        when(allocationService.getAllocationForUser(userId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<AllocationBreakdownDTO>> response = allocationController.getAllocationForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(allocationService, times(1)).getAllocationForUser(userId);
    }

    // Test retrieving allocation for a user when multiple allocations exist
    @Test
    void testGetAllocationForUser_MultipleAllocations() {
        // Setup allocation list with multiple entries
        UUID userId = UUID.randomUUID();
        List<AllocationBreakdownDTO> allocations = new ArrayList<>();
        allocations.add(new AllocationBreakdownDTO("AAPL", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));
        allocations.add(new AllocationBreakdownDTO("MSFT", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));
        allocations.add(new AllocationBreakdownDTO("GOOGL", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));
        allocations.add(new AllocationBreakdownDTO("AMZN", BigDecimal.valueOf(2000), BigDecimal.valueOf(25)));

        // Map method return value to setup
        when(allocationService.getAllocationForUser(userId)).thenReturn(allocations);

        // Run method
        ResponseEntity<List<AllocationBreakdownDTO>> response = allocationController.getAllocationForUser(userId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(4, response.getBody().size());
        verify(allocationService, times(1)).getAllocationForUser(userId);
    }
}
