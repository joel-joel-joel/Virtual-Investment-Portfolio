package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import com.joelcode.personalinvestmentportfoliotracker.exceptions.CustomAuthenticationException;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PriceHistoryRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.PriceHistoryMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PriceHistoryServiceImpl implements PriceHistoryService{

    // Define key fields
    private final PriceHistoryRepository priceHistoryRepository;
    private final PriceHistoryValidationService validationService;

    // Constructor
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository,
                                   PriceHistoryValidationService validationService) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.validationService = validationService;
    }

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

    @Override
    public PriceHistoryDTO getPriceHistoryById(UUID id) {
        PriceHistory priceHistory = validationService.validatePriceHistoryExists(id);
        return PriceHistoryMapper.toDTO(priceHistory);
    }

    @Override
    public List<PriceHistoryDTO> getAllPriceHistories() {
        return priceHistoryRepository.findAll()
                .stream()
                .map(PriceHistoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePriceHistory(UUID priceHistoryId) {
        PriceHistory priceHistory = validationService.validatePriceHistoryExists(priceHistoryId);
        priceHistoryRepository.delete(priceHistory);
    }

    @Override
    public BigDecimal getCurrentPrice(UUID stockId) {
        return priceHistoryRepository.findLatestPriceByStockId(stockId)
                .orElseThrow(() -> new CustomAuthenticationException("No price found for stock " + stockId));
    }

    @Override
    public List<PriceHistoryDTO> getPriceHistoryForStock(UUID stockId) {
        // Fetch all price history records for the stock
        List<PriceHistory> historyList = priceHistoryRepository.findByStock_IdOrderByDateAsc(stockId);

        // Map to DTOs
        List<PriceHistoryDTO> dtos = historyList.stream()
                .map(PriceHistoryMapper::toDTO)
                .collect(Collectors.toList());

        return dtos;
    }

    @Override
    public PriceHistoryDTO getLatestPriceForStock(UUID stockId) {
        // Fetch latest price (order by date descending, get first)
        Optional<PriceHistory> latest = priceHistoryRepository
                .findTopByStock_IdOrderByDateDesc(stockId);

        return latest.map(PriceHistoryMapper::toDTO)
                .orElse(null); // returns null if no price exists
    }


}
