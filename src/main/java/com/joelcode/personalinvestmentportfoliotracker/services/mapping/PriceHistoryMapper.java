package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;

public class PriceHistoryMapper {

    // Convert price history creation request DTO to entity
    public static PriceHistory toEntity(PriceHistoryCreateRequest request) {
        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setCloseDate(request.getCloseDate());
        priceHistory.setClosePrice(request.getClosePrice());
        priceHistory.setStockId(request.getStockId());
        return priceHistory;
    }

    // Convert price history entity to price history response DTO
    public static PriceHistoryDTO toDTO(PriceHistory priceHistory) {
        if (priceHistory == null) return null;
        return new PriceHistoryDTO(priceHistory.getPriceHistoryId(),
                priceHistory.getCloseDate(),
                priceHistory.getClosePrice(),
                priceHistory.getStock().getStockId());
    }
}
