package com.joelcode.personalinvestmentportfoliotracker.services.dividend;

import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Profile("!test")
public class DividendValidationService {

    // Define key field
    private final DividendRepository dividendRepository;


    // Constructor
    public DividendValidationService(DividendRepository dividendRepository) {
        this.dividendRepository = dividendRepository;
    }


    // Validation functions

    // Validate existence
    public Dividend validateDividendExists(UUID dividendId) {
        return dividendRepository.findById(dividendId)
                .orElseThrow(() -> new RuntimeException("Dividend not found with ID: " + dividendId));
    }

    // Validate creation request
    public void validateCreateRequest(BigDecimal amount, LocalDateTime paymentDate) {
        validateAmount(amount);
        validatePaymentDate(paymentDate);
    }

    // Validate amount
    public void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Dividend amount must be greater than zero.");
        }
    }

    // Validate pay date
    public void validatePaymentDate(LocalDateTime paymentDate) {
        if (paymentDate == null) {
            throw new RuntimeException("Payment date cannot be null.");
        }
        if (paymentDate.isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Payment date cannot be in the future.");
        }
    }
}
