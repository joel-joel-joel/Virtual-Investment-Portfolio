package com.joelcode.personalinvestmentportfoliotracker.services.yahoofinance;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooNewsItem;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooSearchResponse;

import java.util.List;

public interface YahooFinanceApiClient {

    /**
     * Fetch historical candle data from Yahoo Finance
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param interval Interval - "1m", "5m", "15m", "30m", "1h", "1d", "1wk", "1mo"
     * @param from Unix timestamp start
     * @param to Unix timestamp end
     * @return FinnhubCandleDTO with OHLCV data
     */
    FinnhubCandleDTO getCandles(String symbol, String interval, long from, long to);

    /**
     * Search Yahoo Finance for news and quotes
     * @param query Search query (symbol, keyword, or general term)
     * @return YahooSearchResponse with news and quote results
     */
    YahooSearchResponse search(String query);

    /**
     * Get news for a specific stock symbol
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param limit Maximum number of articles
     * @return List of news items for the symbol
     */
    List<YahooNewsItem> getNewsForSymbol(String symbol, int limit);

    /**
     * Get general market news
     * @param limit Maximum number of articles
     * @return List of general news items
     */
    List<YahooNewsItem> getGeneralNews(int limit);
}
