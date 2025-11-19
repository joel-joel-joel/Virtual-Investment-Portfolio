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
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioAggregationServiceImpl implements PortfolioAggregationService{

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final AccountRepository accountRepository;
    private final HoldingCalculationService holdingCalculationService;
    private final HoldingRepository holdingRepository;
    private final DividendCalculationService dividendCalculationService;
    private final AllocationBreakdownService allocationBreakdownService;
    private final DividendPaymentService dividendPaymentService;
    private final UserValidationService userValidationService;
    private final PriceHistoryService priceHistoryService;
    private final AccountValidationService accountValidationService;

    // Constructor
    public PortfolioAggregationServiceImpl (AccountService accountService, HoldingService holdingService,
                                            HoldingCalculationService holdingCalculationService,
                                            DividendCalculationService dividendCalculationService, AccountRepository accountRepository,
                                            HoldingRepository holdingRepository,
                                            AllocationBreakdownService allocationBreakdownService,
                                            DividendPaymentService dividendPaymentService,
                                            UserValidationService userValidationService,
                                            PriceHistoryService priceHistoryService,
                                            AccountValidationService accountValidationService) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.holdingCalculationService = holdingCalculationService;
        this.dividendCalculationService = dividendCalculationService;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.allocationBreakdownService = allocationBreakdownService;
        this.dividendPaymentService = dividendPaymentService;
        this.userValidationService = userValidationService;
        this.priceHistoryService = priceHistoryService;
        this.accountValidationService = accountValidationService;
    }

    // Interface function
    @Override
    public PortfolioOverviewDTO getPortfolioOverview(UUID accountId) {

        // Fetch account
        AccountDTO account = accountService.getAccountById(accountId);

        // Fetch holdings
        List<HoldingDTO> holdings = holdingService.getHoldingsByAccount(accountId);

        // Initialize and calculate invested returns and losses
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;

        for (HoldingDTO holding : holdings) {
            totalInvested = totalInvested.add(holding.getTotalCostBasis());
            totalUnrealizedGain = totalUnrealizedGain.add(holding.getUnrealizedGain());
            totalRealizedGain = totalRealizedGain.add(holding.getRealizedGain());
        }

        // Fetch dividends
        BigDecimal totalDividends = dividendCalculationService.calculateTotalDividends(accountId);

        // Fetch cash balance
        BigDecimal cashBalance = account.getCashBalance();
        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }

        // Calculate full portfolio balance
        BigDecimal totalPortfolioValue = cashBalance.add(
                holdings.stream()
                        .map(HoldingDTO::getCurrentValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        // Construct DTO
        PortfolioOverviewDTO dto = new PortfolioOverviewDTO();
        dto.setAccountId(accountId);
        dto.setTotalPortfolioValue(totalPortfolioValue);
        dto.setTotalInvested(totalInvested);
        dto.setTotalUnrealizedGain(totalUnrealizedGain);
        dto.setTotalRealizedGain(totalRealizedGain);
        dto.setTotalDividends(totalDividends);
        dto.setCashBalance(cashBalance);
        dto.setHoldings(holdings);

        return dto;
    }


    @Override
    public List<AllocationBreakdownDTO> getAllocationBreakdown(UUID accountId) {

        List<Holding> holdings = holdingRepository.getHoldingsEntitiesByAccount(accountId);

        BigDecimal totalValue = holdings.stream()
                .map(holdingCalculationService::calculateCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return holdings.stream()
                .map(h -> {
                    BigDecimal currentValue = holdingCalculationService.calculateCurrentValue(h);
                    BigDecimal percentage =
                            totalValue.compareTo(BigDecimal.ZERO) == 0
                                    ? BigDecimal.ZERO
                                    : currentValue
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(totalValue, 4, RoundingMode.HALF_UP);

                    AllocationBreakdownDTO dto = new AllocationBreakdownDTO();
                    dto.setStockCode(h.getStock().getStockCode());
                    dto.setPercentage(percentage);
                    dto.setCurrentValue(currentValue);
                    return dto;
                })
                .toList();
    }

    // Aggregate portfolio for an account
    public PortfolioAggregationDTO aggregateForAccount(UUID accountId) {
        // Validate account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Calculate total value of holdings
        BigDecimal totalValue = account.getHoldings().stream()
                .map(h -> priceHistoryService.getCurrentPrice(h.getStock().getStockId())
                        .multiply(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total dividends received
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> dto.getAmountPerShare())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Number of holdings
        int numberOfHoldings = account.getHoldings().size();

        // Allocation breakdown
        List<AllocationBreakdownDTO> allocations = allocationBreakdownService.getAllocationForAccount(accountId);

        return new PortfolioAggregationDTO(
                account.getAccountId(),
                account.getUser().getUserId(),
                totalValue,
                totalDividends,
                numberOfHoldings,
                allocations
        );
    }

     // Aggregate portfolio data across all accounts for a user.

    public PortfolioAggregationDTO aggregateForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        int totalHoldings = 0;
        List<AllocationBreakdownDTO> allAllocations = new ArrayList<>();

        // Loop through all accounts and aggregate
        for (Account account : user.getAccounts()) {
            PortfolioAggregationDTO accountAgg = aggregateForAccount(account.getAccountId());
            totalValue = totalValue.add(accountAgg.getTotalValue());
            totalDividends = totalDividends.add(accountAgg.getTotalDividends());
            totalHoldings += accountAgg.getNumberOfHoldings();
            allAllocations.addAll((Collection<? extends AllocationBreakdownDTO>) accountAgg.getAllocations());
        }

        return new PortfolioAggregationDTO(
                null, // user-level aggregation does not have a single accountId
                userId,
                totalValue,
                totalDividends,
                totalHoldings,
                allAllocations
        );
    }

}
