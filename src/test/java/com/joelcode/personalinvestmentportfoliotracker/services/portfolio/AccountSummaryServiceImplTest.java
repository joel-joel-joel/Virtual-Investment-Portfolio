package com.joelcode.personalinvestmentportfoliotracker.services.portfolio;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AccountSummaryDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.HoldingSummaryDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.summary.AccountSummaryServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountSummaryServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private StockService stockService;

    @Mock
    private DividendPaymentCalculationService dividendPaymentCalculationService;

    @Mock
    private UserValidationService userValidationService;

    @InjectMocks
    private AccountSummaryServiceImpl accountSummaryService;

    private UUID accountId;
    private UUID stockId;
    private UUID userId;
    private Account testAccount;
    private Holding testHolding;
    private Stock testStock;
    private User testUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        accountId = UUID.randomUUID();
        stockId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Initialize Stock
        testStock = new Stock();
        testStock.setStockId(stockId);
        testStock.setStockCode("TEST");

        // Initialize Holding
        testHolding = new Holding();
        testHolding.setStock(testStock);
        testHolding.setQuantity(BigDecimal.valueOf(10));
        testHolding.setAverageCostBasis(BigDecimal.valueOf(50));

        // Initialize Account
        testAccount = new Account();
        testAccount.setAccountId(accountId);
        testAccount.setAccountBalance(BigDecimal.valueOf(1000));

        // Initialize User
        testUser = new User();
        testUser.setUserId(userId);
        List<Account> accounts = new ArrayList<>();
        accounts.add(testAccount);
        testUser.setAccounts(accounts);

        // Link holdings repository
        when(holdingRepository.findByAccount_AccountId(accountId)).thenReturn(List.of(testHolding));
    }

    @Test
    void testGetAccountSummary_Success() {
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
        when(stockService.getCurrentPrice(stockId)).thenReturn(BigDecimal.valueOf(60));
        when(dividendPaymentCalculationService.calculateTotalDividends(accountId)).thenReturn(BigDecimal.valueOf(25));

        AccountSummaryDTO summary = accountSummaryService.getAccountSummary(accountId);

        assertNotNull(summary);
        assertEquals(accountId, summary.getAccountId());
        assertEquals(BigDecimal.valueOf(500), summary.getTotalCostBasis()); // 50 * 10
        assertEquals(BigDecimal.valueOf(600), summary.getTotalMarketValue()); // 60 * 10
        assertEquals(BigDecimal.valueOf(100), summary.getTotalUnrealizedGain()); // 600 - 500
        assertEquals(BigDecimal.valueOf(25), summary.getTotalDividends());
        assertEquals(BigDecimal.valueOf(1000), summary.getTotalCashBalance());

        List<HoldingSummaryDTO> holdings = summary.getHoldings();
        assertEquals(1, holdings.size());
        assertEquals(stockId, holdings.get(0).getStockId());
        assertEquals(BigDecimal.valueOf(10), holdings.get(0).getQuantity());
    }

    @Test
    void testGetAccountSummariesForUser_Success() {
        when(userValidationService.validateUserExists(userId)).thenReturn(testUser);
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(testAccount));
        when(stockService.getCurrentPrice(stockId)).thenReturn(BigDecimal.valueOf(60));
        when(dividendPaymentCalculationService.calculateTotalDividends(accountId)).thenReturn(BigDecimal.valueOf(25));

        List<AccountSummaryDTO> summaries = accountSummaryService.getAccountSummariesForUser(userId);

        assertEquals(1, summaries.size());
        AccountSummaryDTO summary = summaries.get(0);
        assertEquals(accountId, summary.getAccountId());
        assertEquals(BigDecimal.valueOf(1000), summary.getTotalCashBalance());
        assertEquals(1, summary.getHoldings().size());
        assertEquals(stockId, summary.getHoldings().get(0).getStockId());
    }
}
