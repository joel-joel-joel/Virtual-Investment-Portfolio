package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import java.math.BigDecimal;
import java.util.UUID;

public interface HoldingCalculationService {

    BigDecimal calculateTotalPortfolioValue(UUID accountId);

    BigDecimal calculateTotalCostBasis(UUID accountId);

    BigDecimal calculateTotalUnrealizedGainLoss(UUID accountId);

    BigDecimal calculateTotalRealizedGainLoss(UUID accountId);
}