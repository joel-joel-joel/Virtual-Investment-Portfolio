package com.joelcode.personalinvestmentportfoliotracker.services.portfolio;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PortfolioPerformanceServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private DividendPaymentCalculationService dividendPaymentCalculationService;

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private AccountValidationService accountValidationService;

    @Mock
    private DividendPaymentService dividendPaymentService;

    @Mock
    private UserValidationService userValidationService;

    @Mock
    private WebSocketController webSocketController;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PortfolioPerformanceServiceImpl portfolioPerformanceService;

    private UUID accountId;
    private UUID stockId;
    private UUID userId;
    private Account account;
    private Holding holding;
    private Stock stock;
    private User user;
    private HoldingDTO holdingDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        accountId = UUID.randomUUID();
        stockId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Initialize Stock
        stock = new Stock();
        stock.setStockId(stockId);
        stock.setStockCode("TEST");
        stock.setStockValue(BigDecimal.valueOf(60)); // Changed to 60 to match currentPrice in DTO

        // Initialize Holding entity
        holding = new Holding();
        holding.setStock(stock);
        holding.setQuantity(BigDecimal.valueOf(10));
        holding.setAverageCostBasis(BigDecimal.valueOf(50));
        holding.setTotalCostBasis(holding.getAverageCostBasis().multiply(holding.getQuantity()));
        holding.setUnrealizedGain(BigDecimal.valueOf(100)); // (60-50)*10 = 100
        holding.setRealizedGain(BigDecimal.valueOf(10));

        // Initialize HoldingDTO
        holdingDTO = new HoldingDTO();
        holdingDTO.setStockId(stockId);
        holdingDTO.setQuantity(BigDecimal.valueOf(10));
        holdingDTO.setAverageCostBasis(BigDecimal.valueOf(50));
        holdingDTO.setCurrentPrice(BigDecimal.valueOf(60));
        holdingDTO.setRealizedGain(BigDecimal.valueOf(10));

        // Initialize Account
        account = new Account();
        account.setAccountId(accountId);
        account.setAccountBalance(BigDecimal.valueOf(1000));

        // Initialize User
        user = new User();
        user.setUserId(userId);
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        user.setAccounts(accounts);

        // Mocks
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));
        when(accountValidationService.validateAccountExistsById(accountId)).thenReturn(account);
        when(holdingRepository.findByAccount_AccountId(accountId)).thenReturn(List.of(holding));
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(List.of(holdingDTO));
        when(dividendPaymentCalculationService.calculateTotalDividends(accountId)).thenReturn(BigDecimal.valueOf(25));
        when(dividendPaymentService.getDividendPaymentsForAccount(accountId)).thenReturn(List.of(
                new DividendPaymentDTO(accountId, BigDecimal.valueOf(25))
        ));
        when(userValidationService.validateUserExists(userId)).thenReturn(user);
    }

    @Test
    void testCalculatePortfolioPerformance() {
        PortfolioPerformanceDTO perf = portfolioPerformanceService.calculatePortfolioPerformance(accountId);

        assertNotNull(perf);
        assertEquals(accountId, perf.getAccountId());
        // Cash: 1000 + Holdings: 10 * 60 = 600 = 1600 total
        assertEquals(BigDecimal.valueOf(1600), perf.getTotalPortfolioValue());
        assertEquals(BigDecimal.valueOf(500), perf.getTotalCostBasis()); // 10 * 50
        assertEquals(BigDecimal.valueOf(10), perf.getTotalRealizedGain());
        assertEquals(BigDecimal.valueOf(100), perf.getTotalUnrealizedGain()); // (60-50)*10
        assertEquals(BigDecimal.valueOf(25), perf.getTotalDividends());
        assertEquals(BigDecimal.valueOf(1000), perf.getCashBalance());
        assertTrue(perf.getRoiPercentage().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetPerformanceForAccount_WithHoldings() {
        PortfolioPerformanceDTO perf = portfolioPerformanceService.getPerformanceForAccount(accountId);

        assertNotNull(perf);
        assertEquals(accountId, perf.getAccountId());
        assertTrue(perf.getTotalPortfolioValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetPerformanceForAccount_EmptyHoldings() {
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(new ArrayList<>());

        PortfolioPerformanceDTO perf = portfolioPerformanceService.getPerformanceForAccount(accountId);

        assertNotNull(perf);
        assertEquals(accountId, perf.getAccountId());
        assertEquals(account.getAccountBalance(), perf.getTotalPortfolioValue());
        assertEquals(BigDecimal.ZERO, perf.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, perf.getTotalUnrealizedGain());
        assertEquals(BigDecimal.ZERO, perf.getTotalRealizedGain());
    }

    @Test
    void testGetPerformanceForUser() {
        PortfolioPerformanceDTO perf = portfolioPerformanceService.getPerformanceForUser(userId);

        assertNotNull(perf);
        // Cash: 1000 + Holdings: 10 * 60 = 600 = 1600 total
        assertEquals(BigDecimal.valueOf(1600), perf.getTotalPortfolioValue());
        assertEquals(BigDecimal.valueOf(500), perf.getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(10), perf.getTotalRealizedGain());
        assertEquals(BigDecimal.valueOf(100), perf.getTotalUnrealizedGain());
    }

    @Test
    void testCreatePortfolioSnapshot() {
        portfolioPerformanceService.createPortfolioSnapshot(accountId);
        verify(snapshotRepository, times(1)).save(any());
        verify(messagingTemplate, times(1))
                .convertAndSend(anyString(), any(WebSocketController.PortfolioUpdateMessage.class));
    }
}