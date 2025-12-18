package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooChartError {

    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

    public YahooChartError() {}

    public YahooChartError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "YahooChartError{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
