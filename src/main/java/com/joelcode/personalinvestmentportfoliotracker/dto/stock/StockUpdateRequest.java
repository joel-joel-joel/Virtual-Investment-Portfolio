package com.joelcode.personalinvestmentportfoliotracker.dto.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class StockUpdateRequest {

    // Stock creation request DTO (input)
    // Non-mandatory fields for single variable updates
    @Size(max = 20, message = "Stock code must be at most 20 characters")
    private String stockCode;

    @Size(max = 100, message = "Company name must be at most 100 characters")
    private String companyName;

    @Positive(message = "Stock value must be positive")
    private Double stockValue;

    // Jackson-compatible constructor
    @JsonCreator
    public StockUpdateRequest(
            @JsonProperty("stockCode") String stockCode,
            @JsonProperty ("companyName") String companyName,
            @JsonProperty ("stockValue") Double stockValue) {
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
    }

    // Getters and setters
    public String getStockCode() {return stockCode;}

    public void setStockCode(String stockCode) {this.stockCode = stockCode;}

    public String getCompanyName() {return companyName;}

    public void setCompanyName(String companyName) {this.companyName = companyName;}

    public Double getStockValue() {return stockValue;}

    public void setStockValue(Double stockValue) {this.stockValue = stockValue;}
}
