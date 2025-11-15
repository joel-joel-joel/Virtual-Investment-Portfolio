package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HoldingServiceImpl implements HoldingService {

    // Define key fields
    private final HoldingRepository holdingRepository;
    private final HoldingValidationService holdingValidationService;

    // Constructor
    public HoldingServiceImpl(HoldingRepository holdingRepository,
                              HoldingValidationService holdingValidationService) {
        this.holdingRepository = holdingRepository;
        this.holdingValidationService = holdingValidationService;
    }

    // Create holding entity from request dto
    @Override
    public HoldingDTO createHolding(HoldingCreateRequest request) {

        // Validate fields and relationships
        Account account = holdingValidationService.validateAccountExists(request.getAccountId());
        Stock stock = holdingValidationService.validateStockExists(request.getStockId());
        holdingValidationService.validateCreateRequest(
                request.getQuantity(),
                request.getAverageCostBasis(),
                request.getTotalCostBasis()
        );

        // Check if holding already exists for this account-stock pair
        holdingValidationService.validateHoldingDoesNotExist(account, stock);

        // Map request -> entity
        Holding holding = HoldingMapper.toEntity(request, account, stock);

        // Save to DB
        holding = holdingRepository.save(holding);

        // Convert entity -> DTO
        return HoldingMapper.toDTO(holding, stock.getCurrentPrice());
    }

    @Override
    public HoldingDTO getHoldingById(UUID holdingId) {
        Holding holding = holdingValidationService.validateHoldingExists(holdingId);
        return HoldingMapper.toDTO(holding, holding.getStock().getCurrentPrice());
    }

    @Override
    public List<HoldingDTO> getAllHoldings() {
        return holdingRepository.findAll()
                .stream()
                .map(holding -> HoldingMapper.toDTO(holding, holding.getStock().getCurrentPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public List<HoldingDTO> getHoldingsByAccount(UUID accountId) {
        Account account = holdingValidationService.validateAccountExists(accountId);
        return holdingRepository.findByAccount(account)
                .stream()
                .map(holding -> HoldingMapper.toDTO(holding, holding.getStock().getCurrentPrice()))
                .collect(Collectors.toList());
    }

    @Override
    public HoldingDTO updateHolding(UUID holdingId, HoldingUpdateRequest request) {

        // Validate holding exists
        Holding holding = holdingValidationService.validateHoldingExists(holdingId);

        // Validate update request
        holdingValidationService.validateUpdateRequest(
                request.getQuantity(),
                request.getAverageCostBasis(),
                request.getTotalCostBasis()
        );

        // Update fields if provided
        if (request.getQuantity() != null) {
            holding.setQuantity(request.getQuantity());
        }
        if (request.getAverageCostBasis() != null) {
            holding.setAverageCostBasis(request.getAverageCostBasis());
        }
        if (request.getTotalCostBasis() != null) {
            holding.setTotalCostBasis(request.getTotalCostBasis());
        }
        if (request.getRealizedGainLoss() != null) {
            holding.setRealizedGainLoss(request.getRealizedGainLoss());
        }

        // Save to DB
        holding = holdingRepository.save(holding);

        // Convert entity -> DTO
        return HoldingMapper.toDTO(holding, holding.getStock().getCurrentPrice());
    }

    @Override
    public void deleteHolding(UUID holdingId) {
        Holding holding = holdingValidationService.validateHoldingExists(holdingId);
        holdingRepository.delete(holding);
    }
}