package com.joelcode.personalinvestmentportfoliotracker.services.stock;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("!test")
public class StockValidationService {

    // Define key field
    private final StockRepository stockRepository;


    // Constructor
    public StockValidationService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }


    // Validation functions

    // Check stock exists
    public Stock validateStockExists(UUID stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock with ID " + stockId + " does not exist."));
    }

    // Check that unique stockCode exists
    public void validateStockCodeIsUnique(String stockCode) {
        if (stockRepository.existsByStockCode(stockCode)) {
            throw new IllegalArgumentException("Stock Code '" + stockCode + "' already exists.");
        }
    }

    // Check that stockCode and company name is valid for stock creation request
    public void validateCreateRequest(String stockCode, String companyName) {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("Stock Code cannot be empty.");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }

        validateStockCodeIsUnique(stockCode);
    }

    // Check that stockCode and company name is valid for stock update request
    public void validateUpdateRequest(String stockCode, String companyName) {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("Stock Code cannot be empty.");
        }
        if (companyName == null || companyName.isBlank()) {
            throw new IllegalArgumentException("Company name cannot be empty.");
        }
    }
}
