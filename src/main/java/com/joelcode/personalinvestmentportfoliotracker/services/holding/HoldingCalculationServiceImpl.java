package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class HoldingCalculationServiceImpl implements HoldingCalculationService {

    // Define key fields
    private final HoldingRepository holdingRepository;
    private final HoldingValidationService holdingValidationService;

    // Constructor
    public HoldingCalculationServiceImpl(HoldingRepository holdingRepository,
                                         HoldingValidationService holdingValidationService) {
        this.holdingRepository = holdingRepository;
        this.holdingValidationService = holdingValidationService;
    }

    // Calculate total portfolio value for an account
    @Override
    public BigDecimal calculateTotalPortfolioValue(UUID accountId) {

        // Validate account exists
        Account account = holdingValidationService.validateAccountExists(accountId);

        // Get all holdings for account
        List<Holding> holdings = holdingRepository.findByAccount(account);

        BigDecimal totalValue = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            BigDecimal currentPrice = BigDecimal.valueOf(holding.getStock().getStockValue());
            BigDecimal currentValue = holding.getCurrentValue(currentPrice);
            totalValue = totalValue.add(currentValue);
        }

        return totalValue;
    }

    // Calculate total cost basis for an account
    @Override
    public BigDecimal calculateTotalCostBasis(UUID accountId) {

        // Validate account exists
        Account account = holdingValidationService.validateAccountExists(accountId);

        // Use repository method for efficient calculation
        BigDecimal totalCostBasis = holdingRepository.sumTotalCostBasisByAccount(account);

        return totalCostBasis != null ? totalCostBasis : BigDecimal.ZERO;
    }

    // Calculate total unrealized gain for an account
    @Override
    public BigDecimal calculateTotalUnrealizedGain(UUID accountId) {

        // Validate account exists
        Account account = holdingValidationService.validateAccountExists(accountId);

        // Get all holdings for account
        List<Holding> holdings = holdingRepository.findByAccount(account);

        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            BigDecimal currentPrice = BigDecimal.valueOf(holding.getStock().getStockValue());
            BigDecimal unrealizedGain = holding.getUnrealizedGain(currentPrice);
            totalUnrealizedGain = totalUnrealizedGain.add(unrealizedGain);
        }

        return totalUnrealizedGain;
    }

    // Calculate total realized gain for an account
    @Override
    public BigDecimal calculateTotalRealizedGain(UUID accountId) {

        // Validate account exists
        Account account = holdingValidationService.validateAccountExists(accountId);

        // Get all holdings for account
        List<Holding> holdings = holdingRepository.findByAccount(account);

        BigDecimal totalRealizedGain = BigDecimal.ZERO;

        for (Holding holding : holdings) {
            BigDecimal realizedGain = holding.getRealizedGain();
            totalRealizedGain = totalRealizedGain.add(realizedGain);
        }

        return totalRealizedGain;
    }
}