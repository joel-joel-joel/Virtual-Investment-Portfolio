package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import java.math.BigDecimal;
import java.util.UUID;

public interface HoldingCalculationService {

    BigDecimal calculateTotalPortfolioValue(UUID accountId);

    BigDecimal calculateTotalCostBasis(UUID accountId);

    BigDecimal calculateTotalUnrealizedGain(UUID accountId);

    BigDecimal calculateTotalRealizedGain(UUID accountId);
}