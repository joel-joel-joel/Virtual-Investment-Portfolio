package com.joelcode.personalinvestmentportfoliotracker.dto.stock;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;

import java.math.BigDecimal;
import java.util.UUID;

public class StockDTO {

    // Stock response DTO (output)
    private UUID stockId;
    private String stockCode;
    private String companyName;
    private BigDecimal stockValue;

    // Constructor
    public StockDTO(UUID stockId, String stockCode, String companyName, BigDecimal stockValue) {
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

    public StockDTO() {}

    // Getters
    public UUID getStockId() {return stockId;}

    public String getStockCode() {return stockCode;}

    public String getCompanyName() {return companyName;}

    public BigDecimal getStockValue() {return stockValue;}
}
