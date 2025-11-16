package com.joelcode.personalinvestmentportfoliotracker.services.dividend;

import java.math.BigDecimal;
import java.util.UUID;

public interface DividendCalculationService {

    BigDecimal calculateTotalDividends(UUID accountId);

    void recalculateDividends(java.util.UUID accountId);

}
