package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooAdjClose {

    @JsonProperty("adjclose")
    private List<BigDecimal> adjclose;

    public YahooAdjClose() {}

    public YahooAdjClose(List<BigDecimal> adjclose) {
        this.adjclose = adjclose;
    }

    // Getters and Setters
    public List<BigDecimal> getAdjclose() { return adjclose; }
    public void setAdjclose(List<BigDecimal> adjclose) { this.adjclose = adjclose; }
}
