package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PriceHistoryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Profile("!test")
public class PriceHistoryValidationService {

    // Define key field
    private final PriceHistoryRepository priceHistoryRepository;


    // Constructor
    public PriceHistoryValidationService(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }


    // Validation functions

    // Check that price history exists by id
    public PriceHistory validatePriceHistoryExists(UUID priceHistoryId) {
        return priceHistoryRepository.findById(priceHistoryId)
                .orElseThrow(() -> new IllegalArgumentException("Price history entry not found for id: " + priceHistoryId));
    }

    // Validate price history creation request
    public void validateCreateRequest(UUID stockId, BigDecimal closePrice) {
        if (stockId == null) {
            throw new IllegalArgumentException("Stock ID cannot be null.");
        }
        if (closePrice == null || closePrice.doubleValue() < 0) {
            throw new IllegalArgumentException("Close price must be a positive number.");
        }
    }
}