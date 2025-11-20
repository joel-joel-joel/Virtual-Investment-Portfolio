package com.joelcode.personalinvestmentportfoliotracker.services;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.*;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.*;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Testing holding service layer business logic
public class HoldingServiceImplTest {

    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private HoldingValidationService holdingValidationService;
    @Mock
    private AccountValidationService accountValidationService;
    @Mock
    private PriceHistoryServiceImpl priceHistoryService;
    @Mock
    private WebSocketController webSocketController;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private HoldingServiceImpl holdingService;

    private Holding testHolding;
    private Account testAccount;
    private Stock testStock;
    private UUID holdingId;
    private UUID accountId;
    private UUID stockId;

    // Set up test holding with mock values
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        holdingId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        stockId = UUID.randomUUID();

        testAccount = new Account();
        testAccount.setAccountId(accountId);

        testStock = new Stock();
        testStock.setStockId(stockId);
        testStock.setStockCode("AAPL");
        testStock.setStockValue(BigDecimal.valueOf(150.0));

        testHolding = new Holding();
        testHolding.setHoldingId(holdingId);
        testHolding.setAccount(testAccount);
        testHolding.setStock(testStock);
        testHolding.setQuantity(BigDecimal.valueOf(100));
        testHolding.setAverageCostBasis(BigDecimal.valueOf(120));
        testHolding.setTotalCostBasis(BigDecimal.valueOf(12000));
        testHolding.setRealizedGain(BigDecimal.ZERO);
    }

    // Test holding creation
    @Test
    void testCreateHolding_Success() {
        HoldingCreateRequest request = new HoldingCreateRequest(
                accountId,
                stockId,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(120),
                BigDecimal.valueOf(12000)
        );

        when(holdingValidationService.validateAccountExists(accountId)).thenReturn(testAccount);
        when(holdingValidationService.validateStockExists(stockId)).thenReturn(testStock);
        when(holdingRepository.save(any(Holding.class))).thenReturn(testHolding);

        HoldingDTO result = holdingService.createHolding(request);

        assertNotNull(result);
        verify(holdingRepository, times(1)).save(any(Holding.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    // Test retrieving holding by id
    @Test
    void testGetHoldingById_ReturnsCorrectDTO() {
        when(holdingValidationService.validateHoldingExists(holdingId)).thenReturn(testHolding);

        HoldingDTO result = holdingService.getHoldingById(holdingId);

        assertNotNull(result);
        assertEquals(holdingId, result.getHoldingId());
    }

    // Test retrieving all holdings
    @Test
    void testGetAllHoldings_ReturnsCorrectList() {
        when(holdingRepository.findAll()).thenReturn(List.of(testHolding));

        List<HoldingDTO> result = holdingService.getAllHoldings();

        assertEquals(1, result.size());
    }

    // Test updating holding
    @Test
    void testUpdateHolding_Success() {
        HoldingUpdateRequest request = new HoldingUpdateRequest();
        request.setQuantity(BigDecimal.valueOf(150));

        when(holdingValidationService.validateHoldingExists(holdingId)).thenReturn(testHolding);
        when(holdingRepository.save(any(Holding.class))).thenReturn(testHolding);

        HoldingDTO result = holdingService.updateHolding(holdingId, request);

        assertNotNull(result);
        verify(holdingRepository, times(1)).save(any(Holding.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    // Test deleting holding
    @Test
    void testDeleteHolding_Success() {
        when(holdingValidationService.validateHoldingExists(holdingId)).thenReturn(testHolding);

        holdingService.deleteHolding(holdingId);

        verify(holdingRepository, times(1)).delete(testHolding);
    }
}