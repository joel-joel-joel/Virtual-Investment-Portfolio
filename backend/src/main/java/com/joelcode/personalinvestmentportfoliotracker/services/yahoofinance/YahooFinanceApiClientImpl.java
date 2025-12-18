package com.joelcode.personalinvestmentportfoliotracker.services.yahoofinance;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooChartResponse;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooChartResult;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooNewsItem;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooQuote;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooSearchResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YahooFinanceApiClientImpl implements YahooFinanceApiClient {

    private final RestTemplate restTemplate;
    private static final String YAHOO_FINANCE_API = "https://query2.finance.yahoo.com/v8/finance/chart";
    private static final String YAHOO_SEARCH_API = "https://query2.finance.yahoo.com/v1/finance/search";

    public YahooFinanceApiClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public FinnhubCandleDTO getCandles(String symbol, String interval, long from, long to) {
        try {
            // Map resolution to Yahoo Finance interval
            String yahooInterval = mapInterval(interval);

            String url = String.format(
                    "%s/%s?period1=%d&period2=%d&interval=%s&events=div,split&includeAdjustedClose=true",
                    YAHOO_FINANCE_API,
                    symbol.toUpperCase(),
                    from,
                    to,
                    yahooInterval
            );

            // Create headers with User-Agent (Yahoo Finance requires this)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Fetch JSON data from Yahoo Finance Chart API
            ResponseEntity<YahooChartResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    YahooChartResponse.class
            );

            YahooChartResponse chartResponse = response.getBody();

            if (chartResponse == null || chartResponse.getChart() == null) {
                return createEmptyResponse();
            }

            // Check for API errors
            if (chartResponse.getChart().getError() != null) {
                throw new RuntimeException("Yahoo Finance error: " + chartResponse.getChart().getError());
            }

            // Check if result exists and has data
            if (chartResponse.getChart().getResult() == null || chartResponse.getChart().getResult().isEmpty()) {
                return createEmptyResponse();
            }

            // Convert Yahoo Finance Chart data to FinnhubCandleDTO format
            return convertYahooChartToCandles(chartResponse.getChart().getResult().get(0));

        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch candles for symbol: " + symbol, e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing candle data for symbol: " + symbol, e);
        }
    }

    /**
     * Convert Yahoo Finance Chart API response to FinnhubCandleDTO format
     */
    private FinnhubCandleDTO convertYahooChartToCandles(YahooChartResult chartResult) {
        FinnhubCandleDTO candles = new FinnhubCandleDTO();

        if (chartResult == null || chartResult.getIndicators() == null || chartResult.getIndicators().getQuote() == null
                || chartResult.getIndicators().getQuote().isEmpty()) {
            return createEmptyResponse();
        }

        List<Long> timestamps = chartResult.getTimestamp();
        YahooQuote quote = chartResult.getIndicators().getQuote().get(0);

        // Extract OHLCV data from Yahoo response
        List<BigDecimal> closePrices = quote.getClose();
        List<BigDecimal> highPrices = quote.getHigh();
        List<BigDecimal> lowPrices = quote.getLow();
        List<BigDecimal> openPrices = quote.getOpen();
        List<Long> volumes = quote.getVolume();

        // Validate data exists
        if (closePrices == null || closePrices.isEmpty()) {
            return createEmptyResponse();
        }

        // Set data in FinnhubCandleDTO format
        candles.setClosePrices(closePrices);
        candles.setHighPrices(highPrices != null ? highPrices : new ArrayList<>());
        candles.setLowPrices(lowPrices != null ? lowPrices : new ArrayList<>());
        candles.setOpenPrices(openPrices != null ? openPrices : new ArrayList<>());
        candles.setTimestamps(timestamps != null ? timestamps : new ArrayList<>());
        candles.setVolumes(volumes != null ? volumes : new ArrayList<>());
        candles.setStatus("ok");

        return candles;
    }

    /**
     * Map resolution format to Yahoo Finance interval
     * Resolution: "1", "5", "15", "30", "60" (minutes), "D" (daily), "W" (weekly), "M" (monthly)
     * Yahoo interval: "1m", "5m", "15m", "30m", "1h", "1d", "1wk", "1mo"
     */
    private String mapInterval(String resolution) {
        return switch (resolution) {
            case "1" -> "1m";
            case "5" -> "5m";
            case "15" -> "15m";
            case "30" -> "30m";
            case "60" -> "1h";
            case "D" -> "1d";
            case "W" -> "1wk";
            case "M" -> "1mo";
            default -> "1d"; // Default to daily
        };
    }

    private FinnhubCandleDTO createEmptyResponse() {
        FinnhubCandleDTO candles = new FinnhubCandleDTO();
        candles.setClosePrices(new ArrayList<>());
        candles.setHighPrices(new ArrayList<>());
        candles.setLowPrices(new ArrayList<>());
        candles.setOpenPrices(new ArrayList<>());
        candles.setTimestamps(new ArrayList<>());
        candles.setVolumes(new ArrayList<>());
        candles.setStatus("no_data");
        return candles;
    }

    @Override
    public YahooSearchResponse search(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(YAHOO_SEARCH_API)
                    .queryParam("q", query)
                    .queryParam("quotesCount", 10)
                    .queryParam("newsCount", 25)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<YahooSearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    YahooSearchResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to search Yahoo Finance", e);
        }
    }

    @Override
    public List<YahooNewsItem> getNewsForSymbol(String symbol, int limit) {
        try {
            YahooSearchResponse searchResponse = search(symbol);
            if (searchResponse == null || searchResponse.getNews() == null) {
                return new ArrayList<>();
            }
            return searchResponse.getNews().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news for symbol: " + symbol, e);
        }
    }

    @Override
    public List<YahooNewsItem> getGeneralNews(int limit) {
        try {
            YahooSearchResponse searchResponse = search("stock market");
            if (searchResponse == null || searchResponse.getNews() == null) {
                return new ArrayList<>();
            }
            return searchResponse.getNews().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch general market news", e);
        }
    }
}
