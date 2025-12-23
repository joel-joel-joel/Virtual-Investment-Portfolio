package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.summary;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.HoldingSummaryDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AccountSummaryDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
@Transactional(readOnly = true)
public class AccountSummaryServiceImpl implements AccountSummaryService{

    // Define key fields
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final StockService stockService;
    private final DividendPaymentService dividendPaymentService;
    private final UserValidationService userValidationService;


    // Constructor
    public AccountSummaryServiceImpl (AccountRepository accountRepository, HoldingRepository holdingRepository,
                                      StockService stockService, DividendPaymentService dividendPaymentService,
                                      UserValidationService userValidationService) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.stockService = stockService;
        this.dividendPaymentService = dividendPaymentService;
        this.userValidationService = userValidationService;
    }


    // Interface functions

    // Get account summary by Id
    public AccountSummaryDTO getAccountSummary(UUID accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        List<Holding> holdings = holdingRepository.findByAccount_AccountId(accountId);

        // Calculate holdings value (current market value of all positions)
        BigDecimal holdingsValue = BigDecimal.ZERO;

        // Calculate total cost basis (total amount invested)
        BigDecimal totalCostBasis = BigDecimal.ZERO;

        List<HoldingSummaryDTO> holdingSummaries = new ArrayList<>();

        for (Holding h : holdings) {
            BigDecimal currentPrice = stockService.getCurrentPrice(h.getStock().getStockId());
            BigDecimal quantity = safe(h.getQuantity());
            BigDecimal averageCostBasis = safe(h.getAverageCostBasis());

            // Market value for this holding
            BigDecimal marketValue = currentPrice.multiply(quantity);

            // Cost basis for this holding
            BigDecimal costBasis = averageCostBasis.multiply(quantity);

            // Unrealized gain for this holding
            BigDecimal unrealizedGain = marketValue.subtract(costBasis);

            // Add to totals
            holdingsValue = holdingsValue.add(marketValue);
            totalCostBasis = totalCostBasis.add(costBasis);

            HoldingSummaryDTO dto = new HoldingSummaryDTO();
            dto.setStockId(h.getStock().getStockId());
            dto.setStockCode(h.getStock().getStockCode());
            dto.setQuantity(quantity);
            dto.setAverageCost(averageCostBasis);
            dto.setMarketPrice(currentPrice);
            dto.setMarketValue(marketValue);
            dto.setUnrealizedGain(unrealizedGain);

            holdingSummaries.add(dto);
        }

        // Calculate total unrealized gain (holdings value - cost basis)
        BigDecimal totalUnrealizedGain = holdingsValue.subtract(totalCostBasis);

        // Get total dividends using totalAmount
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> safe(dto.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get cash balance
        BigDecimal cashBalance = safe(account.getAccountBalance());

        // Calculate total portfolio value (holdings + cash)
        BigDecimal totalPortfolioValue = holdingsValue.add(cashBalance);

        // Calculate ROI: (Total Return / Cost Basis) × 100
        // For summary, Total Return = Unrealized Gain + Dividends
        BigDecimal totalReturn = totalUnrealizedGain.add(totalDividends);
        BigDecimal roiPercentage = BigDecimal.ZERO;
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            roiPercentage = totalReturn
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalCostBasis, 2, RoundingMode.HALF_UP);
        }

        // Build summary
        AccountSummaryDTO summary = new AccountSummaryDTO();
        summary.setAccountId(accountId);
        summary.setTotalCostBasis(totalCostBasis);
        summary.setTotalMarketValue(holdingsValue);
        summary.setTotalUnrealizedGain(totalUnrealizedGain);
        summary.setTotalDividends(totalDividends);
        summary.setTotalCashBalance(cashBalance);
        summary.setHoldings(holdingSummaries);

        return summary;
    }

    // Get all summaries for a user
    public List<AccountSummaryDTO> getAccountSummariesForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        // Stream through each account and get its summary
        return user.getAccounts().stream()
                .map(account -> getAccountSummary(account.getAccountId()))
                .collect(Collectors.toList());
    }

    // Helper to safely return BigDecimal or ZERO if null
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}