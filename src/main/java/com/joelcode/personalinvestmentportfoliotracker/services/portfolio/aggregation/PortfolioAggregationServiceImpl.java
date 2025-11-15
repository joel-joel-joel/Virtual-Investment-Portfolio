package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.model.PortfolioOverviewDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioAggregationServiceImpl implements PortfolioAggregationService{

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final HoldingCalculationService holdingCalculationService;
    private final DividendCalculationService dividendCalculationService;
    private final AccountRepository accountRepository;

    // Constructor
    public PortfolioAggregationServiceImpl (AccountService accountService, HoldingService holdingService,
                                            HoldingCalculationService holdingCalculationService,
                                            DividendCalculationService dividendCalculationService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.holdingCalculationService = holdingCalculationService;
        this.dividendCalculationService = dividendCalculationService;
        this.accountRepository = accountRepository;
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

}
