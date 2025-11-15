package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryDTO;

import java.util.List;
import java.util.UUID;

public interface PriceHistoryService {

    PriceHistoryDTO createPriceHistory(PriceHistoryCreateRequest request);

    PriceHistoryDTO getPriceHistoryById(UUID id);

    List<PriceHistoryDTO> getAllPriceHistories();

    void deletePriceHistory(UUID id);
}
