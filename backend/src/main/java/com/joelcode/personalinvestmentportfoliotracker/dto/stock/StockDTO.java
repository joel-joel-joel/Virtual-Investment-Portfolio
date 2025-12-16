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
    private String industry;

    // Constructor
    public StockDTO(UUID stockId, String stockCode, String companyName, BigDecimal stockValue) {
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
        this.industry = null;
    }

    public StockDTO(UUID stockId, String stockCode, String companyName, BigDecimal stockValue, String industry) {
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
        this.industry = industry;
    }

    public StockDTO(Stock stock) {
        this.stockId = stock.getStockId();
        this.stockCode = stock.getStockCode();
        this.companyName = stock.getCompanyName();
        this.stockValue = stock.getStockValue();
        this.industry = stock.getIndustry();
    }

    public StockDTO() {}

    // Getters
    public UUID getStockId() {return stockId;}

    public String getStockCode() {return stockCode;}

    public String getCompanyName() {return companyName;}

    public BigDecimal getStockValue() {return stockValue;}

    public String getIndustry() {return industry;}

    public void setIndustry(String industry) {this.industry = industry;}
}
