package com.joelcode.personalinvestmentportfoliotracker.dto.stock;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;

import java.util.UUID;

public class StockDTO {

    // Stock response DTO (output)
    private final UUID stockId;
    private final String stockCode;
    private final String companyName;
    private final Double stockValue;

    // Constructor
    public StockDTO(UUID stockId, String stockCode, String companyName, Double stockValue) {
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
    }

    public StockDTO(Stock stock) {
        this.stockId = stock.getStockId();
        this.stockCode = stock.getStockCode();
        this.companyName = stock.getCompanyName();
        this.stockValue = stock.getStockValue();
    }

    // Getters
    public UUID getStockId() {return stockId;}

    public String getStockCode() {return stockCode;}

    public String getCompanyName() {return companyName;}

    public Double getStockValue() {return stockValue;}
}
