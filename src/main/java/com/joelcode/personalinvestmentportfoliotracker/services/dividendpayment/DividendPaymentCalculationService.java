package com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DividendPaymentCalculationService {

    BigDecimal calculateTotalDividends(UUID accountId);

    BigDecimal calculateDividendsInDateRange(UUID accountId, LocalDateTime start, LocalDateTime end);

    BigDecimal calculateDividendsForStock(UUID accountId, UUID stockId);



}
