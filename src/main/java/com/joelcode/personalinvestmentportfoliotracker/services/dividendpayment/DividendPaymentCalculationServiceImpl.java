package com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment;

import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendPaymentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Profile("!test")
public class DividendPaymentCalculationServiceImpl implements DividendPaymentCalculationService {

    // Define key field
    private final DividendPaymentRepository paymentRepository;


    // Constructor
    public DividendPaymentCalculationServiceImpl(DividendPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Calculation functions

    // Calculate total dividends
    @Override
    public BigDecimal calculateTotalDividends(UUID accountId) {
        BigDecimal result =  paymentRepository.calculateTotalDividendsByAccount(accountId);
        return result != null ? result : BigDecimal.ZERO;

    }

    // Calculate within date range
    public BigDecimal calculateDividendsInDateRange(UUID accountId,
                                                    LocalDateTime start,
                                                    LocalDateTime end) {
        return paymentRepository.calculateDividendsInDateRange(accountId, start, end);
    }

    // Calculate for a stock
    public BigDecimal calculateDividendsForStock(UUID accountId, UUID stockId) {
        return paymentRepository.calculateTotalDividendsByAccountAndStock(accountId, stockId);
    }
}