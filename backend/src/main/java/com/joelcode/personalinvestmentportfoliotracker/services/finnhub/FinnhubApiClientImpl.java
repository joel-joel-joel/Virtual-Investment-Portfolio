package com.joelcode.personalinvestmentportfoliotracker.services.finnhub;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCompanyProfileDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubEarningsCalendarDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubMetricsDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubQuoteDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubSearchResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@Service
public class FinnhubApiClientImpl implements FinnhubApiClient {

    @Value("${finnhub.api.key}")
    private String apiKey;

    @Value("${finnhub.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public FinnhubApiClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public FinnhubQuoteDTO getQuote(String symbol) {
        try {
            String url = String.format("%s/quote?symbol=%s&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
            System.err.println("📡 [FinnhubApiClient] Fetching quote for: " + symbol);
            System.err.println("   URL: " + url.replace(apiKey, "***"));

            FinnhubQuoteDTO result = restTemplate.getForObject(url, FinnhubQuoteDTO.class);

            System.err.println("✅ [FinnhubApiClient] Quote fetched successfully for: " + symbol);
            return result;
        } catch (RestClientException e) {
            // Log the actual exception with full details
            System.err.println("❌ [FinnhubApiClient] RestClientException for " + symbol + ":");
            System.err.println("   Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            e.printStackTrace();

            // Re-throw as-is to preserve exception type
            throw e;
        }
    }

    @Override
    public FinnhubCompanyProfileDTO getCompanyProfile(String symbol) {
        try {
            String url = String.format("%s/stock/profile2?symbol=%s&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
            System.err.println("📡 [FinnhubApiClient] Fetching company profile for: " + symbol);
            System.err.println("   URL: " + url.replace(apiKey, "***"));

            FinnhubCompanyProfileDTO result = restTemplate.getForObject(url, FinnhubCompanyProfileDTO.class);

            System.err.println("✅ [FinnhubApiClient] Company profile fetched successfully for: " + symbol);
            return result;
        } catch (RestClientException e) {
            // Log the actual exception with full details
            System.err.println("❌ [FinnhubApiClient] RestClientException for company profile " + symbol + ":");
            System.err.println("   Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            e.printStackTrace();

            // Re-throw as-is to preserve exception type
            throw e;
        }
    }

    @Override
    public FinnhubMetricsDTO getMetrics(String symbol) {
        try {
            String url = String.format("%s/stock/metric?symbol=%s&metric=all&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
            System.err.println("📡 [FinnhubApiClient] Fetching metrics for: " + symbol);
            System.err.println("   URL: " + url.replace(apiKey, "***"));

            FinnhubMetricsDTO result = restTemplate.getForObject(url, FinnhubMetricsDTO.class);

            System.err.println("✅ [FinnhubApiClient] Metrics fetched successfully for: " + symbol);
            return result;
        } catch (RestClientException e) {
            // Log the actual exception with full details
            System.err.println("❌ [FinnhubApiClient] RestClientException for metrics " + symbol + ":");
            System.err.println("   Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            e.printStackTrace();

            // Re-throw as-is to preserve exception type
            throw e;
        }
    }

    @Override
    public FinnhubCandleDTO getCandles(String symbol, String resolution, long from, long to) {
        try {
            String url = String.format("%s/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    baseUrl, symbol.toUpperCase(), resolution, from, to, apiKey);
            System.err.println("📡 [FinnhubApiClient] Fetching candles for: " + symbol);
            System.err.println("   URL: " + url.replace(apiKey, "***"));

            FinnhubCandleDTO result = restTemplate.getForObject(url, FinnhubCandleDTO.class);

            System.err.println("✅ [FinnhubApiClient] Candles fetched successfully for: " + symbol);
            return result;
        } catch (RestClientException e) {
            // Log the actual exception with full details
            System.err.println("❌ [FinnhubApiClient] RestClientException for candles " + symbol + ":");
            System.err.println("   Type: " + e.getClass().getSimpleName());
            System.err.println("   Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Cause: " + e.getCause().getClass().getSimpleName() + " - " + e.getCause().getMessage());
            }
            e.printStackTrace();

            // Re-throw as-is to preserve exception type
            throw e;
        }
    }

    @Override
    public BigDecimal getCurrentPrice(String symbol) {
        FinnhubQuoteDTO quote = getQuote(symbol);
        return quote != null ? quote.getCurrentPrice() : null;
    }

    @Override
    public FinnhubSearchResponseDTO searchCompanies(String query) {
        try {
            System.out.println("  📡 FinnhubApiClientImpl.searchCompanies()");
            System.out.println("    Query: " + query);
            System.out.println("    Base URL: " + baseUrl);

            // Use UriComponentsBuilder to properly encode the query parameter
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                    .queryParam("q", query)
                    .queryParam("token", apiKey)
                    .build()
                    .toUri();

            System.out.println("    🔗 Built URI: " + uri.toString());
            System.out.println("    🚀 Calling Finnhub API...");

            FinnhubSearchResponseDTO response = restTemplate.getForObject(uri, FinnhubSearchResponseDTO.class);

            System.out.println("    ✅ Response received from Finnhub");
            System.out.println("    📊 Response object: " + (response != null ? "Not null" : "NULL"));
            if (response != null) {
                System.out.println("    📊 Result count: " + (response.getResult() != null ? response.getResult().size() : "null"));
                System.out.println("    📊 Count field: " + response.getCount());
            }

            return response;
        } catch (Exception e) {
            System.out.println("    ❌ Finnhub API Error: " + e.getClass().getSimpleName());
            System.out.println("    📝 Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to search companies for query: " + query, e);
        }
    }

    @Override
    public FinnhubEarningsCalendarDTO getEarningsCalendar(String from, String to, String symbol) {
        try {
            System.out.println("  📡 FinnhubApiClientImpl.getEarningsCalendar()");
            System.out.println("    From: " + from + ", To: " + to + ", Symbol: " + symbol);

            // Use UriComponentsBuilder to properly encode parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/calendar/earnings")
                    .queryParam("token", apiKey);

            if (from != null && !from.isEmpty()) {
                builder.queryParam("from", from);
            }
            if (to != null && !to.isEmpty()) {
                builder.queryParam("to", to);
            }
            if (symbol != null && !symbol.isEmpty()) {
                builder.queryParam("symbol", symbol.toUpperCase());
            }

            URI uri = builder.build().toUri();

            System.out.println("    🔗 Built URI: " + uri.toString());
            System.out.println("    🚀 Calling Finnhub API...");

            FinnhubEarningsCalendarDTO response = restTemplate.getForObject(uri, FinnhubEarningsCalendarDTO.class);

            System.out.println("    ✅ Response received");
            return response;
        } catch (Exception e) {
            System.out.println("    ❌ Finnhub API Error: " + e.getClass().getSimpleName());
            System.out.println("    📝 Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch earnings calendar from Finnhub", e);
        }
    }
}
