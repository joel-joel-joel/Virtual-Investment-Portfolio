package com.joelcode.personalinvestmentportfoliotracker.dto.stock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class StockCreateRequest {

    // Stock creation request DTO (input)
    @NotBlank (message = "Stock code is required")
    @Size(max = 20, message = "Stock code must be at most 20 characters")
    private final String stockCode;

    @NotBlank (message = "Company name is required")
    @Size(max = 100, message = "Company name must be at most 100 characters")
    private final String companyName;

    @NotNull(message = "Stock value is required")
    @Positive(message = "Stock value must be positive")
    private final Double stockValue;

    // Jackson-compatible constructor
    @JsonCreator
    public StockCreateRequest(
            @JsonProperty ("stockCode") String stockCode,
            @JsonProperty ("companyName") String companyName,
            @JsonProperty ("stockValue") Double stockValue) {
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.stockValue = stockValue;
    }

    // Getters
    public String getStockCode() {return stockCode;}

    public String getCompanyName() {return companyName;}

    public Double getStockValue() {return stockValue;}
}
