package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation.AllocationBreakdownService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview.PortfolioOverviewService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioOverviewServiceImpl implements PortfolioOverviewService {

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

    @Override
    public PortfolioOverviewDTO getPortfolioOverviewForAccount(java.util.UUID accountId) {
        // Validate account exists
        Account account = accountValidationService.validateAccountExists(accountId);

        // Get holdings as DTOs
        List<HoldingDTO> holdings = holdingService.getHoldingsForAccount(accountId);

        // Calculate total portfolio value
        BigDecimal totalPortfolioValue = holdings.stream()
                .map(h -> h.getCurrentPrice().multiply(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total invested
        BigDecimal totalInvested = holdings.stream()
                .map(h -> h.getAverageCostBasis().multiply(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate unrealized gain
        BigDecimal totalUnrealizedGain = totalPortfolioValue.subtract(totalInvested);

        // Calculate realized gain (you may need a transaction service for this)
        BigDecimal totalRealizedGain = BigDecimal.ZERO; // placeholder, implement logic if you have realized transactions

        // Calculate total dividends
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> dto.getAmountPerShare())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cash balance (assume account has a cashBalance field)
        BigDecimal cashBalance = account.getAccountBalance() != null ? account.getAccountBalance() : BigDecimal.ZERO;

        return new PortfolioOverviewDTO(
                account.getAccountId(),
                totalPortfolioValue,
                totalInvested,
                totalUnrealizedGain,
                totalRealizedGain,
                totalDividends,
                cashBalance,
                holdings
        );
    }

    @Override
    public PortfolioOverviewDTO getPortfolioOverviewForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        BigDecimal cashBalance = BigDecimal.ZERO;

        List<HoldingDTO> allHoldings = new ArrayList<>();

        // Aggregate across all accounts
        for (Account account : user.getAccounts()) {
            PortfolioOverviewDTO accountOverview = getPortfolioOverviewForAccount(account.getAccountId());

            totalPortfolioValue = totalPortfolioValue.add(accountOverview.getTotalPortfolioValue());
            totalInvested = totalInvested.add(accountOverview.getTotalInvested());
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
                null,                  // No single accountId for user-level overview
                totalPortfolioValue,
                totalInvested,
                totalUnrealizedGain,
                totalRealizedGain,
                totalDividends,
                cashBalance,
                allHoldings
        );
    }
}
