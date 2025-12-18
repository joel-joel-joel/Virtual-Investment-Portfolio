package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooChartResponse {

    @JsonProperty("chart")
    private YahooChart chart;

    public YahooChartResponse() {}

    public YahooChartResponse(YahooChart chart) {
        this.chart = chart;
    }

    // Getters and Setters
    public YahooChart getChart() { return chart; }
    public void setChart(YahooChart chart) { this.chart = chart; }
}
