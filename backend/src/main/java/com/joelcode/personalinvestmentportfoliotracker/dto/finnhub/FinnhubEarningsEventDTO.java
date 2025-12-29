package com.joelcode.personalinvestmentportfoliotracker.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * DTO for a single earnings event from Finnhub Earnings Calendar API
 * Represents one company's earnings release date and estimated/actual earnings
 */
public class FinnhubEarningsEventDTO {
    @JsonProperty("date")
    private String date;  // Format: YYYY-MM-DD

    @JsonProperty("epsActual")
    private BigDecimal epsActual;  // Actual earnings per share (optional)

    @JsonProperty("epsEstimate")
    private BigDecimal epsEstimate;  // Estimated earnings per share (optional)

    @JsonProperty("hour")
    private String hour;  // "bmo" (before market open), "amc" (after market close), "dmh" (during market hours)

    @JsonProperty("quarter")
    private Integer quarter;  // Quarter number (1-4)

    @JsonProperty("revenueActual")
    private BigDecimal revenueActual;  // Actual revenue (optional)

    @JsonProperty("revenueEstimate")
    private BigDecimal revenueEstimate;  // Estimated revenue (optional)

    @JsonProperty("symbol")
    private String symbol;  // Stock symbol (e.g., "AAPL")

    @JsonProperty("year")
    private Integer year;  // Year of earnings release

    public FinnhubEarningsEventDTO() {}

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getEpsActual() {
        return epsActual;
    }

    public void setEpsActual(BigDecimal epsActual) {
        this.epsActual = epsActual;
    }

    public BigDecimal getEpsEstimate() {
        return epsEstimate;
    }

    public void setEpsEstimate(BigDecimal epsEstimate) {
        this.epsEstimate = epsEstimate;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public BigDecimal getRevenueActual() {
        return revenueActual;
    }

    public void setRevenueActual(BigDecimal revenueActual) {
        this.revenueActual = revenueActual;
    }

    public BigDecimal getRevenueEstimate() {
        return revenueEstimate;
    }

    public void setRevenueEstimate(BigDecimal revenueEstimate) {
        this.revenueEstimate = revenueEstimate;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
