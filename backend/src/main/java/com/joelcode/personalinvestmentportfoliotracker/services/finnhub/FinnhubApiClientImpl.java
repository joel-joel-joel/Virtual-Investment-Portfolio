package com.joelcode.personalinvestmentportfoliotracker.services.finnhub;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCompanyProfileDTO;
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
            return restTemplate.getForObject(url, FinnhubQuoteDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch quote for symbol: " + symbol, e);
        }
    }

    @Override
    public FinnhubCompanyProfileDTO getCompanyProfile(String symbol) {
        try {
            String url = String.format("%s/stock/profile2?symbol=%s&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
            return restTemplate.getForObject(url, FinnhubCompanyProfileDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch company profile for symbol: " + symbol, e);
        }
    }

    @Override
    public FinnhubMetricsDTO getMetrics(String symbol) {
        try {
            String url = String.format("%s/stock/metric?symbol=%s&metric=all&token=%s", baseUrl, symbol.toUpperCase(), apiKey);
            return restTemplate.getForObject(url, FinnhubMetricsDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch metrics for symbol: " + symbol, e);
        }
    }

    @Override
    public FinnhubCandleDTO getCandles(String symbol, String resolution, long from, long to) {
        try {
            String url = String.format("%s/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    baseUrl, symbol.toUpperCase(), resolution, from, to, apiKey);
            return restTemplate.getForObject(url, FinnhubCandleDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch candles for symbol: " + symbol, e);
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
            // Use UriComponentsBuilder to properly encode the query parameter
            URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + "/search")
                    .queryParam("q", query)
                    .queryParam("token", apiKey)
                    .build()
                    .toUri();
            return restTemplate.getForObject(uri, FinnhubSearchResponseDTO.class);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to search companies for query: " + query, e);
        }
    }
}
