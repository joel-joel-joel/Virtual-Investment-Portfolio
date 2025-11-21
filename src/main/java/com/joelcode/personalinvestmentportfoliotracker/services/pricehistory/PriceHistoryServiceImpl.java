package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import com.joelcode.personalinvestmentportfoliotracker.exceptions.CustomAuthenticationException;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PriceHistoryRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.PriceHistoryMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class PriceHistoryServiceImpl implements PriceHistoryService{

    // Define key fields
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceHistoryValidationService validationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketController webSocketController;


    // Constructor
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository,
                                   PriceHistoryValidationService validationService,
                                   SimpMessagingTemplate messagingTemplate,
                                   WebSocketController webSocketController) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.validationService = validationService;
        this.messagingTemplate = messagingTemplate;
        this.webSocketController = webSocketController;
    }


    // Interface function

    // Create price history entity from request dto
    @Override
    public PriceHistoryDTO createPriceHistory(PriceHistoryCreateRequest request) {

        // Check that creation request is calid
        validationService.validateCreateRequest(request.getStockId(), request.getClosePrice());

        // Create entity from dto request
        PriceHistory priceHistory = PriceHistoryMapper.toEntity(request);

        // Save to db
        priceHistory = priceHistoryRepository.save(priceHistory);

        // Map to dto
        return PriceHistoryMapper.toDTO(priceHistory);
    }

    // Get price history by ID
    @Override
    public PriceHistoryDTO getPriceHistoryById(UUID id) {
        PriceHistory priceHistory = validationService.validatePriceHistoryExists(id);
        return PriceHistoryMapper.toDTO(priceHistory);
    }

    // Get all price histories
    @Override
    public List<PriceHistoryDTO> getAllPriceHistories() {
        return priceHistoryRepository.findAll()
                .stream()
                .map(PriceHistoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Delete price history
    @Override
    public void deletePriceHistory(UUID priceHistoryId) {
        PriceHistory priceHistory = validationService.validatePriceHistoryExists(priceHistoryId);
        priceHistoryRepository.delete(priceHistory);
    }

    // Get current price
    @Override
    public BigDecimal getCurrentPrice(UUID stockId) {
        return priceHistoryRepository.findTopByStock_StockIdOrderByCloseDateDesc(stockId).map(PriceHistory::getClosePrice)
                .orElseThrow(() -> new CustomAuthenticationException("No price found for stock " + stockId));
    }

    // Get price history for stock
    @Override
    public List<PriceHistoryDTO> getPriceHistoryForStock(UUID stockId) {
        // Fetch all price history records for the stock
        List<PriceHistory> historyList = priceHistoryRepository.findByStock_StockIdOrderByCloseDateAsc(stockId);

        // Map to DTOs
        List<PriceHistoryDTO> dtos = historyList.stream()
                .map(PriceHistoryMapper::toDTO)
                .collect(Collectors.toList());

        return dtos;
    }

    // Get latest price for stock
    @Override
    public PriceHistoryDTO getLatestPriceForStock(UUID stockId) {
        // Fetch latest price from repository
        Optional<PriceHistory> latest = priceHistoryRepository
                .findTopByStock_StockIdOrderByCloseDateDesc(stockId);

        if (latest.isPresent()) {
            PriceHistory priceHistory = latest.get();
            String stockCode = priceHistory.getStock().getStockCode();
            BigDecimal latestPrice = priceHistory.getClosePrice();

            // Broadcast via WebSocket using centralized controller method
            webSocketController.broadcastStockPriceUpdate(stockId, stockCode, latestPrice);

            // Return DTO to caller
            return PriceHistoryMapper.toDTO(priceHistory);
        } else {
            return null; // or throw an exception if preferred
        }
    }

}