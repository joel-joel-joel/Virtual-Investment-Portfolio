package com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.DividendPayment;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendPaymentRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Profile("!test")
public class DividendPaymentValidationService {

    // Define key fields
    private final DividendPaymentRepository paymentRepository;
    private final DividendRepository dividendRepository;
    private final AccountRepository accountRepository;

    // Constructor
    public DividendPaymentValidationService(DividendPaymentRepository paymentRepository,
                                            DividendRepository dividendRepository,
                                            AccountRepository accountRepository) {
        this.paymentRepository = paymentRepository;
        this.dividendRepository = dividendRepository;
        this.accountRepository = accountRepository;
    }


    // Validation functions

    // Validate payment exists
    public DividendPayment validatePaymentExists(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Dividend payment not found with ID: " + paymentId));
    }

    // Validate account exists
    public void validateAccountExists(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new RuntimeException("Account not found with ID: " + accountId);
        }
    }

    // Validate creation request
    public void validateCreateRequest(DividendPaymentCreateRequest request) {
        if (request.getAccountId() == null) {
            throw new RuntimeException("Account ID cannot be null");
        }
        if (request.getDividendId() == null) {
            throw new RuntimeException("Dividend ID cannot be null");
        }
        validateShareQuantity(request.getShareQuantity());
    }

    // Validate share quantity
    public void validateShareQuantity(BigDecimal shareQuantity) {
        if (shareQuantity == null || shareQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Share quantity must be greater than zero");
        }
    }

    // Validate by date range
    public void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new RuntimeException("Start and end dates cannot be null");
        }
        if (start.isAfter(end)) {
            throw new RuntimeException("Start date must be before end date");
        }
    }
}