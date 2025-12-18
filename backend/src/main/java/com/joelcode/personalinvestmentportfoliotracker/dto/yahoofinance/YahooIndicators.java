package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooIndicators {

    @JsonProperty("quote")
    private List<YahooQuote> quote;

    @JsonProperty("adjclose")
    private List<YahooAdjClose> adjclose;

    public YahooIndicators() {}

    public YahooIndicators(List<YahooQuote> quote, List<YahooAdjClose> adjclose) {
        this.quote = quote;
        this.adjclose = adjclose;
    }

    // Getters and Setters
    public List<YahooQuote> getQuote() { return quote; }
    public void setQuote(List<YahooQuote> quote) { this.quote = quote; }

    public List<YahooAdjClose> getAdjclose() { return adjclose; }
    public void setAdjclose(List<YahooAdjClose> adjclose) { this.adjclose = adjclose; }
}
