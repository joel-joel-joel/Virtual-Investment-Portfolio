package com.joelcode.personalinvestmentportfoliotracker.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class FinnhubQuoteDTO {

    @JsonProperty("c")
    private BigDecimal currentPrice;

    @JsonProperty("h")
    private BigDecimal highPrice;

    @JsonProperty("l")
    private BigDecimal lowPrice;

    @JsonProperty("o")
    private BigDecimal openPrice;

    @JsonProperty("pc")
    private BigDecimal previousClosePrice;

    @JsonProperty("t")
    private Long timestamp;

    @JsonProperty("d")
    private BigDecimal change;

    @JsonProperty("dp")
    private BigDecimal changePercent;

    public FinnhubQuoteDTO() {}

    public FinnhubQuoteDTO(BigDecimal currentPrice, BigDecimal highPrice, BigDecimal lowPrice,
                           BigDecimal openPrice, BigDecimal previousClosePrice, Long timestamp) {
        this.currentPrice = currentPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.openPrice = openPrice;
        this.previousClosePrice = previousClosePrice;
        this.timestamp = timestamp;
    }

    public FinnhubQuoteDTO(BigDecimal currentPrice, BigDecimal highPrice, BigDecimal lowPrice,
                           BigDecimal openPrice, BigDecimal previousClosePrice, Long timestamp,
                           BigDecimal change, BigDecimal changePercent) {
        this.currentPrice = currentPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.openPrice = openPrice;
        this.previousClosePrice = previousClosePrice;
        this.timestamp = timestamp;
        this.change = change;
        this.changePercent = changePercent;
    }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }

    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }

    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }

    public BigDecimal getPreviousClosePrice() { return previousClosePrice; }
    public void setPreviousClosePrice(BigDecimal previousClosePrice) { this.previousClosePrice = previousClosePrice; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    public BigDecimal getChange() { return change; }
    public void setChange(BigDecimal change) { this.change = change; }

    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
}
