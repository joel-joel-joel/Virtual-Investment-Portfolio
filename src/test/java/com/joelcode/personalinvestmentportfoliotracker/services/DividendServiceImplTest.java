package com.joelcode.personalinvestmentportfoliotracker.services;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Testing dividend service layer business logic
public class DividendServiceImplTest {

    // Define mock key fields
    @Mock
    private DividendRepository dividendRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private DividendValidationService dividendValidationService;

    @Mock
    private DividendPaymentService dividendPaymentService;

    @Mock
    private WebSocketController webSocketController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private DividendServiceImpl dividendService;

    private Dividend testDividend;
    private Stock testStock;
    private UUID dividendId;
    private UUID stockId;

    // Set up a test dividend with mock values
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        dividendId = UUID.randomUUID();
        stockId = UUID.randomUUID();

        testStock = new Stock();
        testStock.setStockId(stockId);
        testStock.setStockCode("AAPL");
        testStock.setCompanyName("Apple Inc.");

        testDividend = new Dividend();
        testDividend.setDividendId(dividendId);
        testDividend.setStock(testStock);
        testDividend.setAmountPerShare(BigDecimal.valueOf(0.25));
        testDividend.setPayDate(LocalDateTime.now().plusDays(30));
        testDividend.setAnnouncementDate(LocalDateTime.now());
    }

    // Test dividend creation
    @Test
    void testCreateDividend_Success() {
        // Create dividend creation request
        DividendCreateRequest request = new DividendCreateRequest(
                stockId,
                BigDecimal.valueOf(0.25),
                LocalDateTime.now().plusDays(30)
        );

        // Mock repository behavior
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(dividendRepository.existsByStockAndPayDate(any(), any())).thenReturn(false);
        when(dividendRepository.save(any(Dividend.class))).thenReturn(testDividend);

        // Create the response from the request
        DividendDTO result = dividendService.createDividend(request);

        // Ensure that the dto is not empty
        assertNotNull(result);
        // Ensure that the amount matches
        assertEquals(BigDecimal.valueOf(0.25), result.getDividendPerShare());
        // Ensure that the repository saved once
        verify(dividendRepository, times(1)).save(any(Dividend.class));
        // Verify payment processing was triggered
        verify(dividendPaymentService, times(1)).processPaymentsForDividend(any());
        // Verify websocket notification
        verify(messagingTemplate, times(1))
                .convertAndSend((String) eq("/topic/dividends"), (Object) any());
    }

    // Test retrieving a dividend by id
    @Test
    void testGetDividendById_ReturnsCorrectDTO() {
        // Mock validation service
        when(dividendValidationService.validateDividendExists(dividendId))
                .thenReturn(testDividend);

        // Attempt to retrieve dividend
        DividendDTO result = dividendService.getDividendById(dividendId);

        // Ensure the dto is not empty
        assertNotNull(result);
        // Ensure that the dividendId matches
        assertEquals(dividendId, result.getDividendId());
    }

    // Test retrieving all dividends
    @Test
    void testGetAllDividends_ReturnsCorrectList() {
        // Mock repository findAll
        when(dividendRepository.findAll()).thenReturn(List.of(testDividend));

        // Store the list of dividends in dtos
        List<DividendDTO> result = dividendService.getAllDividends();

        // Verify that only one object is in the list
        assertEquals(1, result.size());
        // Verify dividendId matches
        assertEquals(dividendId, result.get(0).getDividendId());
    }

    // Test retrieving dividends by stock
    @Test
    void testGetDividendsByStock_ReturnsCorrectList() {
        // Mock repository behavior
        when(dividendRepository.findByStock_StockId(stockId))
                .thenReturn(List.of(testDividend));

        // Get dividends for stock
        List<DividendDTO> result = dividendService.getDividendsByStock(stockId);

        // Verify results
        assertEquals(1, result.size());
        assertEquals(stockId, result.get(0).getStockId());
    }

    // Test deleting a dividend
    @Test
    void testDeleteDividend_Success() {
        // Mock validation
        when(dividendValidationService.validateDividendExists(dividendId))
                .thenReturn(testDividend);

        // Delete dividend
        dividendService.deleteDividend(dividendId);

        // Verify deletion
        verify(dividendRepository, times(1)).delete(testDividend);
    }

    // Test duplicate dividend prevention
    @Test
    void testCreateDividend_DuplicateThrowsException() {
        // Create request
        DividendCreateRequest request = new DividendCreateRequest(
                stockId,
                BigDecimal.valueOf(0.25),
                LocalDateTime.now().plusDays(30)
        );

        // Mock duplicate exists
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(testStock));
        when(dividendRepository.existsByStockAndPayDate(any(), any())).thenReturn(true);

        // Expect exception
        assertThrows(RuntimeException.class, () -> {
            dividendService.createDividend(request);
        });
    }
}