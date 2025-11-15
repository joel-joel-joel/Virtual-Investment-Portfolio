package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class HoldingValidationService {

    private final HoldingRepository holdingRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;

    public HoldingValidationService(HoldingRepository holdingRepository,
                                    AccountRepository accountRepository,
                                    StockRepository stockRepository) {
        this.holdingRepository = holdingRepository;
        this.accountRepository = accountRepository;
        this.stockRepository = stockRepository;
    }

    public Holding validateHoldingExists(UUID holdingId) {
        return holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found with ID: " + holdingId));
    }

    public Account validateAccountExists(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
    }

    public Stock validateStockExists(UUID stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + stockId));
    }

    public void validateHoldingDoesNotExist(Account account, Stock stock) {
        if (holdingRepository.existsByAccountAndStock(account, stock)) {
            throw new RuntimeException("Holding already exists for this account and stock combination.");
        }
    }

    public void validateCreateRequest(BigDecimal quantity, BigDecimal averageCostBasis, BigDecimal totalCostBasis) {
        validateQuantity(quantity);
        validateAverageCostBasis(averageCostBasis);
        validateTotalCostBasis(totalCostBasis);
    }

    public void validateUpdateRequest(BigDecimal quantity, BigDecimal averageCostBasis, BigDecimal totalCostBasis) {
        if (quantity != null) {
            validateQuantity(quantity);
        }
        if (averageCostBasis != null) {
            validateAverageCostBasis(averageCostBasis);
        }
        if (totalCostBasis != null) {
            validateTotalCostBasis(totalCostBasis);
        }
    }

    public void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Quantity must be greater than zero.");
        }
    }

    public void validateAverageCostBasis(BigDecimal averageCostBasis) {
        if (averageCostBasis == null || averageCostBasis.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Average cost basis must be greater than zero.");
        }
    }

    public void validateTotalCostBasis(BigDecimal totalCostBasis) {
        if (totalCostBasis == null || totalCostBasis.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total cost basis must be greater than zero.");
        }
    }
}