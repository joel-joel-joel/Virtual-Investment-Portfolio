package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountValidationService;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryServiceImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HoldingServiceImpl implements HoldingService {

    // Define key fields
    private final HoldingRepository holdingRepository;
    private final HoldingValidationService holdingValidationService;
    private final AccountValidationService accountValidationService;
    private final PriceHistoryServiceImpl priceHistoryService;
    private final WebSocketController webSocketController;
    private final SimpMessagingTemplate messagingTemplate;

    // Constructor
    public HoldingServiceImpl(HoldingRepository holdingRepository,
                              HoldingValidationService holdingValidationService,
                              AccountValidationService accountValidationService,
                              PriceHistoryServiceImpl priceHistoryService,
                              WebSocketController webSocketController,
                              SimpMessagingTemplate messagingTemplate) {
        this.holdingRepository = holdingRepository;
        this.holdingValidationService = holdingValidationService;
        this.accountValidationService = accountValidationService;
        this.priceHistoryService = priceHistoryService;
        this.webSocketController = webSocketController;
        this.messagingTemplate = messagingTemplate;
    }

    // Interface function

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

        WebSocketController.HoldingUpdateMessage updateMessage = new WebSocketController.HoldingUpdateMessage(
                holding.getAccount().getAccountId(),
                holding.getStock().getStockId(),
                holding.getQuantity(),
                holding.getTotalCostBasis(),
                holding.getAverageCostBasis(),
                holding.getRealizedGain(),
                LocalDateTime.now()
        );

        // Broadcast update to all subscribers for this account
        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + holding.getAccount().getAccountId(),
                updateMessage
        );

        // Convert entity -> DTO
        return HoldingMapper.toDTO(holding, stock.getStockValue());
    }

    // Get holding by ID
    @Override
    public HoldingDTO getHoldingById(UUID holdingId) {
        Holding holding = holdingValidationService.validateHoldingExists(holdingId);
        return HoldingMapper.toDTO(holding, holding.getStock().getStockValue());
    }

    // Get all holdings
    @Override
    public List<HoldingDTO> getAllHoldings() {
        return holdingRepository.findAll()
                .stream()
                .map(holding -> HoldingMapper.toDTO(holding, holding.getStock().getStockValue()))
                .collect(Collectors.toList());
    }

    // Get holdings by account
    @Override
    public List<HoldingDTO> getHoldingsByAccount(UUID accountId) {
        Account account = holdingValidationService.validateAccountExists(accountId);
        return holdingRepository.findByAccount(account)
                .stream()
                .map(holding -> HoldingMapper.toDTO(holding, holding.getStock().getStockValue()))
                .collect(Collectors.toList());
    }

    // Update holding
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
        if (request.getRealizedGain() != null) {
            holding.setRealizedGain(request.getRealizedGain());
        }

        // Save to DB
        holding = holdingRepository.save(holding);

        WebSocketController.HoldingUpdateMessage updateMessage = new WebSocketController.HoldingUpdateMessage(
                holding.getAccount().getAccountId(),
                holding.getStock().getStockId(),
                holding.getQuantity(),
                holding.getTotalCostBasis(),
                holding.getAverageCostBasis(),
                holding.getRealizedGain(),
                LocalDateTime.now()
        );

        // Broadcast update to all subscribers for this account
        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + holding.getAccount().getAccountId(),
                updateMessage
        );


        // Convert entity -> DTO
        return HoldingMapper.toDTO(holding, holding.getStock().getStockValue());
    }

    // Transactional type methods

    // Update holding after sale
    @Override
    public void updateHoldingAfterSale(Holding holding, BigDecimal quantitySold, BigDecimal salePrice) {
        BigDecimal avgCost = holding.getAverageCostBasis();
        BigDecimal realizedGain = quantitySold.multiply(salePrice.subtract(avgCost));

        // Update holding
        holding.setQuantity(holding.getQuantity().subtract(quantitySold));
        holding.setRealizedGain(holding.getRealizedGain().add(realizedGain));
        holding.setTotalCostBasis(holding.getAverageCostBasis().multiply(holding.getQuantity()));

        holdingRepository.save(holding);

        WebSocketController.HoldingUpdateMessage updateMessage = new WebSocketController.HoldingUpdateMessage(
                holding.getAccount().getAccountId(),
                holding.getStock().getStockId(),
                holding.getQuantity(),
                holding.getTotalCostBasis(),
                holding.getAverageCostBasis(),
                holding.getRealizedGain(),
                LocalDateTime.now()
        );

        // Broadcast update to all subscribers for this account
        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + holding.getAccount().getAccountId(),
                updateMessage
        );

    }

    // Update or create holding from transaction
    @Override
    public void updateOrCreateHoldingFromTransaction(TransactionCreateRequest request) {

        UUID accountId = request.getAccountId();
        UUID stockId = request.getStockId();
        BigDecimal quantity = request.getShareQuantity();
        BigDecimal pricePerShare = request.getPricePerShare();

        // Try to fetch existing holding
        Optional<Holding> holdingOpt = holdingRepository.getHoldingByAccount_AccountIdAndStock_StockId(request.getAccountId(), request.getStockId());

        if (holdingOpt.isPresent()) {
            Holding holding = holdingOpt.get();

            if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
                // Recalculate average cost basis
                BigDecimal totalCost = holding.getTotalCostBasis().add(quantity.multiply(pricePerShare));
                BigDecimal totalShares = holding.getQuantity().add(quantity);
                BigDecimal avgCost;
                if (totalShares.compareTo(BigDecimal.ZERO) == 0) {
                    avgCost = BigDecimal.ZERO;
                } else {
                    avgCost = totalCost.divide(totalShares, 2, RoundingMode.HALF_UP);
                }

                holding.setQuantity(totalShares);
                holding.setAverageCostBasis(avgCost);
                holding.setTotalCostBasis(totalCost);

            } else if (request.getTransactionType().name().equalsIgnoreCase("SELL")) {
                // Reduce quantity and calculate realized gain
                BigDecimal realizedGain = quantity.multiply(pricePerShare.subtract(holding.getAverageCostBasis()));
                holding.setQuantity(holding.getQuantity().subtract(quantity));
                holding.setRealizedGain(holding.getRealizedGain().add(realizedGain));
                holding.setTotalCostBasis(holding.getAverageCostBasis().multiply(holding.getQuantity()));
            }

            holdingRepository.save(holding);

            WebSocketController.HoldingUpdateMessage updateMessage = new WebSocketController.HoldingUpdateMessage(
                    holding.getAccount().getAccountId(),
                    holding.getStock().getStockId(),
                    holding.getQuantity(),
                    holding.getTotalCostBasis(),
                    holding.getAverageCostBasis(),
                    holding.getRealizedGain(),
                    LocalDateTime.now()
            );

            // Broadcast update to all subscribers for this account
            messagingTemplate.convertAndSend(
                    "/topic/portfolio/" + holding.getAccount().getAccountId(),
                    updateMessage
            );

        } else {
            // No holding exists → create new only for BUY
            if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
                Holding newHolding = new Holding();
                newHolding.setAccount(holdingValidationService.validateAccountExists(accountId));
                newHolding.setStock(holdingValidationService.validateStockExists(stockId));
                newHolding.setQuantity(quantity);
                newHolding.setAverageCostBasis(pricePerShare);
                newHolding.setTotalCostBasis(pricePerShare.multiply(quantity));
                newHolding.setRealizedGain(BigDecimal.ZERO);

                holdingRepository.save(newHolding);
            } else {
                throw new IllegalArgumentException("Cannot sell stock you don't hold");
            }
        }

    }

    // Delete holding
    @Override
    public void deleteHolding(UUID id) {
        Holding holding = holdingValidationService.validateHoldingExists(id);
        holdingRepository.delete(holding);
    }

    // Get holdings for account
    @Override
    public List<HoldingDTO> getHoldingsForAccount(UUID accountId) {
        // Validate account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Stream through holdings and map to DTOs with current price
        List<HoldingDTO> holdingDTOs = account.getHoldings().stream()
                .map(h -> {
                    BigDecimal currentPrice = priceHistoryService.getCurrentPrice(h.getStock().getStockId());
                    return HoldingMapper.toDTO(h, currentPrice);
                })
                .collect(Collectors.toList());

        return holdingDTOs;
    }


}