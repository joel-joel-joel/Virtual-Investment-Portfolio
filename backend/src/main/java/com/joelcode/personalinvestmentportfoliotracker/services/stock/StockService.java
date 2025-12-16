package com.joelcode.personalinvestmentportfoliotracker.services.stock;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockService {

    StockDTO createStock(StockCreateRequest request);

    StockDTO updateStock(UUID id, StockUpdateRequest request);

    StockDTO getStockById(UUID id);

    StockDTO getStockBySymbol(String symbol);

    List<StockDTO> getAllStocks();

    BigDecimal getCurrentPrice(UUID stockId);

    void deleteStock(UUID id);

    void populateMissingIndustryData(Stock stock);
}
