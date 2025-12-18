package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooQuote {

    @JsonProperty("open")
    private List<BigDecimal> open;

    @JsonProperty("high")
    private List<BigDecimal> high;

    @JsonProperty("low")
    private List<BigDecimal> low;

    @JsonProperty("close")
    private List<BigDecimal> close;

    @JsonProperty("volume")
    private List<Long> volume;

    public YahooQuote() {}

    public YahooQuote(List<BigDecimal> open, List<BigDecimal> high, List<BigDecimal> low,
                      List<BigDecimal> close, List<Long> volume) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
    }

    // Getters and Setters
    public List<BigDecimal> getOpen() { return open; }
    public void setOpen(List<BigDecimal> open) { this.open = open; }

    public List<BigDecimal> getHigh() { return high; }
    public void setHigh(List<BigDecimal> high) { this.high = high; }

    public List<BigDecimal> getLow() { return low; }
    public void setLow(List<BigDecimal> low) { this.low = low; }

    public List<BigDecimal> getClose() { return close; }
    public void setClose(List<BigDecimal> close) { this.close = close; }

    public List<Long> getVolume() { return volume; }
    public void setVolume(List<Long> volume) { this.volume = volume; }
}
