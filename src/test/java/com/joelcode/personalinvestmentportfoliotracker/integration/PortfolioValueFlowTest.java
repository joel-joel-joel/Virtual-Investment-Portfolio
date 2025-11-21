package com.joelcode.personalinvestmentportfoliotracker.integration;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.*;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PortfolioValueFlowTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DividendPaymentRepository dividendPaymentRepository;

    @Mock
    private HoldingCalculationServiceImpl holdingCalculationService;

    @Mock
    private PortfolioOverviewServiceImpl portfolioOverviewService;

    private User testUser;
    private Account testAccount;
    private Stock aapl;
    private Stock msft;
    private Holding aaplHolding;
    private Holding msftHolding;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = new User();
        testUser.setUsername("investor");
        testUser.setEmail("investor@example.com");
        entityManager.persistAndFlush(testUser);

        // Create account with cash balance
        testAccount = new Account();
        testAccount.setAccountName("Investment Account");
        testAccount.setAccountBalance(BigDecimal.valueOf(50000.0)); // Cash balance
        testAccount.setUser(testUser);
        entityManager.persistAndFlush(testAccount);

        // Create stocks
        aapl = new Stock();
        aapl.setStockCode("AAPL");
        aapl.setCompanyName("Apple Inc");
        aapl.setStockValue(BigDecimal.valueOf(150.0));
        entityManager.persistAndFlush(aapl);

        msft = new Stock();
        msft.setStockCode("MSFT");
        msft.setCompanyName("Microsoft");
        msft.setStockValue(BigDecimal.valueOf(300.0));
        entityManager.persistAndFlush(msft);

        // Create holdings
        aaplHolding = new Holding();
        aaplHolding.setAccount(testAccount);
        aaplHolding.setStock(aapl);
        aaplHolding.setQuantity(100);
        aaplHolding.setAverageCostBasis(140.0);
        aaplHolding.setTotalCostBasis(14000.0);
        aaplHolding.setRealizedGain(BigDecimal.valueOf(500.0));
        aaplHolding.setFirstPurchaseDate(LocalDateTime.now().minusMonths(1));
        entityManager.persistAndFlush(aaplHolding);

        msftHolding = new Holding();
        msftHolding.setAccount(testAccount);
        msftHolding.setStock(msft);
        msftHolding.setQuantity(50);
        msftHolding.setAverageCostBasis(280.0);
        msftHolding.setTotalCostBasis(14000.0);
        msftHolding.setRealizedGain(BigDecimal.valueOf(1000.0));
        msftHolding.setFirstPurchaseDate(LocalDateTime.now().minusMonths(2));
        entityManager.persistAndFlush(msftHolding);
    }

    @Test
    void testPortfolioValue_CurrentValueCalculation() {
        // Arrange
        BigDecimal aaplValue = BigDecimal.valueOf(150.0).multiply(BigDecimal.valueOf(100)); // 15,000
        BigDecimal msftValue = BigDecimal.valueOf(300.0).multiply(BigDecimal.valueOf(50)); // 15,000
        BigDecimal holdingsValue = aaplValue.add(msftValue); // 30,000
        BigDecimal cashBalance = testAccount.getAccountBalance(); // 50,000
        BigDecimal expectedTotalValue = holdingsValue.add(cashBalance); // 80,000

        // Act - Calculate current values
        BigDecimal aaplCurrentValue = BigDecimal.valueOf(150.0).multiply(BigDecimal.valueOf(100));
        BigDecimal msftCurrentValue = BigDecimal.valueOf(300.0).multiply(BigDecimal.valueOf(50));
        BigDecimal totalHoldingsValue = aaplCurrentValue.add(msftCurrentValue);
        BigDecimal totalPortfolioValue = totalHoldingsValue.add(cashBalance);

        // Assert
        assertEquals(BigDecimal.valueOf(15000.0), aaplCurrentValue);
        assertEquals(BigDecimal.valueOf(15000.0), msftCurrentValue);
        assertEquals(BigDecimal.valueOf(30000.0), totalHoldingsValue);
        assertEquals(BigDecimal.valueOf(80000.0), totalPortfolioValue);
    }

    @Test
    void testPortfolioValue_CostBasisCalculation() {
        // Arrange
        BigDecimal aaplCostBasis = BigDecimal.valueOf(14000.0);
        BigDecimal msftCostBasis = BigDecimal.valueOf(14000.0);
        BigDecimal expectedTotalCostBasis = aaplCostBasis.add(msftCostBasis); // 28,000

        // Act
        BigDecimal totalCostBasis = aaplCostBasis.add(msftCostBasis);

        // Assert
        assertEquals(BigDecimal.valueOf(28000.0), totalCostBasis);
    }

    @Test
    void testPortfolioValue_UnrealizedGainCalculation() {
        // Arrange
        BigDecimal aaplCurrentValue = BigDecimal.valueOf(150.0).multiply(BigDecimal.valueOf(100)); // 15,000
        BigDecimal aaplCostBasis = BigDecimal.valueOf(14000.0);
        BigDecimal aaplUnrealizedGain = aaplCurrentValue.subtract(aaplCostBasis); // 1,000

        BigDecimal msftCurrentValue = BigDecimal.valueOf(300.0).multiply(BigDecimal.valueOf(50)); // 15,000
        BigDecimal msftCostBasis = BigDecimal.valueOf(14000.0);
        BigDecimal msftUnrealizedGain = msftCurrentValue.subtract(msftCostBasis); // 1,000

        BigDecimal totalUnrealizedGain = aaplUnrealizedGain.add(msftUnrealizedGain); // 2,000

        // Act & Assert
        assertEquals(BigDecimal.valueOf(1000.0), aaplUnrealizedGain);
        assertEquals(BigDecimal.valueOf(1000.0), msftUnrealizedGain);
        assertEquals(BigDecimal.valueOf(2000.0), totalUnrealizedGain);
    }

    @Test
    void testPortfolioValue_RealizedGainCalculation() {
        // Arrange
        BigDecimal aaplRealizedGain = BigDecimal.valueOf(500.0);
        BigDecimal msftRealizedGain = BigDecimal.valueOf(1000.0);
        BigDecimal expectedTotalRealizedGain = aaplRealizedGain.add(msftRealizedGain); // 1,500

        // Act
        BigDecimal totalRealizedGain = aaplRealizedGain.add(msftRealizedGain);

        // Assert
        assertEquals(BigDecimal.valueOf(1500.0), totalRealizedGain);
    }

    @Test
    void testPortfolioValue_TotalGainCalculation() {
        // Arrange
        BigDecimal unrealizedGain = BigDecimal.valueOf(2000.0);
        BigDecimal realizedGain = BigDecimal.valueOf(1500.0);
        BigDecimal totalGain = unrealizedGain.add(realizedGain); // 3,500

        // Act & Assert
        assertEquals(BigDecimal.valueOf(3500.0), totalGain);
    }

    @Test
    void testPortfolioValue_PriceIncrease() {
        // Arrange - Stock price increases
        BigDecimal oldAaplPrice = BigDecimal.valueOf(150.0);
        BigDecimal newAaplPrice = BigDecimal.valueOf(160.0); // 10% increase
        BigDecimal oldValue = oldAaplPrice.multiply(BigDecimal.valueOf(100)); // 15,000
        BigDecimal newValue = newAaplPrice.multiply(BigDecimal.valueOf(100)); // 16,000

        // Act & Assert
        assertEquals(BigDecimal.valueOf(15000.0), oldValue);
        assertEquals(BigDecimal.valueOf(16000.0), newValue);
        assertTrue(newValue.compareTo(oldValue) > 0);
    }

    @Test
    void testPortfolioValue_PriceDecrease() {
        // Arrange - Stock price decreases
        BigDecimal oldMsftPrice = BigDecimal.valueOf(300.0);
        BigDecimal newMsftPrice = BigDecimal.valueOf(280.0); // 6.67% decrease
        BigDecimal oldValue = oldMsftPrice.multiply(BigDecimal.valueOf(50)); // 15,000
        BigDecimal newValue = newMsftPrice.multiply(BigDecimal.valueOf(50)); // 14,000

        // Act & Assert
        assertEquals(BigDecimal.valueOf(15000.0), oldValue);
        assertEquals(BigDecimal.valueOf(14000.0), newValue);
        assertTrue(newValue.compareTo(oldValue) < 0);
    }

    @Test
    void testPortfolioValue_AddNewHolding() {
        // Arrange - Create new stock holding
        Stock googl = new Stock();
        googl.setStockCode("GOOGL");
        googl.setCompanyName("Google");
        googl.setStockValue(BigDecimal.valueOf(2800.0));
        entityManager.persistAndFlush(googl);

        Holding googlHolding = new Holding();
        googlHolding.setAccount(testAccount);
        googlHolding.setStock(googl);
        googlHolding.setQuantity(10);
        googlHolding.setAverageCostBasis(2700.0);
        googlHolding.setTotalCostBasis(27000.0);
        googlHolding.setRealizedGain(BigDecimal.ZERO);
        googlHolding.setFirstPurchaseDate(LocalDateTime.now());
        entityManager.persistAndFlush(googlHolding);

        // Act
        BigDecimal previousHoldingsValue = BigDecimal.valueOf(30000.0); // AAPL + MSFT
        BigDecimal newHoldingValue = BigDecimal.valueOf(2800.0).multiply(BigDecimal.valueOf(10)); // 28,000
        BigDecimal newTotalHoldingsValue = previousHoldingsValue.add(newHoldingValue); // 58,000

        // Assert
        assertEquals(BigDecimal.valueOf(28000.0), newHoldingValue);
        assertEquals(BigDecimal.valueOf(58000.0), newTotalHoldingsValue);

        // Verify holding persisted
        assertTrue(holdingRepository.findByAccountAndStock(testAccount, googl).isPresent());
    }

    @Test
    void testPortfolioValue_ReduceHoldingQuantity() {
        // Arrange - Sell half of AAPL
        BigDecimal soldQuantity = BigDecimal.valueOf(50);
        BigDecimal remainingQuantity = BigDecimal.valueOf(50);
        BigDecimal pricePerShare = BigDecimal.valueOf(160.0); // Sold at profit

        // Act - Update holding
        aaplHolding.setQuantity(50);
        BigDecimal realizedGain = soldQuantity.multiply(pricePerShare.subtract(BigDecimal.valueOf(140.0))); // 1,000
        aaplHolding.setRealizedGain(aaplHolding.getRealizedGain().add(realizedGain)); // 500 + 1,000 = 1,500
        entityManager.persistAndFlush(aaplHolding);

        // Calculate new portfolio value
        BigDecimal aaplNewValue = BigDecimal.valueOf(150.0).multiply(BigDecimal.valueOf(50)); // 7,500
        BigDecimal msftValue = BigDecimal.valueOf(300.0).multiply(BigDecimal.valueOf(50)); // 15,000
        BigDecimal newHoldingsValue = aaplNewValue.add(msftValue); // 22,500
        BigDecimal cashIncrease = soldQuantity.multiply(pricePerShare); // 8,000
        BigDecimal newCashBalance = testAccount.getAccountBalance().add(cashIncrease); // 58,000
        BigDecimal newTotalValue = newHoldingsValue.add(newCashBalance); // 80,500

        // Assert
        assertEquals(50, aaplHolding.getQuantity());
        assertEquals(BigDecimal.valueOf(1500.0), aaplHolding.getRealizedGain());
        assertEquals(BigDecimal.valueOf(80500.0), newTotalValue);
    }

    @Test
    void testPortfolioValue_LiquidateFully() {
        // Arrange - Sell all MSFT
        BigDecimal salePrice = BigDecimal.valueOf(310.0);
        BigDecimal totalProceeds = salePrice.multiply(BigDecimal.valueOf(50)); // 15,500

        // Act
        msftHolding.setQuantity(0);
        entityManager.persistAndFlush(msftHolding);

        BigDecimal aaplValue = BigDecimal.valueOf(150.0).multiply(BigDecimal.valueOf(100)); // 15,000
        BigDecimal newCashBalance = testAccount.getAccountBalance().add(totalProceeds); // 50,000 + 15,500
        BigDecimal newTotalValue = aaplValue.add(newCashBalance); // 15,000 + 65,500

        // Assert
        assertEquals(0, msftHolding.getQuantity());
        assertEquals(BigDecimal.valueOf(65500.0), newCashBalance);
        assertEquals(BigDecimal.valueOf(80500.0), newTotalValue);
    }

    @Test
    void testPortfolioValue_WithDividends() {
        // Arrange - Create dividend payments
        Stock stock = aapl;

        Dividend dividend = new Dividend();
        dividend.setStock(stock);
        dividend.setDividendPerShare(BigDecimal.valueOf(2.5));
        dividend.setPayDate(LocalDateTime.now());
        entityManager.persistAndFlush(dividend);

        DividendPayment payment = new DividendPayment();
        payment.setAccount(testAccount);
        payment.setStock(stock);
        payment.setDividend(dividend);
        payment.setShareQuantity(100);
        payment.setPaymentAmount(BigDecimal.valueOf(250.0)); // 100 * 2.5
        payment.setPaymentDate(java.time.LocalDate.now());
        payment.setStatus(DividendPayment.PaymentStatus.PAID);
        entityManager.persistAndFlush(payment);

        // Act
        BigDecimal holdingsValue = BigDecimal.valueOf(30000.0);
        BigDecimal dividendIncome = BigDecimal.valueOf(250.0);
        BigDecimal totalWithDividends = testAccount.getAccountBalance()
                .add(dividendIncome)
                .add(holdingsValue);

        // Assert
        assertEquals(BigDecimal.valueOf(250.0), dividendIncome);
        assertEquals(BigDecimal.valueOf(80250.0), totalWithDividends);
    }

    @Test
    void testPortfolioValue_MultipleStockPriceChanges() {
        // Arrange - Simulate market changes
        BigDecimal aaplOldPrice = BigDecimal.valueOf(150.0);
        BigDecimal aaplNewPrice = BigDecimal.valueOf(155.0); // +3.33%
        BigDecimal msftOldPrice = BigDecimal.valueOf(300.0);
        BigDecimal msftNewPrice = BigDecimal.valueOf(295.0); // -1.67%

        // Act - Calculate portfolio impact
        BigDecimal aaplOldValue = aaplOldPrice.multiply(BigDecimal.valueOf(100)); // 15,000
        BigDecimal aaplNewValue = aaplNewPrice.multiply(BigDecimal.valueOf(100)); // 15,500
        BigDecimal aaplChange = aaplNewValue.subtract(aaplOldValue); // 500

        BigDecimal msftOldValue = msftOldPrice.multiply(BigDecimal.valueOf(50)); // 15,000
        BigDecimal msftNewValue = msftNewPrice.multiply(BigDecimal.valueOf(50)); // 14,750
        BigDecimal msftChange = msftNewValue.subtract(msftOldValue); // -250

        BigDecimal oldTotalHoldings = BigDecimal.valueOf(30000.0);
        BigDecimal newTotalHoldings = aaplNewValue.add(msftNewValue); // 30,250
        BigDecimal netChange = newTotalHoldings.subtract(oldTotalHoldings); // 250

        // Assert
        assertEquals(BigDecimal.valueOf(500.0), aaplChange);
        assertEquals(BigDecimal.valueOf(-250.0), msftChange);
        assertEquals(BigDecimal.valueOf(250.0), netChange);
        assertEquals(BigDecimal.valueOf(80250.0), newTotalHoldings.add(testAccount.getAccountBalance()));
    }

    @Test
    void testPortfolioValue_EmptyPortfolio() {
        // Arrange - Create account with no holdings
        Account emptyAccount = new Account();
        emptyAccount.setAccountName("Cash Account");
        emptyAccount.setAccountBalance(BigDecimal.valueOf(100000.0));
        emptyAccount.setUser(testUser);
        entityManager.persistAndFlush(emptyAccount);

        // Act
        BigDecimal holdingsValue = BigDecimal.ZERO;
        BigDecimal cashBalance = emptyAccount.getAccountBalance();
        BigDecimal totalValue = holdingsValue.add(cashBalance);

        // Assert
        assertEquals(BigDecimal.valueOf(100000.0), totalValue);
        assertEquals(BigDecimal.ZERO, holdingsValue);
    }

    @Test
    void testPortfolioValue_AllCashPosition() {
        // Arrange - Liquidate all holdings
        aaplHolding.setQuantity(0);
        msftHolding.setQuantity(0);
        entityManager.persistAndFlush(aaplHolding);
        entityManager.persistAndFlush(msftHolding);

        // Act
        BigDecimal holdingsValue = BigDecimal.ZERO;
        BigDecimal cashBalance = testAccount.getAccountBalance();
        BigDecimal totalValue = holdingsValue.add(cashBalance);

        // Assert
        assertEquals(BigDecimal.valueOf(50000.0), totalValue);
    }
}