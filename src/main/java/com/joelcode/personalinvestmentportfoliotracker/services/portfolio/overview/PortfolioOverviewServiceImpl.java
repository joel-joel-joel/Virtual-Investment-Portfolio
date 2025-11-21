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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!test")
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
        // Validate account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Get holdings as DTOs
        List<HoldingDTO> holdings = holdingService.getHoldingsForAccount(accountId);

        BigDecimal holdingsValue = holdings.stream()
                .map(h -> h.getCurrentPrice().multiply(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total invested for each holding
        BigDecimal totalInvested = holdings.stream()
                .map(h -> h.getAverageCostBasis().multiply(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRealizedGain = BigDecimal.ZERO;

        BigDecimal totalUnrealizedGain = holdingsValue.subtract(totalInvested);

        BigDecimal cashBalance = account.getAccountBalance() != null ? account.getAccountBalance() : BigDecimal.ZERO;

        BigDecimal totalPortfolioValue = holdingsValue.add(cashBalance);

        // Calculate total dividends
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> dto.getDividendPerShare() != null ? dto.getDividendPerShare() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        return new PortfolioOverviewDTO(
                account.getUserid(),
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

    // Get portfolio overview on user level
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
            totalInvested = totalInvested.add(accountOverview.getTotalCostBasis());
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
                totalInvested,
                totalUnrealizedGain,
                totalRealizedGain,
                totalDividends,
                cashBalance,
                allHoldings
        );
    }
}
