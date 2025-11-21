package com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("!test")
public class PortfolioSnapshotCalculationServiceImpl implements PortfolioSnapshotCalculationService {

    // Define key fields
    private final PortfolioSnapshotService snapshotService;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final PortfolioSnapshotValidationService snapshotValidationService;
    private final HoldingCalculationService holdingCalculationService;


    // Constructor
    public PortfolioSnapshotCalculationServiceImpl(PortfolioSnapshotService snapshotService,
                                                   PortfolioSnapshotRepository snapshotRepository,
                                                   PortfolioSnapshotValidationService snapshotValidationService,
                                                   HoldingCalculationService holdingCalculationService) {
        this.snapshotService = snapshotService;
        this.snapshotRepository = snapshotRepository;
        this.snapshotValidationService = snapshotValidationService;
        this.holdingCalculationService = holdingCalculationService;
    }

    // Calculation functions

    // Generate a snapshot for today based on current holdings
    @Override
    public PortfolioSnapshotDTO generateSnapshotForToday(UUID accountId) {

        // Validate account exists
        Account account = snapshotValidationService.validateAccountExists(accountId);

        LocalDate today = LocalDate.now();

        // Check if snapshot already exists for today
        if (snapshotRepository.existsByAccountAndSnapshotDate(account, today)) {
            throw new RuntimeException("Snapshot already exists for today.");
        }

        // Calculate current portfolio metrics
        BigDecimal totalValue = holdingCalculationService.calculateTotalPortfolioValue(accountId);
        BigDecimal totalCostBasis= holdingCalculationService.calculateTotalCostBasis(accountId); // NEW
        BigDecimal unrealizedGainLoss = holdingCalculationService.calculateTotalUnrealizedGain(accountId);
        BigDecimal realizedGainLoss = holdingCalculationService.calculateTotalRealizedGain(accountId);
        BigDecimal totalGain = unrealizedGainLoss.add(realizedGainLoss);

        // Calculate day change by comparing to yesterday's snapshot
        BigDecimal dayChange = BigDecimal.ZERO;
        BigDecimal dayChangePercent = BigDecimal.ZERO;

        Optional<PortfolioSnapshot> latestSnapshot = snapshotRepository.findLatestByAccount(account);
        if (latestSnapshot.isPresent()) {
            BigDecimal previousValue = latestSnapshot.get().getTotalValue();
            dayChange = totalValue.subtract(previousValue);
            if (previousValue.compareTo(BigDecimal.ZERO) > 0) {
                dayChangePercent = dayChange
                        .divide(previousValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }

        PortfolioSnapshotCreateRequest request = new PortfolioSnapshotCreateRequest(
                accountId,
                today,
                totalValue,
                BigDecimal.ZERO, // Cash balance - can be enhanced later
                totalCostBasis,  // <-- use totalInvested here
                totalGain,
                dayChange
        );

        // Create and return snapshot
        return snapshotService.createSnapshot(request);
    }


    // Calculate time-weighted return (TWR) between two dates
    @Override
    public BigDecimal calculateTimeWeightedReturn(UUID accountId, LocalDate startDate, LocalDate endDate) {

        // Validate account exists
        Account account = snapshotValidationService.validateAccountExists(accountId);
        snapshotValidationService.validateDateRange(startDate, endDate);

        // Get snapshots for the date range
        List<PortfolioSnapshot> snapshots = snapshotRepository.findByAccountAndSnapshotDateBetween(
                account, startDate, endDate
        );

        if (snapshots.isEmpty()) {
            throw new RuntimeException("No snapshots found in the specified date range.");
        }

        // Get starting and ending values
        BigDecimal startValue = snapshots.get(0).getTotalValue();
        BigDecimal endValue = snapshots.get(snapshots.size() - 1).getTotalValue();

        if (startValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // TWR = (Ending Value - Starting Value) / Starting Value * 100
        return endValue.subtract(startValue)
                .divide(startValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    // Calculate average portfolio value over a date range
    @Override
    public BigDecimal calculateAveragePortfolioValue(UUID accountId, LocalDate startDate, LocalDate endDate) {

        // Validate account exists
        Account account = snapshotValidationService.validateAccountExists(accountId);
        snapshotValidationService.validateDateRange(startDate, endDate);

        // Use repository method for efficient calculation
        BigDecimal averageValue = snapshotRepository.averageTotalValueByAccountAndDateRange(
                account, startDate, endDate
        );

        return averageValue != null ? averageValue : BigDecimal.ZERO;
    }
}