package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooQuoteItem {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("shortname")
    private String shortName;

    @JsonProperty("quoteType")
    private String quoteType;

    @JsonProperty("sector")
    private String sector;

    @JsonProperty("industry")
    private String industry;

    public YahooQuoteItem() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }

    public String getQuoteType() { return quoteType; }
    public void setQuoteType(String quoteType) { this.quoteType = quoteType; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
}
