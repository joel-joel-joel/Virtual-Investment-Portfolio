package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;

public class StockMapper {

    // Convert stock creation request DTO to entity
    public static Stock toEntity(StockCreateRequest request){
        Stock stock = new Stock();
        stock.setStockCode(request.getStockCode());
        stock.setCompanyName(request.getCompanyName());
        stock.setStockValue(request.getStockValue());
        return stock;
    }

    // Update stock entity from update request DTO
    public static void updateEntity(Stock stock, StockUpdateRequest request){
        if (request.getStockCode() != null) {stock.setStockCode(request.getStockCode());}
        if (request.getCompanyName() != null) {stock.setCompanyName(request.getCompanyName());}
        if (request.getStockValue() != null) {stock.setStockValue(request.getStockValue());}
    }

    // Convert stock entity from stock response DTO
    public static StockDTO toDTO(Stock stock){
        if (stock == null) return null;
        return new StockDTO(stock.getStockId(),
                stock.getStockCode(),
                stock.getCompanyName(),
                stock.getStockValue());
    }
}
