package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HoldingServiceImpl implements HoldingService {

    // Define key fields
    private final HoldingRepository holdingRepository;
    private final HoldingValidationService holdingValidationService;
    private final HoldingService holdingService;

    // Constructor
    public HoldingServiceImpl(HoldingRepository holdingRepository,
                              HoldingValidationService holdingValidationService, HoldingService holdingService) {
        this.holdingRepository = holdingRepository;
        this.holdingValidationService = holdingValidationService;
        this.holdingService = holdingService;
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
        return HoldingMapper.toDTO(holding, BigDecimal.valueOf(stock.getStockValue()));
    }

    @Override
    public HoldingDTO getHoldingById(UUID holdingId) {
        Holding holding = holdingValidationService.validateHoldingExists(holdingId);
        return HoldingMapper.toDTO(holding, BigDecimal.valueOf(holding.getStock().getStockValue()));
    }

    @Override
    public List<HoldingDTO> getAllHoldings() {
        return holdingRepository.findAll()
                .stream()
                .map(holding -> HoldingMapper.toDTO(holding, BigDecimal.valueOf(holding.getStock().getStockValue())))
                .collect(Collectors.toList());
    }

    @Override
    public List<HoldingDTO> getHoldingsByAccount(UUID accountId) {
        Account account = holdingValidationService.validateAccountExists(accountId);
        return holdingRepository.findByAccount(account)
                .stream()
                .map(holding -> HoldingMapper.toDTO(holding, BigDecimal.valueOf(holding.getStock().getStockValue())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Holding> getHoldingsEntitiesByAccount(UUID accountId) {
        return holdingRepository.findAllByAccountId(accountId);
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
        return HoldingMapper.toDTO(holding, BigDecimal.valueOf(holding.getStock().getStockValue()));
    }

    @Override
    public void deleteHolding(UUID holdingId) {
        Holding holding = holdingValidationService.validateHoldingExists(holdingId);
        holdingRepository.delete(holding);
    }

    // Transactional type methods

    @Override
    public Holding getHoldingByAccountIdAndStockId(UUID accountId, UUID stockId) {
        Account account = holdingValidationService.validateAccountExists(accountId);
        Stock stock = holdingValidationService.validateStockExists(stockId);
        Holding holding = holdingRepository.findByAccountAndStock(account, stock)
                .orElseThrow(() -> new RuntimeException("No holding found for account ID: " + accountId + " and stock ID: " + stockId));
        return holding;
    }

    @Override
    public void updateHoldingAfterSale(Holding holding, BigDecimal quantitySold, BigDecimal salePrice) {
        BigDecimal avgCost = holding.getAverageCostBasis();
        BigDecimal realizedGain = quantitySold.multiply(salePrice.subtract(avgCost));

        // Update holding
        holding.setQuantity(holding.getQuantity().subtract(quantitySold));
        holding.setRealizedGainLoss(holding.getRealizedGainLoss().add(realizedGain));
        holding.setTotalCostBasis(holding.getAverageCostBasis().multiply(holding.getQuantity()));

        holdingRepository.save(holding);
    }

    @Override
    public void updateOrCreateHoldingFromTransaction(TransactionCreateRequest request) {

        UUID accountId = request.getAccountId();
        UUID stockId = request.getStockId();
        BigDecimal quantity = request.getShareQuantity();
        BigDecimal pricePerShare = request.getPricePerShare();

        // Try to fetch existing holding
        Holding holding = holdingService.getHoldingByAccountIdAndStockId(request.getAccountId(), request.getStockId());

        if (holding != null) {

            if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
                // Recalculate average cost basis
                BigDecimal totalCost = holding.getTotalCostBasis().add(quantity.multiply(pricePerShare));
                BigDecimal totalShares = holding.getQuantity().add(quantity);
                BigDecimal avgCost = totalCost.divide(totalShares, 2, BigDecimal.ROUND_HALF_UP);

                holding.setQuantity(totalShares);
                holding.setAverageCostBasis(avgCost);
                holding.setTotalCostBasis(totalCost);

            } else if (request.getTransactionType().name().equalsIgnoreCase("SELL")) {
                // Reduce quantity and calculate realized gain
                BigDecimal realizedGain = quantity.multiply(pricePerShare.subtract(holding.getAverageCostBasis()));
                holding.setQuantity(holding.getQuantity().subtract(quantity));
                holding.setRealizedGainLoss(holding.getRealizedGainLoss().add(realizedGain));
                holding.setTotalCostBasis(holding.getAverageCostBasis().multiply(holding.getQuantity()));
            }

            holdingRepository.save(holding);

        } else {
            // No holding exists → create new only for BUY
            if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
                Holding newHolding = new Holding();
                newHolding.setAccount(holdingValidationService.validateAccountExists(accountId));
                newHolding.setStock(holdingValidationService.validateStockExists(stockId));
                newHolding.setQuantity(quantity);
                newHolding.setAverageCostBasis(pricePerShare);
                newHolding.setTotalCostBasis(pricePerShare.multiply(quantity));
                newHolding.setRealizedGainLoss(BigDecimal.ZERO);

                holdingRepository.save(newHolding);
            } else {
                throw new IllegalArgumentException("Cannot sell stock you don't hold");
            }
        }
    }

}