package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooSearchResponse {

    @JsonProperty("news")
    private List<YahooNewsItem> news;

    @JsonProperty("quotes")
    private List<YahooQuoteItem> quotes;

    public YahooSearchResponse() {}

    public List<YahooNewsItem> getNews() { return news; }
    public void setNews(List<YahooNewsItem> news) { this.news = news; }

    public List<YahooQuoteItem> getQuotes() { return quotes; }
    public void setQuotes(List<YahooQuoteItem> quotes) { this.quotes = quotes; }
}
