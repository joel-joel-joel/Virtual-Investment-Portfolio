package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooChartResult {

    @JsonProperty("timestamp")
    private List<Long> timestamp;

    @JsonProperty("indicators")
    private YahooIndicators indicators;

    @JsonProperty("meta")
    private Map<String, Object> meta;

    public YahooChartResult() {}

    public YahooChartResult(List<Long> timestamp, YahooIndicators indicators, Map<String, Object> meta) {
        this.timestamp = timestamp;
        this.indicators = indicators;
        this.meta = meta;
    }

    // Getters and Setters
    public List<Long> getTimestamp() { return timestamp; }
    public void setTimestamp(List<Long> timestamp) { this.timestamp = timestamp; }

    public YahooIndicators getIndicators() { return indicators; }
    public void setIndicators(YahooIndicators indicators) { this.indicators = indicators; }

    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}
