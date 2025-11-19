package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class HoldingMapper {

    // Convert holding creation request DTO to entity
    public static Holding toEntity(HoldingCreateRequest request, Account account, Stock stock) {
        Holding holding = new Holding();
        holding.setAccount(account);
        holding.setStock(stock);
        holding.setQuantity(request.getQuantity());
        holding.setAverageCostBasis(request.getAverageCostBasis());
        holding.setTotalCostBasis(request.getTotalCostBasis());
        holding.setRealizedGain(BigDecimal.ZERO);
        holding.setFirstPurchaseDate(LocalDateTime.now());
        return holding;
    }

    // Update holding entity from update request DTO
    public static void updateEntity(Holding holding, HoldingUpdateRequest request) {
        if (request.getQuantity() != null) {holding.setQuantity(request.getQuantity());}
        if (request.getAverageCostBasis() != null) {holding.setAverageCostBasis(request.getAverageCostBasis());}
        if (request.getTotalCostBasis() != null) {holding.setTotalCostBasis(request.getTotalCostBasis());}
        if (request.getRealizedGain() != null) {holding.setRealizedGain(request.getRealizedGain());}
    }

    // Convert holding entity to holding response DTO
    public static HoldingDTO toDTO(Holding holding, BigDecimal currentPrice) {
        if (holding == null) return null;
        return new HoldingDTO(
                holding.getHoldingId(),
                holding.getAccount().getAccountId(),
                holding.getStock().getStockId(),
                holding.getStock().getStockCode(),
                holding.getQuantity(),
                holding.getAverageCostBasis(),
                holding.getTotalCostBasis(),
                holding.getRealizedGain(),
                holding.getFirstPurchaseDate(),
                currentPrice,
                holding.getCurrentValue(currentPrice),
                holding.getUnrealizedGain(currentPrice),
                holding.getUnrealizedGainPercent(currentPrice)
        );
    }
}