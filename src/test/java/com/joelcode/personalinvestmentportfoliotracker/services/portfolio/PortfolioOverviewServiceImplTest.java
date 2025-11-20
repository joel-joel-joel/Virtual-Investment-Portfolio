package com.joelcode.personalinvestmentportfoliotracker.services.portfolio;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PortfolioOverviewServiceImplTest {

    @Mock
    private AccountValidationService accountValidationService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private DividendPaymentService dividendPaymentService;

    @Mock
    private UserValidationService userValidationService;

    @InjectMocks
    private PortfolioOverviewServiceImpl portfolioOverviewService;

    private UUID accountId;
    private UUID userId;
    private Account account;
    private User user;
    private HoldingDTO holdingDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Initialize account
        account = new Account();
        account.setAccountId(accountId);
        account.setAccountBalance(BigDecimal.valueOf(1000));

        // Initialize user
        user = new User();
        user.setUserId(userId);
        List<Account> accounts = new ArrayList<>();
        accounts.add(account);
        user.setAccounts(accounts);

        // Initialize HoldingDTO
        holdingDTO = new HoldingDTO();
        holdingDTO.setStockId(UUID.randomUUID());
        holdingDTO.setQuantity(BigDecimal.valueOf(10));
        holdingDTO.setAverageCostBasis(BigDecimal.valueOf(50));
        holdingDTO.setCurrentPrice(BigDecimal.valueOf(60));

        // Mocks
        when(accountValidationService.validateAccountExistsById(accountId)).thenReturn(account);
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(List.of(holdingDTO));
        when(dividendPaymentService.getDividendPaymentsForAccount(accountId)).thenReturn(List.of(
                new com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO(accountId, BigDecimal.valueOf(25))
        ));
        when(userValidationService.validateUserExists(userId)).thenReturn(user);
    }

    @Test
    void testGetPortfolioOverviewForAccount() {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForAccount(accountId);

        assertNotNull(overview);
        assertEquals(accountId, overview.getAccountId());
        assertEquals(BigDecimal.valueOf(1600), overview.getTotalPortfolioValue());
        assertTrue(overview.getTotalCostBasis().compareTo(BigDecimal.valueOf(500)) == 0);
        assertEquals(BigDecimal.valueOf(100), overview.getTotalUnrealizedGain());
        assertTrue(overview.getCashBalance().compareTo(BigDecimal.valueOf(1000)) == 0);
        assertEquals(1, overview.getHoldings().size());
    }

    @Test
    void testGetPortfolioOverviewForUser() {
        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForUser(userId);

        assertNotNull(overview);
        assertNull(overview.getAccountId()); // User-level overview has no single accountId
        assertEquals(BigDecimal.valueOf(1600), overview.getTotalPortfolioValue());
        assertTrue(overview.getTotalCostBasis().compareTo(BigDecimal.valueOf(500)) == 0);
        assertEquals(BigDecimal.valueOf(100), overview.getTotalUnrealizedGain());
        assertTrue(overview.getCashBalance().compareTo(BigDecimal.valueOf(1000)) == 0);
        assertEquals(1, overview.getHoldings().size());
    }

    @Test
    void testGetPortfolioOverviewForAccount_EmptyHoldings() {
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(new ArrayList<>());

        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForAccount(accountId);

        assertNotNull(overview);
        assertEquals(accountId, overview.getAccountId());
        assertEquals(BigDecimal.valueOf(1000), overview.getTotalPortfolioValue());
        assertEquals(BigDecimal.ZERO, overview.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, overview.getTotalUnrealizedGain());
        assertEquals(BigDecimal.ZERO, overview.getTotalRealizedGain());
        assertTrue(overview.getCashBalance().compareTo(BigDecimal.valueOf(1000)) == 0);
    }

    @Test
    void testGetPortfolioOverviewForUser_EmptyHoldings() {
        when(holdingService.getHoldingsForAccount(accountId)).thenReturn(new ArrayList<>());

        PortfolioOverviewDTO overview = portfolioOverviewService.getPortfolioOverviewForUser(userId);

        assertNotNull(overview);
        assertNull(overview.getAccountId());
        assertEquals(BigDecimal.valueOf(1000), overview.getTotalPortfolioValue());
        assertEquals(BigDecimal.ZERO, overview.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, overview.getTotalUnrealizedGain());
        assertEquals(BigDecimal.ZERO, overview.getTotalRealizedGain());
        assertTrue(overview.getCashBalance().compareTo(BigDecimal.valueOf(1000)) == 0);
    }
}
