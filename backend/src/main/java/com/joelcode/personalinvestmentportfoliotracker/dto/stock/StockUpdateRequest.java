package com.joelcode.personalinvestmentportfoliotracker.dto.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class StockUpdateRequest {

    // Stock creation request DTO (input)
    // Non-mandatory fields for single variable updates
    @Size(max = 20, message = "Stock code must be at most 20 characters")
    private String stockCode;

    @Size(max = 20, message = "Stock ID must be at most 20 characters")
    private UUID stockId;

    @Size(max = 100, message = "Company name must be at most 100 characters")
    private String companyName;

    @Size(max = 100, message = "Industry must be at most 100 characters")
    private String industry;

    // Jackson-compatible constructor
    @JsonCreator
    public StockUpdateRequest(
            @JsonProperty("stockCode") String stockCode,
            @JsonProperty ("companyName") String companyName,
            @JsonProperty ("stockId") UUID stockId,
            @JsonProperty ("industry") String industry){
        this.stockCode = stockCode;
        this.stockId = stockId;
        this.companyName = companyName;
        this.industry = industry;
    }

    // Getters and setters
    public UUID getStockId() {return stockId;}

    public void setStockId(UUID stockId) {this.stockId = stockId;}

    public String getStockCode() {return stockCode;}

    public void setStockCode(String stockCode) {this.stockCode = stockCode;}

    public String getCompanyName() {return companyName;}

    public void setCompanyName(String companyName) {this.companyName = companyName;}

    public String getIndustry() {return industry;}

    public void setIndustry(String industry) {this.industry = industry;}

}
