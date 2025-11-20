package com.joelcode.personalinvestmentportfoliotracker.services.dividend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DividendCalculationService {

    BigDecimal calculateTotalDividends(UUID accountId);

    void recalculateDividends(java.util.UUID accountId);

    BigDecimal calculateDividendsInDateRange(UUID accountId,
                                             LocalDateTime start,
                                             LocalDateTime end);

    BigDecimal calculateDividendsForStock(UUID accountId, UUID stockId);



}
