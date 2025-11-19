package com.joelcode.personalinvestmentportfoliotracker.services.dividend;

import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendPaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DividendCalculationServiceImpl implements DividendCalculationService {

    private final DividendPaymentRepository paymentRepository;

    public DividendCalculationServiceImpl(DividendPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public BigDecimal calculateTotalDividends(UUID accountId) {
        return paymentRepository.calculateTotalDividendsByAccount(accountId);
    }

    @Override
    public void recalculateDividends(UUID accountId) {
        // This method is no longer needed with the new structure
        // Dividend payments are created when dividends are announced
        // and are immutable records of what was actually paid
        throw new UnsupportedOperationException(
                "Recalculation not supported. Dividend payments are immutable records. " +
                        "Use processPaymentsForDividend() when announcing new dividends."
        );
    }

    // New helper methods
    public BigDecimal calculateDividendsInDateRange(UUID accountId,
                                                    LocalDateTime start,
                                                    LocalDateTime end) {
        return paymentRepository.calculateDividendsInDateRange(accountId, start, end);
    }

    public BigDecimal calculateDividendsForStock(UUID accountId, UUID stockId) {
        return paymentRepository.calculateTotalDividendsByAccountAndStock(accountId, stockId);
    }
}