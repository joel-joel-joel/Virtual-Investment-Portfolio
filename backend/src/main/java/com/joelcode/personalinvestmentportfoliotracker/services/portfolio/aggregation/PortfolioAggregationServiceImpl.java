package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioAggregationDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!test")
public class PortfolioAggregationServiceImpl implements PortfolioAggregationService {

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final AccountRepository accountRepository;
    private final HoldingCalculationService holdingCalculationService;
    private final HoldingRepository holdingRepository;
    private final DividendPaymentService dividendPaymentService;
    private final AllocationBreakdownService allocationBreakdownService;
    private final UserValidationService userValidationService;
    private final PriceHistoryService priceHistoryService;
    private final AccountValidationService accountValidationService;


    // Constructor
    public PortfolioAggregationServiceImpl(AccountService accountService, HoldingService holdingService,
                                           HoldingCalculationService holdingCalculationService,
                                           AccountRepository accountRepository,
                                           HoldingRepository holdingRepository,
                                           AllocationBreakdownService allocationBreakdownService,
                                           DividendPaymentService dividendPaymentService,
                                           UserValidationService userValidationService,
                                           PriceHistoryService priceHistoryService,
                                           AccountValidationService accountValidationService) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.holdingCalculationService = holdingCalculationService;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.allocationBreakdownService = allocationBreakdownService;
        this.dividendPaymentService = dividendPaymentService;
        this.userValidationService = userValidationService;
        this.priceHistoryService = priceHistoryService;
        this.accountValidationService = accountValidationService;
    }


    // Interface functions

    // Get portfolio overview
    @Override
    public PortfolioOverviewDTO getPortfolioOverview(UUID accountId) {
        // Retrieve account and account holdings
        AccountDTO account = accountService.getAccountById(accountId);
        List<HoldingDTO> holdings = holdingService.getHoldingsByAccount(accountId);

        // Calculate holdings value (current market value of all positions)
        BigDecimal holdingsValue = holdings.stream()
                .map(h -> safe(h.getCurrentPrice()).multiply(safe(h.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total cost basis (total amount invested)
        BigDecimal totalCostBasis = holdings.stream()
                .map(h -> safe(h.getAverageCostBasis()).multiply(safe(h.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate unrealized gain (current value - cost basis)
        BigDecimal totalUnrealizedGain = holdingsValue.subtract(totalCostBasis);

        // Calculate realized gain (from closed positions)
        BigDecimal totalRealizedGain = holdings.stream()
                .map(h -> safe(h.getRealizedGain()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get total dividends received
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> safe(dto.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get cash balance
        BigDecimal cashBalance = safe(account.getCashBalance());

        // Calculate total portfolio value (holdings + cash)
        BigDecimal totalPortfolioValue = holdingsValue.add(cashBalance);

        // Construct DTO
        PortfolioOverviewDTO dto = new PortfolioOverviewDTO();
        dto.setAccountId(accountId);
        dto.setTotalPortfolioValue(totalPortfolioValue);
        dto.setHoldingsValue(holdingsValue);
        dto.setTotalCostBasis(totalCostBasis);
        dto.setTotalUnrealizedGain(totalUnrealizedGain);
        dto.setTotalRealizedGain(totalRealizedGain);
        dto.setTotalDividends(totalDividends);
        dto.setCashBalance(cashBalance);
        dto.setHoldings(holdings);

        return dto;
    }

    // Get allocation breakdown
    @Override
    public List<AllocationBreakdownDTO> getAllocationBreakdown(UUID accountId) {
        List<Holding> holdings = holdingRepository.findByAccount_AccountId(accountId);

        // Calculate current value of holdings for each holding
        BigDecimal totalValue = holdings.stream()
                .map(h -> safe(holdingCalculationService.calculateCurrentValue(h)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate percentage allocation for each holding in regard to the total portfolio
        return holdings.stream()
                .map(h -> {
                    BigDecimal currentValue = safe(holdingCalculationService.calculateCurrentValue(h));
                    BigDecimal percentage = totalValue.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : currentValue.multiply(BigDecimal.valueOf(100))
                            .divide(totalValue, 4, RoundingMode.HALF_UP);

                    AllocationBreakdownDTO dto = new AllocationBreakdownDTO();
                    dto.setStockCode(h.getStock().getStockCode());
                    dto.setPercentage(percentage);
                    dto.setCurrentValue(currentValue);
                    return dto;
                })
                .toList();
    }

    // Aggregate account
    public PortfolioAggregationDTO aggregateForAccount(UUID accountId) {
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Calculate holdings value using current prices
        BigDecimal holdingsValue = account.getHoldings().stream()
                .map(h -> safe(priceHistoryService.getCurrentPrice(h.getStock().getStockId()))
                        .multiply(safe(h.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get cash balance
        BigDecimal cashBalance = safe(account.getAccountBalance());

        // Total portfolio value = holdings + cash
        BigDecimal totalValue = holdingsValue.add(cashBalance);

        // Calculate total dividends using totalAmount
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> safe(dto.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int numberOfHoldings = account.getHoldings().size();
        List<AllocationBreakdownDTO> allocations = allocationBreakdownService.getAllocationForAccount(accountId);

        // Return constructed DTO
        return new PortfolioAggregationDTO(
                account.getAccountId(),
                account.getUser().getUserId(),
                totalValue,
                totalDividends,
                numberOfHoldings,
                allocations
        );
    }

    // Aggregate by user
    public PortfolioAggregationDTO aggregateForUser(UUID userId) {
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        int totalHoldings = 0;
        List<AllocationBreakdownDTO> allAllocations = new ArrayList<>();

        // Retrieve and calculate for accounts attached to user
        for (Account account : user.getAccounts()) {
            PortfolioAggregationDTO accountAgg = aggregateForAccount(account.getAccountId());
            totalValue = totalValue.add(safe(accountAgg.getTotalValue()));
            totalDividends = totalDividends.add(safe(accountAgg.getTotalDividends()));
            totalHoldings += accountAgg.getNumberOfHoldings();
            if (accountAgg.getAllocations() != null) {
                allAllocations.addAll(accountAgg.getAllocations());
            }
        }

        return new PortfolioAggregationDTO(
                null,
                userId,
                totalValue,
                totalDividends,
                totalHoldings,
                allAllocations
        );
    }

    // Helper to safely return BigDecimal or ZERO if null
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}