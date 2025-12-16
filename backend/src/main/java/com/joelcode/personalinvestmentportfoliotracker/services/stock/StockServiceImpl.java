package com.joelcode.personalinvestmentportfoliotracker.services.stock;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PriceHistoryRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.StockMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("!test")
public class StockServiceImpl implements StockService {

    // Define key fields
    private final StockRepository stockRepository;
    private final StockValidationService stockValidationService;
    private final PriceHistoryRepository priceHistoryRepository;
    private final FinnhubApiClient finnhubApiClient;


    // Constructor
    public StockServiceImpl(StockRepository stockRepository, StockValidationService stockValidationService, PriceHistoryRepository priceHistoryRepository, FinnhubApiClient finnhubApiClient) {
        this.stockRepository = stockRepository;
        this.stockValidationService = stockValidationService;
        this.priceHistoryRepository = priceHistoryRepository;
        this.finnhubApiClient = finnhubApiClient;
    }


    // Interface function

    // Create a new stock and show essential information
    @Override
    public StockDTO createStock(StockCreateRequest request) {

        // Check that code is unique and request is valid
        stockValidationService.validateCreateRequest(request.getStockCode(), request.getCompanyName());
        stockValidationService.validateStockCodeIsUnique(request.getStockCode());

        // Map stock creation request to entity
        Stock stock = StockMapper.toEntity(request);

        // Save to db
        stock = stockRepository.save(stock);

        // Map entity to dto
        return StockMapper.toDTO(stock);
    }

    // Find stock by ID
    @Override
    public StockDTO getStockById(UUID id) {

        // Validate stock exists
        Stock stock = stockValidationService.validateStockExists(id);

        return StockMapper.toDTO(stock);
    }

    // Find stock by symbol
    @Override
    public StockDTO getStockBySymbol(String symbol) {
        Optional<Stock> stock = stockRepository.findByStockCode(symbol.toUpperCase());
        return stock.map(StockMapper::toDTO).orElse(null);
    }

    // Generate a list of all the sticks inclusive of their information
    @Override
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll()
                .stream()
                .map(StockMapper::toDTO)
                .toList();
    }

    // Update stock entity by given stockId
    @Override
    public StockDTO updateStock (UUID id, StockUpdateRequest request) {
        // Validate stock exists
        Stock stock = stockValidationService.validateStockExists(id);

        // Validate update request
        stockValidationService.validateUpdateRequest(request.getStockCode(), request.getCompanyName());

        // If stock code changes, validate new uniqueness
        if (!stock.getStockCode().equals(request.getStockCode())) {
            stockValidationService.validateStockCodeIsUnique(request.getStockCode());
        }

        // Update entity via mapper helper
        StockMapper.updateEntity(stock, request);

        // Save updated entity
        stock = stockRepository.save(stock);

        return StockMapper.toDTO(stock);
    }

    // Get current price
    @Override
    public BigDecimal getCurrentPrice(UUID stockId) {

        // Validate stock exists
        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new IllegalArgumentException("Stock with ID " + stockId + " does not exist."));

        try {
            // Fetch real-time price from FinnHub
            BigDecimal realtimePrice = finnhubApiClient.getCurrentPrice(stock.getStockCode());
            if (realtimePrice != null && realtimePrice.compareTo(BigDecimal.ZERO) > 0) {
                // Update stock value with real-time price
                stock.setStockValue(realtimePrice);
                stockRepository.save(stock);

                // Optionally save to price history for tracking
                savePriceHistory(stock, realtimePrice);

                return realtimePrice;
            }
        } catch (Exception e) {
            // Silently fall back to database if FinnHub call fails
        }

        // Fallback: Try to fetch latest price history record
        Optional<PriceHistory> latestPrice = priceHistoryRepository
                .findTopByStockOrderByCloseDateDesc(stock);

        // If exists, return close price, otherwise fallback to stock.stockValue
        return latestPrice
                .map(PriceHistory::getClosePrice)
                .orElse(stock.getStockValue());
    }

    // Helper method to save price history
    private void savePriceHistory(Stock stock, BigDecimal price) {
        try {
            PriceHistory priceHistory = new PriceHistory();
            priceHistory.setStock(stock);
            priceHistory.setClosePrice(price);
            priceHistory.setCloseDate(LocalDateTime.now());
            priceHistoryRepository.save(priceHistory);
        } catch (Exception e) {
            // Silently ignore if price history save fails
        }
    }

    // Helper method to fetch and populate missing industry data from FinnHub
    @Override
    public void populateMissingIndustryData(Stock stock) {
        if (stock == null || stock.getIndustry() != null) {
            return; // Skip if stock is null or industry is already set
        }

        try {
            // Fetch company profile from FinnHub
            var profile = finnhubApiClient.getCompanyProfile(stock.getStockCode());
            if (profile != null && profile.getIndustry() != null) {
                stock.setIndustry(profile.getIndustry());
                stockRepository.save(stock);
            }
        } catch (Exception e) {
            // Silently ignore if FinnHub call fails
            System.err.println("Failed to fetch industry for " + stock.getStockCode() + ": " + e.getMessage());
        }
    }


    // Delete stock
    @Override
    public void deleteStock(UUID id) {
        Stock stock = stockValidationService.validateStockExists(id);
        stockRepository.delete(stock);
    }

}