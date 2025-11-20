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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioAggregationServiceImpl implements PortfolioAggregationService {

    private final AccountService accountService;
    private final HoldingService holdingService;
    private final AccountRepository accountRepository;
    private final HoldingCalculationService holdingCalculationService;
    private final HoldingRepository holdingRepository;
    @Autowired
    private DividendCalculationService dividendCalculationService;
    private final AllocationBreakdownService allocationBreakdownService;
    private final DividendPaymentService dividendPaymentService;
    private final UserValidationService userValidationService;
    private final PriceHistoryService priceHistoryService;
    private final AccountValidationService accountValidationService;

    public PortfolioAggregationServiceImpl(AccountService accountService, HoldingService holdingService,
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

    @Override
    public PortfolioOverviewDTO getPortfolioOverview(UUID accountId) {
        AccountDTO account = accountService.getAccountById(accountId);
        List<HoldingDTO> holdings = holdingService.getHoldingsByAccount(accountId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;

        for (HoldingDTO holding : holdings) {
            totalInvested = totalInvested.add(safe(holding.getTotalCostBasis()));
            totalUnrealizedGain = totalUnrealizedGain.add(safe(holding.getUnrealizedGain()));
            totalRealizedGain = totalRealizedGain.add(safe(holding.getRealizedGain()));
        }

        BigDecimal totalDividends = safe(dividendCalculationService.calculateTotalDividends(accountId));
        BigDecimal cashBalance = safe(account.getCashBalance());

        BigDecimal totalPortfolioValue = cashBalance.add(
                holdings.stream()
                        .map(h -> safe(h.getCurrentValue()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

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
                .map(h -> safe(holdingCalculationService.calculateCurrentValue(h)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

    public PortfolioAggregationDTO aggregateForAccount(UUID accountId) {
        Account account = accountValidationService.validateAccountExistsById(accountId);

        BigDecimal totalValue = account.getHoldings().stream()
                .map(h -> safe(priceHistoryService.getCurrentPrice(h.getStock().getStockId())).multiply(safe(h.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> safe(dto.getDividendPerShare()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int numberOfHoldings = account.getHoldings().size();
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

    public PortfolioAggregationDTO aggregateForUser(UUID userId) {
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        int totalHoldings = 0;
        List<AllocationBreakdownDTO> allAllocations = new ArrayList<>();

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
