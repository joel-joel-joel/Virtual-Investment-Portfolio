package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooChart {

    @JsonProperty("result")
    private List<YahooChartResult> result;

    @JsonProperty("error")
    private YahooChartError error;

    public YahooChart() {}

    public YahooChart(List<YahooChartResult> result, YahooChartError error) {
        this.result = result;
        this.error = error;
    }

    // Getters and Setters
    public List<YahooChartResult> getResult() { return result; }
    public void setResult(List<YahooChartResult> result) { this.result = result; }

    public YahooChartError getError() { return error; }
    public void setError(YahooChartError error) { this.error = error; }
}
