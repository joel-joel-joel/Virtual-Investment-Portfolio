package com.joelcode.personalinvestmentportfoliotracker.dto.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class StockCreateRequest {

    // Stock creation request DTO (input)
    @NotBlank (message = "Stock ID is required")
    @Size(max = 20, message = "Stock ID must be at most 20 characters")
    private UUID stockId;

    @NotBlank (message = "Stock code is required")
    @Size(max = 20, message = "Stock code must be at most 20 characters")
    private String stockCode;

    @NotBlank (message = "Company name is required")
    @Size(max = 100, message = "Company name must be at most 100 characters")
    private String companyName;

    @NotNull(message = "Stock value is required")
    @Positive(message = "Stock value must be positive")
    private BigDecimal stockValue;

    @Size(max = 100, message = "Industry must be at most 100 characters")
    private String industry;

    // Jackson-compatible constructor
    @JsonCreator
    public StockCreateRequest(
            @JsonProperty ("stockiD") UUID stockId,
            @JsonProperty ("companyName") String companyName,
            @JsonProperty ("stockCode") String stockCode,
            @JsonProperty ("stockValue") BigDecimal stockValue,
            @JsonProperty ("industry") String industry){
        this.stockId = stockId;
        this.companyName = companyName;
        this.stockCode = stockCode;
        this.stockValue = stockValue;
        this.industry = industry;
    }

    public StockCreateRequest() {}

    // Getters and setters
    public String getStockCode() {return stockCode;}

    public String getCompanyName() {return companyName;}

    public BigDecimal getStockValue() {return stockValue;}

    public UUID getStockId() {return stockId;}

    public void setStockId(String stockId) {this.stockId = UUID.fromString(stockId);}

    public void setCompanyName(String companyName) {this.companyName = companyName;}

    public void setStockCode(String stockCode) {this.stockCode = stockCode;}

    public void setStockValue(BigDecimal stockValue) {this.stockValue = stockValue;}

    public String getIndustry() {return industry;}

    public void setIndustry(String industry) {this.industry = industry;}
}
