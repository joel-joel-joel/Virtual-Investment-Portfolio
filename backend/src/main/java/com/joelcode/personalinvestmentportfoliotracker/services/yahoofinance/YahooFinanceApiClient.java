package com.joelcode.personalinvestmentportfoliotracker.services.yahoofinance;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;

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
}
