package com.joelcode.personalinvestmentportfoliotracker;

import com.joelcode.personalinvestmentportfoliotracker.repositories.*;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot.PortfolioSnapshotService;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
import com.joelcode.personalinvestmentportfoliotracker.services.transaction.TransactionService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance.PortfolioPerformanceService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.summary.AccountSummaryService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation.PortfolioAggregationService;
import com.joelcode.personalinvestmentportfoliotracker.services.utility.SearchService;
import com.joelcode.personalinvestmentportfoliotracker.jwt.JwtTokenProvider;
import org.mockito.Mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {

    // Repository Mocks
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private DividendRepository dividendRepository;

    @Mock
    private DividendPaymentRepository dividendPaymentRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private PortfolioSnapshotRepository portfolioSnapshotRepository;

    @Mock
    private UserRepository userRepository;

    // Service Mocks
    @Mock
    private AccountService accountService;

    @Mock
    private StockService stockService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private DividendService dividendService;

    @Mock
    private DividendPaymentService dividendPaymentService;

    @Mock
    private PriceHistoryService priceHistoryService;

    @Mock
    private PortfolioSnapshotService portfolioSnapshotService;

    @Mock
    private UserService userService;

    @Mock
    private PortfolioOverviewService portfolioOverviewService;

    @Mock
    private PortfolioPerformanceService portfolioPerformanceService;

    @Mock
    private AccountSummaryService accountSummaryService;

    @Mock
    private AllocationBreakdownService allocationBreakdownService;

    @Mock
    private PortfolioAggregationService portfolioAggregationService;

    @Mock
    private SearchService searchService;

    // Security Mocks
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Bean definitions for test purposes
    @Bean
    public AccountRepository testAccountRepository() {
        return accountRepository;
    }

    @Bean
    public StockRepository testStockRepository() {
        return stockRepository;
    }

    @Bean
    public TransactionRepository testTransactionRepository() {
        return transactionRepository;
    }

    @Bean
    public HoldingRepository testHoldingRepository() {
        return holdingRepository;
    }

    @Bean
    public DividendRepository testDividendRepository() {
        return dividendRepository;
    }

    @Bean
    public DividendPaymentRepository testDividendPaymentRepository() {
        return dividendPaymentRepository;
    }

    @Bean
    public PriceHistoryRepository testPriceHistoryRepository() {
        return priceHistoryRepository;
    }

    @Bean
    public PortfolioSnapshotRepository testPortfolioSnapshotRepository() {
        return portfolioSnapshotRepository;
    }

    @Bean
    public UserRepository testUserRepository() {
        return userRepository;
    }

    @Bean
    public AccountService testAccountService() {
        return accountService;
    }

    @Bean
    public StockService testStockService() {
        return stockService;
    }

    @Bean
    public TransactionService testTransactionService() {
        return transactionService;
    }

    @Bean
    public HoldingService testHoldingService() {
        return holdingService;
    }

    @Bean
    public DividendService testDividendService() {
        return dividendService;
    }

    @Bean
    public DividendPaymentService testDividendPaymentService() {
        return dividendPaymentService;
    }

    @Bean
    public PriceHistoryService testPriceHistoryService() {
        return priceHistoryService;
    }

    @Bean
    public PortfolioSnapshotService testPortfolioSnapshotService() {
        return portfolioSnapshotService;
    }

    @Bean
    public UserService testUserService() {
        return userService;
    }

    @Bean
    public PortfolioOverviewService testPortfolioOverviewService() {
        return portfolioOverviewService;
    }

    @Bean
    public PortfolioPerformanceService testPortfolioPerformanceService() {
        return portfolioPerformanceService;
    }

    @Bean
    public AccountSummaryService testAccountSummaryService() {
        return accountSummaryService;
    }

    @Bean
    public AllocationBreakdownService testAllocationBreakdownService() {
        return allocationBreakdownService;
    }

    @Bean
    public PortfolioAggregationService testPortfolioAggregationService() {
        return portfolioAggregationService;
    }

    @Bean
    public SearchService testSearchService() {
        return searchService;
    }

    @Bean
    public AuthenticationManager testAuthenticationManager() {
        return authenticationManager;
    }

    @Bean
    public JwtTokenProvider testJwtTokenProvider() {
        return jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder testPasswordEncoder() {
        return passwordEncoder;
    }
}