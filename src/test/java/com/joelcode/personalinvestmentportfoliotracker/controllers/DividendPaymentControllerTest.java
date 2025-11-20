package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers.DividendPaymentController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.DividendPayment;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
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
class DividendPaymentControllerTest {

    @Mock
    private DividendPaymentService dividendPaymentService;

    private DividendPaymentController dividendPaymentController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked service
        dividendPaymentController = new DividendPaymentController();
        dividendPaymentController.dividendPaymentService = dividendPaymentService;
    }

    // Test retrieving all dividend payments when records exist
    @Test
    void testGetAllDividendPayments_Success() {
        // Setup dividend payment list with sample data
        List<DividendPaymentDTO> payments = new ArrayList<>();
        payments.add(new DividendPaymentDTO(UUID.randomUUID(), UUID.randomUUID(), "Checking",
                UUID.randomUUID(),"APPL", UUID.randomUUID(), BigDecimal.valueOf(2.5), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now(), DividendPayment.PaymentStatus.PENDING));

        payments.add(new DividendPaymentDTO(UUID.randomUUID(), UUID.randomUUID(), "Saving",
                UUID.randomUUID(),"TSLA", UUID.randomUUID(), BigDecimal.valueOf(3.5), BigDecimal.valueOf(40),
                BigDecimal.valueOf(10000), LocalDateTime.now(), LocalDateTime.now(), DividendPayment.PaymentStatus.PAID));

        // Map method return value to setup
        when(dividendPaymentService.getAllDividendPayments()).thenReturn(payments);

        // Run method
        ResponseEntity<List<DividendPaymentDTO>> response = dividendPaymentController.getAllDividendPayments();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(dividendPaymentService, times(1)).getAllDividendPayments();
    }

    // Test retrieving all dividend payments when no records exist
    @Test
    void testGetAllDividendPayments_Empty() {
        // Setup empty dividend payment list
        when(dividendPaymentService.getAllDividendPayments()).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<DividendPaymentDTO>> response = dividendPaymentController.getAllDividendPayments();

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(dividendPaymentService, times(1)).getAllDividendPayments();
    }

    // Test retrieving a single dividend payment by ID when record exists
    @Test
    void testGetDividendPaymentById_Success() {
        // Setup dividend payment DTO with sample data
        UUID paymentId = UUID.randomUUID();
        DividendPaymentDTO payment = new DividendPaymentDTO(UUID.randomUUID(), UUID.randomUUID(), "Checking",
                UUID.randomUUID(),"APPL", UUID.randomUUID(), BigDecimal.valueOf(2.5), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now(), DividendPayment.PaymentStatus.PENDING);

        // Map method return value to setup
        when(dividendPaymentService.getDividendPaymentById(paymentId)).thenReturn(payment);

        // Run method
        ResponseEntity<DividendPaymentDTO> response = dividendPaymentController.getDividendPaymentById(paymentId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(2.5), response.getBody().getDividendPerShare());
        verify(dividendPaymentService, times(1)).getDividendPaymentById(paymentId);
    }

    // Test retrieving a single dividend payment by ID when record does not exist
    @Test
    void testGetDividendPaymentById_NotFound() {
        // Setup dividend payment ID with null return
        UUID paymentId = UUID.randomUUID();
        when(dividendPaymentService.getDividendPaymentById(paymentId)).thenReturn(null);

        // Run method
        ResponseEntity<DividendPaymentDTO> response = dividendPaymentController.getDividendPaymentById(paymentId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dividendPaymentService, times(1)).getDividendPaymentById(paymentId);
    }

    // Test creating a dividend payment record successfully
    @Test
    void testCreateDividendPayment_Success() {
        // Setup create request and expected DTO
        UUID paymentId = UUID.randomUUID();
        DividendPaymentCreateRequest request = new DividendPaymentCreateRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.valueOf(2.5), LocalDateTime.now());
        DividendPaymentDTO created = new DividendPaymentDTO(paymentId, UUID.randomUUID(), "Checking",
                UUID.randomUUID(),"APPL", UUID.randomUUID(), BigDecimal.valueOf(2.5), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now(), DividendPayment.PaymentStatus.PENDING);

        // Map method return value to setup
        when(dividendPaymentService.createDividendPayment(request)).thenReturn(created);

        // Run method
        ResponseEntity<DividendPaymentDTO> response = dividendPaymentController.createDividendPayment(request);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(BigDecimal.valueOf(2.5), response.getBody().getDividendPerShare());
        assertEquals(paymentId, response.getBody().getPaymentId());
        verify(dividendPaymentService, times(1)).createDividendPayment(request);
    }

    // Test deleting a dividend payment record successfully
    @Test
    void testDeleteDividendPayment_Success() {
        // Setup dividend payment ID and mock void service
        UUID paymentId = UUID.randomUUID();
        doNothing().when(dividendPaymentService).deleteDividendPayment(paymentId);

        // Run method
        ResponseEntity<Void> response = dividendPaymentController.deleteDividendPayment(paymentId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(dividendPaymentService, times(1)).deleteDividendPayment(paymentId);
    }

    // Test retrieving dividend payments for a specific stock when records exist
    @Test
    void testGetDividendPaymentsForStock_Success() {
        // Setup stock ID and dividend payment list
        UUID stockId = UUID.randomUUID();
        List<DividendPaymentDTO> payments = new ArrayList<>();
        payments.add(new DividendPaymentDTO(UUID.randomUUID(), UUID.randomUUID(), "Checking",
                UUID.randomUUID(),"APPL", UUID.randomUUID(), BigDecimal.valueOf(2.5), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now(), DividendPayment.PaymentStatus.PENDING));

        // Map method return value to setup
        when(dividendPaymentService.getDividendPaymentsForStock(stockId)).thenReturn(payments);

        // Run method
        ResponseEntity<List<DividendPaymentDTO>> response = dividendPaymentController.getDividendPaymentsForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(dividendPaymentService, times(1)).getDividendPaymentsForStock(stockId);
    }

    // Test retrieving dividend payments for a specific stock when no records exist
    @Test
    void testGetDividendPaymentsForStock_Empty() {
        // Setup stock ID with empty list
        UUID stockId = UUID.randomUUID();
        when(dividendPaymentService.getDividendPaymentsForStock(stockId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<DividendPaymentDTO>> response = dividendPaymentController.getDividendPaymentsForStock(stockId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(dividendPaymentService, times(1)).getDividendPaymentsForStock(stockId);
    }

    // Test retrieving dividend payments for a specific account when records exist
    @Test
    void testGetDividendPaymentsForAccount_Success() {
        // Setup account ID and dividend payment list
        UUID accountId = UUID.randomUUID();
        List<DividendPaymentDTO> payments = new ArrayList<>();
        payments.add(new DividendPaymentDTO(UUID.randomUUID(), UUID.randomUUID(), "Checking",
                UUID.randomUUID(),"APPL", UUID.randomUUID(), BigDecimal.valueOf(2.5), BigDecimal.valueOf(30),
                BigDecimal.valueOf(5000), LocalDateTime.now(), LocalDateTime.now(), DividendPayment.PaymentStatus.PENDING));

        // Map method return value to setup
        when(dividendPaymentService.getDividendPaymentsForAccount(accountId)).thenReturn(payments);

        // Run method
        ResponseEntity<List<DividendPaymentDTO>> response = dividendPaymentController.getDividendPaymentsForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(dividendPaymentService, times(1)).getDividendPaymentsForAccount(accountId);
    }

    // Test retrieving dividend payments for a specific account when no records exist
    @Test
    void testGetDividendPaymentsForAccount_Empty() {
        // Setup account ID with empty list
        UUID accountId = UUID.randomUUID();
        when(dividendPaymentService.getDividendPaymentsForAccount(accountId)).thenReturn(new ArrayList<>());

        // Run method
        ResponseEntity<List<DividendPaymentDTO>> response = dividendPaymentController.getDividendPaymentsForAccount(accountId);

        // Assert testing variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
        verify(dividendPaymentService, times(1)).getDividendPaymentsForAccount(accountId);
    }
}
