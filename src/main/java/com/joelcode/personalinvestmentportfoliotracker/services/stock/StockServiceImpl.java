package com.joelcode.personalinvestmentportfoliotracker.services.stock;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PriceHistoryRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.StockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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


    // Constructor
    public StockServiceImpl(StockRepository stockRepository, StockValidationService stockValidationService, PriceHistoryRepository priceHistoryRepository) {
        this.stockRepository = stockRepository;
        this.stockValidationService = stockValidationService;
        this.priceHistoryRepository = priceHistoryRepository;
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

        // Try to fetch latest price history record
        Optional<PriceHistory> latestPrice = priceHistoryRepository
                .findTopByStockOrderByCloseDateDesc(stock);

        // If exists, return close price, otherwise fallback to stock.stockValue
        return latestPrice
                .map(PriceHistory::getClosePrice)
                .orElse(stock.getStockValue());
    }


    // Delete stock
    @Override
    public void deleteStock(UUID id) {
        Stock stock = stockValidationService.validateStockExists(id);
        stockRepository.delete(stock);
    }

}