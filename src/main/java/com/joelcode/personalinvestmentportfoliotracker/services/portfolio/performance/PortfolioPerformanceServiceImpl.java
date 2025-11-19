package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance;

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
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioPerformanceServiceImpl implements PortfolioPerformanceService{

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final DividendCalculationService dividendCalculationService;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final AccountValidationService accountValidationService;
    private final DividendPaymentService dividendPaymentService;
    private final UserValidationService userValidationService;

    // Constructor
    public PortfolioPerformanceServiceImpl (AccountService accountService, HoldingService holdingService,
                                            DividendCalculationService dividendCalculationService,
                                            PortfolioSnapshotRepository snapshotRepository,
                                            AccountRepository accountRepository, HoldingRepository holdingRepository,
                                            AccountValidationService accountValidationService,
                                            DividendPaymentService dividendPaymentService,
                                            UserValidationService userValidationService) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.dividendCalculationService = dividendCalculationService;
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.accountValidationService = accountValidationService;
        this.dividendPaymentService = dividendPaymentService;
        this.userValidationService = userValidationService;
    }

    @Override
    @Transactional
    public PortfolioPerformanceDTO calculatePortfolioPerformance(UUID accountId){

        // Find account and retrieve holdings
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        List<Holding> holdings = holdingRepository.getHoldingsEntitiesByAccount(accountId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        BigDecimal totalHoldingsValue = BigDecimal.ZERO;

        // Calculate value from holdings
        for (Holding h : holdings) {
            totalInvested = totalInvested.add(h.getTotalCostBasis());
            totalUnrealizedGain = totalUnrealizedGain.add(h.getUnrealizedGain(h.getCurrentValue(BigDecimal.valueOf(h.getStock().getStockValue()))));
            totalRealizedGain = totalRealizedGain.add(h.getRealizedGain());
            totalHoldingsValue = totalHoldingsValue.add(h.getCurrentValue(h.getCurrentValue(BigDecimal.valueOf(h.getStock().getStockValue()))));
        }

        // Fetch dividends
        BigDecimal totalDividends = dividendCalculationService.calculateTotalDividends(accountId);

        // Fetch cash balance
        BigDecimal cashBalance = account.getAccountBalance();
        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }

        // Total portfilio balance
        BigDecimal totalPortfolioValue = cashBalance.add(totalHoldingsValue);

        // Calculate roi
        BigDecimal roi = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            roi = totalPortfolioValue.subtract(totalInvested)
                    .divide(totalInvested, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Build dto
        PortfolioPerformanceDTO dto = new PortfolioPerformanceDTO();
        dto.setAccountId(accountId);
        dto.setTotalPortfolioValue(totalPortfolioValue);
        dto.setTotalInvested(totalInvested);
        dto.setTotalRealizedGain(totalRealizedGain);
        dto.setTotalUnrealizedGain(totalUnrealizedGain);
        dto.setTotalDividends(totalDividends);
        dto.setCashBalance(cashBalance);
        dto.setRoiPercentage(roi);

        return dto;
    }

    @Override
    @Transactional
    public void createPortfolioSnapshot(UUID accountId){
        PortfolioPerformanceDTO performance = calculatePortfolioPerformance(accountId);

        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setAccount(accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found")));
        snapshot.setTotalValue(performance.getTotalPortfolioValue());
        snapshot.setCashBalance(performance.getCashBalance());
        snapshot.setTotalInvested(performance.getTotalInvested());
        snapshot.setRealizedGain(performance.getTotalRealizedGain());
        snapshot.setUnrealizedGain(performance.getTotalUnrealizedGain());
        snapshot.setTotalDividends(performance.getTotalDividends());
        snapshot.setRoiPercentage(performance.getRoiPercentage());
        snapshot.setSnapshotDate(java.time.LocalDate.now());

        snapshotRepository.save(snapshot);

    }

    @Override
    public PortfolioPerformanceDTO getPerformanceForAccount(UUID accountId) {
        // Validate account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Get holdings as DTOs
        List<HoldingDTO> holdings = holdingService.getHoldingsForAccount(accountId);

        // EDGE CASE: Empty holdings
        if (holdings == null || holdings.isEmpty()) {
            BigDecimal cashBalance = account.getAccountBalance() != null ?
                    account.getAccountBalance() : BigDecimal.ZERO;

            return new PortfolioPerformanceDTO(
                    account.getAccountId(),
                    cashBalance,           // totalPortfolioValue = just cash
                    BigDecimal.ZERO,       // totalInvested
                    BigDecimal.ZERO,       // totalRealizedGain
                    BigDecimal.ZERO,       // totalUnrealizedGain
                    BigDecimal.ZERO,       // totalDividends
                    cashBalance,           // cashBalance
                    BigDecimal.ZERO,       // roiPercentage
                    BigDecimal.ZERO,       // dailyGain
                    BigDecimal.ZERO        // monthlyGain
            );
        }

        // Calculate total invested
        BigDecimal totalInvested = BigDecimal.ZERO;
        for (HoldingDTO h : holdings) {
            if (h != null && h.getAverageCostBasis() != null && h.getQuantity() != null) {
                totalInvested = totalInvested.add(
                        h.getAverageCostBasis().multiply(h.getQuantity())
                );
            }
        }

        // Calculate total current value
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        for (HoldingDTO h : holdings) {
            if (h != null && h.getCurrentPrice() != null && h.getQuantity() != null) {
                totalPortfolioValue = totalPortfolioValue.add(
                        h.getCurrentPrice().multiply(h.getQuantity())
                );
            }
        }

        // Unrealized gain
        BigDecimal totalUnrealizedGain = totalPortfolioValue.subtract(totalInvested);

        // Realized gain (placeholder)
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        for (HoldingDTO h : holdings) {
            if (h != null && h.getRealizedGain() != null) {
                totalRealizedGain = totalRealizedGain.add(h.getRealizedGain());
            }
        }

        // Total dividends
        BigDecimal totalDividends = BigDecimal.ZERO;
        try {
            List<DividendPaymentDTO> payments = dividendPaymentService.getDividendPaymentsForAccount(accountId);
            if (payments != null) {
                for (DividendPaymentDTO dto : payments) {
                    if (dto != null && dto.getTotalAmount() != null) {
                        totalDividends = totalDividends.add(dto.getTotalAmount());
                    }
                }
            }
        } catch (Exception e) {
            // If dividend calculation fails, continue with zero
            totalDividends = BigDecimal.ZERO;
        }

        // Cash balance
        BigDecimal cashBalance = account.getAccountBalance() != null ?
                account.getAccountBalance() : BigDecimal.ZERO;

        // ROI calculation with EDGE CASE handling
        BigDecimal totalReturn = totalUnrealizedGain.add(totalRealizedGain).add(totalDividends);
        BigDecimal roiPercentage;

        // EDGE CASE: Division by zero
        if (totalInvested.compareTo(BigDecimal.ZERO) == 0) {
            roiPercentage = BigDecimal.ZERO;
        } else {
            roiPercentage = totalReturn
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        }

        // Daily and monthly gain placeholders
        BigDecimal dailyGain = BigDecimal.ZERO;
        BigDecimal monthlyGain = BigDecimal.ZERO;

        return new PortfolioPerformanceDTO(
                account.getAccountId(),
                totalPortfolioValue,
                totalInvested,
                totalRealizedGain,
                totalUnrealizedGain,
                totalDividends,
                cashBalance,
                roiPercentage,
                dailyGain,
                monthlyGain
        );
    }


    @Override
    public PortfolioPerformanceDTO getPerformanceForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalDividends = BigDecimal.ZERO;
        BigDecimal cashBalance = BigDecimal.ZERO;

        // Aggregate performance across all accounts
        for (Account account : user.getAccounts()) {
            PortfolioPerformanceDTO accountPerf = getPerformanceForAccount(account.getAccountId());
            totalInvested = totalInvested.add(accountPerf.getTotalInvested());
            totalPortfolioValue = totalPortfolioValue.add(accountPerf.getTotalPortfolioValue());
            totalRealizedGain = totalRealizedGain.add(accountPerf.getTotalRealizedGain());
            totalUnrealizedGain = totalUnrealizedGain.add(accountPerf.getTotalUnrealizedGain());
            totalDividends = totalDividends.add(accountPerf.getTotalDividends());
            cashBalance = cashBalance.add(account.getAccountBalance() != null ? account.getAccountBalance() : BigDecimal.ZERO);
        }

        // Total return = unrealized + realized + dividends
        BigDecimal totalReturn = totalUnrealizedGain.add(totalRealizedGain).add(totalDividends);

        // ROI % = (Total Return / Total Invested) * 100
        BigDecimal roiPercentage = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalReturn.multiply(BigDecimal.valueOf(100)).divide(totalInvested, 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        // Daily and monthly gain placeholders
        BigDecimal dailyGain = BigDecimal.ZERO;   // Replace with actual calculation
        BigDecimal monthlyGain = BigDecimal.ZERO; // Replace with actual calculation

        return new PortfolioPerformanceDTO(
                null,                     // No single account for user-level
                totalPortfolioValue,      // totalPortfolioValue
                totalInvested,            // totalInvested
                totalRealizedGain,        // totalRealizedGain
                totalUnrealizedGain,      // totalUnrealizedGain
                totalDividends,           // totalDividends
                cashBalance,              // cashBalance
                roiPercentage,            // roiPercentage
                dailyGain,                // dailyGain
                monthlyGain               // monthlyGain
        );
    }




}
