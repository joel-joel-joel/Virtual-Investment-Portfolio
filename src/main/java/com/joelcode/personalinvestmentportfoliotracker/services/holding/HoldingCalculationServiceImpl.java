package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Profile("!test")
public class HoldingCalculationServiceImpl implements HoldingCalculationService {

    // Define key fields
    private final HoldingRepository holdingRepository;
    private final HoldingValidationService holdingValidationService;
    private final StockService stockService;

    // Constructor
    public HoldingCalculationServiceImpl(HoldingRepository holdingRepository,
                                         HoldingValidationService holdingValidationService,
                                         StockService stockService) {
        this.holdingRepository = holdingRepository;
        this.holdingValidationService = holdingValidationService;
        this.stockService = stockService;
    }

    // Calculation functions

    // Calculate total portfolio value
    @Override
    public BigDecimal calculateTotalPortfolioValue(UUID accountId) {
        var account = holdingValidationService.validateAccountExists(accountId);
        List<Holding> holdings = holdingRepository.findByAccount(account);

        // EDGE CASE: Empty holdings
        if (holdings == null || holdings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            // EDGE CASE: Null stock check
            if (holding.getStock() == null) {
                continue;
            }

            try {
                BigDecimal currentPrice = stockService.getCurrentPrice(holding.getStock().getStockId());

                // EDGE CASE: Null or negative price
                if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) < 0) {
                    currentPrice = BigDecimal.ZERO;
                }

                BigDecimal currentValue = calculateCurrentValue(holding, currentPrice);
                totalValue = totalValue.add(currentValue);
            } catch (Exception e) {
                // EDGE CASE: Price fetch fails, skip this holding
                continue;
            }
        }

        return totalValue.setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate current value
    @Override
    public BigDecimal calculateCurrentValue(Holding holding) {
        // EDGE CASE: Null checks
        if (holding == null || holding.getStock() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal price = holding.getStock().getStockValue();

        // EDGE CASE: Null quantity
        if (holding.getQuantity() == null) {
            return BigDecimal.ZERO;
        }

        return price.multiply(holding.getQuantity()).setScale(2, RoundingMode.HALF_UP);
    }

    // Overloaded method for explicit price
    public BigDecimal calculateCurrentValue(Holding holding, BigDecimal currentPrice) {
        if (holding == null || currentPrice == null || holding.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.multiply(holding.getQuantity()).setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate total cost basis
    @Override
    public BigDecimal calculateTotalCostBasis(UUID accountId) {
        var account = holdingValidationService.validateAccountExists(accountId);
        BigDecimal totalCostBasis = holdingRepository.sumTotalCostBasisByAccount(account);

        // EDGE CASE: Null result from repository
        return totalCostBasis != null ? totalCostBasis : BigDecimal.ZERO;
    }

    // Calculate total unrealized gain
    @Override
    public BigDecimal calculateTotalUnrealizedGain(UUID accountId) {
        var account = holdingValidationService.validateAccountExists(accountId);
        List<Holding> holdings = holdingRepository.findByAccount(account);

        // EDGE CASE: Empty holdings
        if (holdings == null || holdings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            // EDGE CASE: Null checks
            if (holding == null || holding.getStock() == null) {
                continue;
            }

            try {
                BigDecimal currentPrice = stockService.getCurrentPrice(holding.getStock().getStockId());

                if (currentPrice == null) {
                    currentPrice = BigDecimal.ZERO;
                }

                BigDecimal unrealizedGain = holding.getUnrealizedGain(currentPrice);

                // EDGE CASE: Null gain
                if (unrealizedGain != null) {
                    totalUnrealizedGain = totalUnrealizedGain.add(unrealizedGain);
                }
            } catch (Exception e) {
                // Skip holding if calculation fails
                continue;
            }
        }

        return totalUnrealizedGain.setScale(2, RoundingMode.HALF_UP);
    }

    // Calculate total realized gain
    @Override
    public BigDecimal calculateTotalRealizedGain(UUID accountId) {
        var account = holdingValidationService.validateAccountExists(accountId);
        List<Holding> holdings = holdingRepository.findByAccount(account);

        // EDGE CASE: Empty holdings
        if (holdings == null || holdings.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRealizedGain = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            // EDGE CASE: Null checks
            if (holding == null) {
                continue;
            }

            BigDecimal realizedGain = holding.getRealizedGain();

            // EDGE CASE: Null realized gain
            if (realizedGain != null) {
                totalRealizedGain = totalRealizedGain.add(realizedGain);
            }
        }

        return totalRealizedGain.setScale(2, RoundingMode.HALF_UP);
    }
}