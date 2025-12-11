package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!test")
public class PortfolioPerformanceServiceImpl implements PortfolioPerformanceService{

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final AccountValidationService accountValidationService;
    private final DividendPaymentService dividendPaymentService;
    private final UserValidationService userValidationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketController webSocketController;


    // Constructor
    public PortfolioPerformanceServiceImpl (AccountService accountService, HoldingService holdingService,
                                            PortfolioSnapshotRepository snapshotRepository,
                                            AccountRepository accountRepository, HoldingRepository holdingRepository,
                                            AccountValidationService accountValidationService,
                                            DividendPaymentService dividendPaymentService,
                                            UserValidationService userValidationService,
                                            SimpMessagingTemplate messagingTemplate,
                                            WebSocketController webSocketController) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.accountValidationService = accountValidationService;
        this.dividendPaymentService = dividendPaymentService;
        this.userValidationService = userValidationService;
        this.messagingTemplate = messagingTemplate;
        this.webSocketController = webSocketController;
    }


    // Interface functions

    // Calculate performance for a portfolio
    @Override
    @Transactional
    public PortfolioPerformanceDTO calculatePortfolioPerformance(UUID accountId){
        // Find account and retrieve holdings
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        List<Holding> holdings = holdingRepository.findByAccount_AccountId(accountId);

        // Calculate holdings value (current market value of all positions)
        BigDecimal holdingsValue = BigDecimal.ZERO;
        for (Holding h : holdings) {
            BigDecimal currentPrice = safe(h.getStock().getStockValue());
            BigDecimal quantity = safe(h.getQuantity());
            holdingsValue = holdingsValue.add(currentPrice.multiply(quantity));
        }

        // Calculate total cost basis (total amount invested)
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        for (Holding h : holdings) {
            totalCostBasis = totalCostBasis.add(safe(h.getTotalCostBasis()));
        }

        // Calculate unrealized gain (current value - cost basis)
        BigDecimal totalUnrealizedGain = holdingsValue.subtract(totalCostBasis);

        // Calculate realized gain (from closed positions)
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        for (Holding h : holdings) {
            totalRealizedGain = totalRealizedGain.add(safe(h.getRealizedGain()));
        }

        // Get total dividends using totalAmount
        BigDecimal totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                .map(dto -> safe(dto.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get cash balance
        BigDecimal cashBalance = safe(account.getAccountBalance());

        // Calculate total portfolio value (holdings + cash)
        BigDecimal totalPortfolioValue = holdingsValue.add(cashBalance);

        // Normalize scale for clean display
        totalPortfolioValue = normalize(totalPortfolioValue);

        // Calculate ROI: (Total Return / Cost Basis) × 100
        // Total Return = Unrealized Gain + Realized Gain + Dividends
        BigDecimal totalReturn = totalUnrealizedGain.add(totalRealizedGain).add(totalDividends);
        BigDecimal roiPercentage = BigDecimal.ZERO;
        if (totalCostBasis.compareTo(BigDecimal.ZERO) > 0) {
            roiPercentage = totalReturn
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalCostBasis, 2, RoundingMode.HALF_UP);
        }

        // Build DTO
        PortfolioPerformanceDTO dto = new PortfolioPerformanceDTO();
        dto.setAccountId(accountId);
        dto.setTotalPortfolioValue(normalize(totalPortfolioValue));
        dto.setTotalCostBasis(normalize(totalCostBasis));
        dto.setTotalRealizedGain(normalize(totalRealizedGain));
        dto.setTotalUnrealizedGain(normalize(totalUnrealizedGain));
        dto.setTotalDividends(normalize(totalDividends));
        dto.setCashBalance(normalize(cashBalance));
        dto.setRoiPercentage(roiPercentage);

        return dto;
    }

    // Create a portfolio snapshot
    @Override
    @Transactional
    public void createPortfolioSnapshot(UUID accountId){
        PortfolioPerformanceDTO performance = calculatePortfolioPerformance(accountId);

        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setAccount(accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found")));
        snapshot.setTotalValue(performance.getTotalPortfolioValue());
        snapshot.setCashBalance(performance.getCashBalance());
        snapshot.setTotalCostBasis(performance.getTotalCostBasis());
        snapshot.setRealizedGain(performance.getTotalRealizedGain());
        snapshot.setUnrealizedGain(performance.getTotalUnrealizedGain());
        snapshot.setTotalDividends(performance.getTotalDividends());
        snapshot.setRoiPercentage(performance.getRoiPercentage());
        snapshot.setSnapshotDate(java.time.LocalDate.now());

        snapshotRepository.save(snapshot);

        // Web socket notification
        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                snapshot.getAccount().getAccountId(),
                performance.getTotalPortfolioValue(),
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + accountId,
                updateMessage
        );
    }

    // Get performance for an account
    @Override
    public PortfolioPerformanceDTO getPerformanceForAccount(UUID accountId) {
        // Validate account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Get holdings as DTOs
        List<HoldingDTO> holdings = holdingService.getHoldingsForAccount(accountId);

        // Get cash balance
        BigDecimal cashBalance = safe(account.getAccountBalance());

        // EDGE CASE: Empty holdings
        if (holdings == null || holdings.isEmpty()) {
            return new PortfolioPerformanceDTO(
                    account.getUserid(),
                    account.getAccountId(),
                    cashBalance,           // totalPortfolioValue = just cash
                    BigDecimal.ZERO,       // totalCostBasis
                    BigDecimal.ZERO,       // totalRealizedGain
                    BigDecimal.ZERO,       // totalUnrealizedGain
                    BigDecimal.ZERO,       // totalDividends
                    cashBalance,           // cashBalance
                    BigDecimal.ZERO,       // roiPercentage
                    BigDecimal.ZERO,       // dailyGain
                    BigDecimal.ZERO        // monthlyGain
            );
        }

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

        // Get total dividends using totalAmount
        BigDecimal totalDividends = BigDecimal.ZERO;
        try {
            totalDividends = dividendPaymentService.getDividendPaymentsForAccount(accountId).stream()
                    .map(dto -> safe(dto.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            // If dividend calculation fails, continue with zero
            totalDividends = BigDecimal.ZERO;
        }

        // Calculate total portfolio value (holdings + cash)
        BigDecimal totalPortfolioValue = holdingsValue.add(cashBalance);

        // Calculate ROI: (Total Return / Cost Basis) × 100
        // Total Return = Unrealized Gain + Realized Gain + Dividends
        BigDecimal totalReturn = totalUnrealizedGain.add(totalRealizedGain).add(totalDividends);
        BigDecimal roiPercentage;

        // EDGE CASE: Division by zero
        if (totalCostBasis.compareTo(BigDecimal.ZERO) == 0) {
            roiPercentage = BigDecimal.ZERO;
        } else {
            roiPercentage = totalReturn
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalCostBasis, 2, RoundingMode.HALF_UP);
        }

        // Daily and monthly gain placeholders (implement if needed)
        BigDecimal dailyGain = BigDecimal.ZERO;
        BigDecimal monthlyGain = BigDecimal.ZERO;

        return new PortfolioPerformanceDTO(
                account.getUserid(),
                account.getAccountId(),
                totalPortfolioValue,
                totalCostBasis,
                totalRealizedGain,
                totalUnrealizedGain,
                totalDividends,
                cashBalance,
                roiPercentage,
                dailyGain,
                monthlyGain
        );
    }

    // Get performance on the user level
    @Override
    public PortfolioPerformanceDTO getPerformanceForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        BigDecimal cashBalance = BigDecimal.ZERO;

        // Aggregate performance across all accounts
        for (Account account : user.getAccounts()) {
            PortfolioPerformanceDTO accountPerf = getPerformanceForAccount(account.getAccountId());
            totalPortfolioValue = totalPortfolioValue.add(accountPerf.getTotalPortfolioValue());
            totalCostBasis = totalCostBasis.add(accountPerf.getTotalCostBasis());
            totalRealizedGain = totalRealizedGain.add(accountPerf.getTotalRealizedGain());
            totalUnrealizedGain = totalUnrealizedGain.add(accountPerf.getTotalUnrealizedGain());
            totalDividends = totalDividends.add(accountPerf.getTotalDividends());
            cashBalance = cashBalance.add(accountPerf.getCashBalance());
        }

        // Calculate ROI: (Total Return / Cost Basis) × 100
        // Total Return = Unrealized Gain + Realized Gain + Dividends
        BigDecimal totalReturn = totalUnrealizedGain.add(totalRealizedGain).add(totalDividends);
        BigDecimal roiPercentage = totalCostBasis.compareTo(BigDecimal.ZERO) > 0
                ? totalReturn.multiply(BigDecimal.valueOf(100)).divide(totalCostBasis, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Daily and monthly gain placeholders (implement if needed)
        BigDecimal dailyGain = BigDecimal.ZERO;
        BigDecimal monthlyGain = BigDecimal.ZERO;

        return new PortfolioPerformanceDTO(
                userId,
                totalPortfolioValue,
                totalCostBasis,
                totalRealizedGain,
                totalUnrealizedGain,
                totalDividends,
                cashBalance,
                roiPercentage,
                dailyGain,
                monthlyGain
        );
    }

    // Normalizing Big Decimal values for clean calculation
    private BigDecimal normalize(BigDecimal value) {
        if (value == null) return null;
        if (value.scale() > 0) {
            try {
                return value.setScale(0, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException ignored) {
                // keep original if non-zero fractional part
            }
        }
        return value;
    }

    // Helper to safely return BigDecimal or ZERO if null
    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}