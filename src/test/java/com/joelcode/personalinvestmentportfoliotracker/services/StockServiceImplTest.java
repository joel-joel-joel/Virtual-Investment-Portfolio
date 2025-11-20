package com.joelcode.personalinvestmentportfoliotracker.services;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.*;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.*;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Testing stock service layer business logic
public class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockValidationService stockValidationService;
    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Stock testStock;
    private UUID stockId;

    // Set up test stock with mock values
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        stockId = UUID.randomUUID();

        testStock = new Stock();
        testStock.setStockId(stockId);
        testStock.setStockCode("AAPL");
        testStock.setCompanyName("Apple Inc.");
        testStock.setStockValue(BigDecimal.valueOf(150.0));
    }

    // Test stock creation
    @Test
    void testCreateStock_Success() {
        StockCreateRequest request = new StockCreateRequest(
                stockId,
                "Apple Inc.",
                "AAPL",
                BigDecimal.valueOf(150.0)
        );

        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);

        StockDTO result = stockService.createStock(request);

        assertNotNull(result);
        assertEquals("AAPL", result.getStockCode());
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    // Test retrieving stock by id
    @Test
    void testGetStockById_ReturnsCorrectDTO() {
        when(stockValidationService.validateStockExists(stockId)).thenReturn(testStock);

        StockDTO result = stockService.getStockById(stockId);

        assertNotNull(result);
        assertEquals(stockId, result.getStockId());
    }

    // Test retrieving all stocks
    @Test
    void testGetAllStocks_ReturnsCorrectList() {
        when(stockRepository.findAll()).thenReturn(List.of(testStock));

        List<StockDTO> result = stockService.getAllStocks();

        assertEquals(1, result.size());
    }

    // Test updating stock
    @Test
    void testUpdateStock_Success() {
        StockUpdateRequest request = new StockUpdateRequest(
                "AAPL",
                "Apple Inc.",
                stockId,
                BigDecimal.valueOf(160.0)
        );

        when(stockValidationService.validateStockExists(stockId)).thenReturn(testStock);
        when(stockRepository.save(any(Stock.class))).thenReturn(testStock);

        StockDTO result = stockService.updateStock(stockId, request);

        assertNotNull(result);
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    // Test deleting stock
    @Test
    void testDeleteStock_Success() {
        when(stockValidationService.validateStockExists(stockId)).thenReturn(testStock);

        stockService.deleteStock(stockId);

        verify(stockRepository, times(1)).delete(testStock);
    }
}