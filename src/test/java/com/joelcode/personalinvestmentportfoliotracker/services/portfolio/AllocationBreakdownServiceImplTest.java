package com.joelcode.personalinvestmentportfoliotracker.services.portfolio;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Testing allocation breakdown service layer business logic
public class AllocationBreakdownServiceImplTest {

    // Define mock key fields
    @Mock
    private HoldingCalculationService holdingCalcService;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private UserValidationService userValidationService;

    @InjectMocks
    private AllocationBreakdownServiceImpl allocationService;

    private UUID accountId;
    private UUID userId;

    private Holding holding1;
    private Holding holding2;

    // Setup sample holdings for testing
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Holding 1
        holding1 = new Holding();
        Stock stock1 = new Stock();
        stock1.setStockCode("AAPL");
        holding1.setStock(stock1);

        // Holding 2
        holding2 = new Holding();
        Stock stock2 = new Stock();
        stock2.setStockCode("MSFT");
        holding2.setStock(stock2);
    }

    // Test allocation when holdings exist
    @Test
    void testGetAllocationForAccount_ReturnsCorrectAllocations() {

        // Prepare test holdings
        List<Holding> holdings = List.of(holding1, holding2);

        when(holdingRepository.getHoldingsEntitiesByAccount(accountId))
                .thenReturn(holdings);

        // Mock calculated values
        when(holdingCalcService.calculateCurrentValue(holding1))
                .thenReturn(BigDecimal.valueOf(600));

        when(holdingCalcService.calculateCurrentValue(holding2))
                .thenReturn(BigDecimal.valueOf(400));

        // Call service
        List<AllocationBreakdownDTO> result = allocationService.getAllocationForAccount(accountId);

        // Run checks
        assertNotNull(result);
        assertEquals(2, result.size());

        AllocationBreakdownDTO aapl = result.stream()
                .filter(r -> r.getStockCode().equals("AAPL"))
                .findFirst().orElse(null);

        AllocationBreakdownDTO msft = result.stream()
                .filter(r -> r.getStockCode().equals("MSFT"))
                .findFirst().orElse(null);

        assertNotNull(aapl);
        assertNotNull(msft);

        // AAPL = 600 / 1000 = 60%
        assertEquals(BigDecimal.valueOf(60.00).setScale(2), aapl.getPercentage());

        // MSFT = 400 / 1000 = 40%
        assertEquals(BigDecimal.valueOf(40.00).setScale(2), msft.getPercentage());
    }

    // Test empty holdings for account
    @Test
    void testGetAllocationForAccount_EmptyHoldings_ReturnsEmptyList() {

        when(holdingRepository.getHoldingsEntitiesByAccount(accountId))
                .thenReturn(new ArrayList<>());

        List<AllocationBreakdownDTO> result = allocationService.getAllocationForAccount(accountId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Test zero-value holdings return zero-percent allocations
    @Test
    void testGetAllocationForAccount_ZeroTotalValue_ReturnsZeroPercent() {

        List<Holding> holdings = List.of(holding1);

        when(holdingRepository.getHoldingsEntitiesByAccount(accountId))
                .thenReturn(holdings);

        // Mock current value = 0
        when(holdingCalcService.calculateCurrentValue(any(Holding.class)))
                .thenReturn(BigDecimal.ZERO);

        List<AllocationBreakdownDTO> result = allocationService.getAllocationForAccount(accountId);

        assertEquals(1, result.size());
        assertEquals(BigDecimal.ZERO, result.get(0).getPercentage());
    }

    // Test user-level allocation across all accounts
    @Test
    void testGetAllocationForUser_ReturnsCombinedAllocations() {

        // Create user
        User user = new User();
        user.setUserId(userId);

        Account account = new Account();
        UUID accId = UUID.randomUUID();
        account.setAccountId(accId);

        user.setAccounts(List.of(account));

        when(userValidationService.validateUserExists(userId))
                .thenReturn(user);

        // Mock underlying account allocation call
        when(holdingRepository.getHoldingsEntitiesByAccount(accId))
                .thenReturn(List.of(holding1));

        when(holdingCalcService.calculateCurrentValue(holding1))
                .thenReturn(BigDecimal.valueOf(500));

        List<AllocationBreakdownDTO> result = allocationService.getAllocationForUser(userId);

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getStockCode());
    }
}
