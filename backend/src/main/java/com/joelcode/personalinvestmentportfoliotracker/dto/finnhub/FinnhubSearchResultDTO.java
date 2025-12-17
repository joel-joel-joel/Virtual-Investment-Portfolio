package com.joelcode.personalinvestmentportfoliotracker.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FinnhubSearchResultDTO {

    @JsonProperty("description")
    private String description;

    @JsonProperty("displaySymbol")
    private String displaySymbol;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("type")
    private String type;

    public FinnhubSearchResultDTO() {}

    public FinnhubSearchResultDTO(String description, String displaySymbol, String symbol, String type) {
        this.description = description;
        this.displaySymbol = displaySymbol;
        this.symbol = symbol;
        this.type = type;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDisplaySymbol() { return displaySymbol; }
    public void setDisplaySymbol(String displaySymbol) { this.displaySymbol = displaySymbol; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
