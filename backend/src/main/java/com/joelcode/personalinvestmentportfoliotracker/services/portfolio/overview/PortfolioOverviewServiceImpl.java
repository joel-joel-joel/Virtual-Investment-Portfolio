package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class PortfolioOverviewServiceImpl implements PortfolioOverviewService {

    // Define key fields
    @Autowired
    private AccountValidationService accountValidationService;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private DividendPaymentService dividendPaymentService;

    @Autowired
    private AllocationBreakdownService allocationService;

    @Autowired
    private UserValidationService userValidationService;


    // Constructor
    public PortfolioOverviewServiceImpl(AccountValidationService accountValidationService,
                                        HoldingService holdingService, DividendPaymentService dividendPaymentService,
                                        AllocationBreakdownService allocationService,
                                        UserValidationService userValidationService) {
        this.accountValidationService = accountValidationService;
        this.holdingService = holdingService;
        this.dividendPaymentService = dividendPaymentService;
        this.allocationService = allocationService;
        this.userValidationService = userValidationService;
    }


    // Interface functions

    // Get overview for account
    @Override
    public PortfolioOverviewDTO getPortfolioOverviewForAccount(UUID accountId) {
        try {
            System.out.println("=".repeat(70));
            System.out.println("📊 PortfolioOverviewService: Getting overview for account: " + accountId);

            // Validate account exists
            System.out.println("🔍 Validating account...");
            Account account = accountValidationService.validateAccountExistsById(accountId);
            System.out.println("✅ Account found: " + account.getAccountName());

            // Get holdings as DTOs
            System.out.println("🔄 Fetching holdings...");
            List<HoldingDTO> holdings = holdingService.getHoldingsForAccount(accountId);
            System.out.println("✅ Got " + holdings.size() + " holdings");

            // Calculate holdings value
            System.out.println("💰 Calculating holdings value...");
            BigDecimal holdingsValue = holdings.stream()
                    .map(h -> {
                        BigDecimal price = safe(h.getCurrentPrice());
                        BigDecimal qty = safe(h.getQuantity());
                        BigDecimal value = price.multiply(qty);
                        System.out.println("  - " + h.getStockSymbol() + ": " + qty + " @ " + price + " = " + value);
                        return value;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("✅ Holdings value: " + holdingsValue);

            // Calculate total cost basis
            System.out.println("💼 Calculating cost basis...");
            BigDecimal totalCostBasis = holdings.stream()
                    .map(h -> safe(h.getAverageCostBasis()).multiply(safe(h.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("✅ Total cost basis: " + totalCostBasis);

            // Calculate unrealized gain
            System.out.println("📈 Calculating unrealized gain...");
            BigDecimal totalUnrealizedGain = holdingsValue.subtract(totalCostBasis);
            System.out.println("✅ Unrealized gain: " + totalUnrealizedGain);

            // Calculate realized gain
            System.out.println("✔️ Calculating realized gain...");
            BigDecimal totalRealizedGain = holdings.stream()
                    .map(h -> safe(h.getRealizedGain()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("✅ Realized gain: " + totalRealizedGain);

            // Get cash balance
            System.out.println("💵 Getting cash balance...");
            BigDecimal cashBalance = safe(account.getAccountBalance());
            System.out.println("✅ Cash balance: " + cashBalance);

            // Calculate total portfolio value
            System.out.println("🎯 Calculating total portfolio value...");
            BigDecimal totalPortfolioValue = holdingsValue.add(cashBalance);
            System.out.println("✅ Total portfolio value: " + totalPortfolioValue);

            // Calculate total dividends
            System.out.println("💸 Fetching dividends...");
            BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                    .map(dto -> safe(dto.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            System.out.println("✅ Total dividends: " + totalDividends);

            System.out.println("=".repeat(70));
            System.out.println("✅ PortfolioOverviewService: Successfully created overview");
            System.out.println("=".repeat(70));

            return new PortfolioOverviewDTO(
                    account.getUserid(),
                    account.getAccountId(),
                    totalPortfolioValue,
                    holdingsValue,
                    totalCostBasis,
                    totalUnrealizedGain,
                    totalRealizedGain,
                    totalDividends,
                    cashBalance,
                    holdings
            );
        } catch (Exception e) {
            System.err.println("=".repeat(70));
            System.err.println("❌ PortfolioOverviewService: ERROR");
            System.err.println("Exception: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("=".repeat(70));
            e.printStackTrace();
            System.err.println("=".repeat(70));
            throw e;
        }
    }
    // Get portfolio overview on user level
    @Override
    public PortfolioOverviewDTO getPortfolioOverviewForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal holdingsValue = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        BigDecimal cashBalance = BigDecimal.ZERO;

        List<HoldingDTO> allHoldings = new ArrayList<>();

        // Aggregate across all accounts
        for (Account account : user.getAccounts()) {
            PortfolioOverviewDTO accountOverview = getPortfolioOverviewForAccount(account.getAccountId());

            totalPortfolioValue = totalPortfolioValue.add(accountOverview.getTotalPortfolioValue());
            holdingsValue = holdingsValue.add(accountOverview.getHoldingsValue());
            totalCostBasis = totalCostBasis.add(accountOverview.getTotalCostBasis());
            totalUnrealizedGain = totalUnrealizedGain.add(accountOverview.getTotalUnrealizedGain());
            totalRealizedGain = totalRealizedGain.add(accountOverview.getTotalRealizedGain());
            totalDividends = totalDividends.add(accountOverview.getTotalDividends());
            cashBalance = cashBalance.add(accountOverview.getCashBalance());

            // Combine holdings
            if (accountOverview.getHoldings() != null) {
                allHoldings.addAll(accountOverview.getHoldings());
            }
        }

        return new PortfolioOverviewDTO(
                userId,
                null,
                totalPortfolioValue,
                holdingsValue,
                totalCostBasis,
                totalUnrealizedGain,
                totalRealizedGain,
                totalDividends,
                cashBalance,
                allHoldings
        );
    }

    // Helper to safely return BigDecimal or ZERO if null
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}