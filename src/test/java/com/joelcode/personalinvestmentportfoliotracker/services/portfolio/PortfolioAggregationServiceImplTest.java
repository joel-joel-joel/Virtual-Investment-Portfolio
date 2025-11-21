package com.joelcode.personalinvestmentportfoliotracker.services.portfolio;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation.PortfolioAggregationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PortfolioAggregationServiceImplTest {

    private PortfolioAggregationServiceImpl service;

    private AccountService accountService;
    private HoldingService holdingService;
    private DividendPaymentCalculationService dividendPaymentCalculationService;
    private DividendPaymentService dividendPaymentService;
    private UserValidationService userValidationService;

    private UUID accountId;
    private UUID userId;

    @BeforeEach
    void setup() {
        accountService = mock(AccountService.class);
        holdingService = mock(HoldingService.class);
        dividendPaymentCalculationService = mock(DividendPaymentCalculationService.class);
        dividendPaymentService = mock(DividendPaymentService.class);
        userValidationService = mock(UserValidationService.class);

        dividendPaymentCalculationService = mock(DividendPaymentCalculationService.class);

        service = new PortfolioAggregationServiceImpl(
                accountService,
                holdingService,
                null, // holdingCalculationService, not needed for these tests
                dividendPaymentCalculationService, // <- pass the mock here
                null, // accountRepository
                null, // holdingRepository
                null, // allocationBreakdownService
                dividendPaymentService,
                userValidationService,
                null, // priceHistoryService
                null  // accountValidationService
        );

        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    // Helper for null-safe BigDecimal operations
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Test
    void testGetPortfolioOverview_nullProtectionAndCalculations() {
        // Setup account DTO
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAccountId(accountId);
        accountDTO.setCashBalance(null); // intentionally null to test protection
        when(accountService.getAccountById(accountId)).thenReturn(accountDTO);

        // Setup holdings
        List<HoldingDTO> holdings = new ArrayList<>();
        HoldingDTO h1 = new HoldingDTO();
        h1.setTotalCostBasis(BigDecimal.valueOf(1000));
        h1.setCurrentValue(BigDecimal.valueOf(1500));
        h1.setRealizedGain(BigDecimal.valueOf(200));
        h1.setUnrealizedGain(BigDecimal.valueOf(300));
        holdings.add(h1);

        HoldingDTO h2 = new HoldingDTO();
        h2.setTotalCostBasis(null); // test null protection
        h2.setCurrentValue(null);   // test null protection
        h2.setRealizedGain(null);   // test null protection
        h2.setUnrealizedGain(null); // test null protection
        holdings.add(h2);

        when(holdingService.getHoldingsByAccount(accountId)).thenReturn(holdings);

        // Setup dividends
        when(dividendPaymentCalculationService.calculateTotalDividends(accountId)).thenReturn(null); // test null protection

        PortfolioOverviewDTO overview = service.getPortfolioOverview(accountId);

        // Assertions with null protection
        assertEquals(BigDecimal.ZERO, safe(overview.getCashBalance()));
        assertEquals(BigDecimal.valueOf(1000), safe(overview.getTotalCostBasis()));
        assertEquals(BigDecimal.valueOf(300), safe(overview.getTotalUnrealizedGain()));
        assertEquals(BigDecimal.valueOf(200), safe(overview.getTotalRealizedGain()));
        assertEquals(BigDecimal.ZERO, safe(overview.getTotalDividends()));
        assertEquals(BigDecimal.valueOf(1500), safe(overview.getTotalPortfolioValue()));
    }

    @Test
    void testGetPortfolioOverview_emptyHoldingsAndZeroCash() {
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAccountId(accountId);
        accountDTO.setCashBalance(BigDecimal.ZERO);
        when(accountService.getAccountById(accountId)).thenReturn(accountDTO);

        when(holdingService.getHoldingsByAccount(accountId)).thenReturn(new ArrayList<>());
        when(dividendPaymentCalculationService.calculateTotalDividends(accountId)).thenReturn(BigDecimal.ZERO);

        PortfolioOverviewDTO overview = service.getPortfolioOverview(accountId);

        assertEquals(BigDecimal.ZERO, safe(overview.getCashBalance()));
        assertEquals(BigDecimal.ZERO, safe(overview.getTotalPortfolioValue()));
        assertEquals(BigDecimal.ZERO, safe(overview.getTotalCostBasis()));
        assertEquals(BigDecimal.ZERO, safe(overview.getTotalUnrealizedGain()));
        assertEquals(BigDecimal.ZERO, safe(overview.getTotalRealizedGain()));
        assertEquals(BigDecimal.ZERO, safe(overview.getTotalDividends()));
    }

}
