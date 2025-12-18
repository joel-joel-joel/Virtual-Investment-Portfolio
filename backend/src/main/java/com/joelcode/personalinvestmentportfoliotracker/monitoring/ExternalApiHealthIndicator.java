package com.joelcode.personalinvestmentportfoliotracker.monitoring;

import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import com.joelcode.personalinvestmentportfoliotracker.services.yahoofinance.YahooFinanceApiClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for External APIs (FinnHub and Yahoo Finance)
 *
 * This component checks the health of external API dependencies and reports
 * their status through Spring Boot Actuator's health endpoint.
 *
 * Usage: Access via /actuator/health endpoint
 */
@Component("externalApis")
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final FinnhubApiClient finnhubApiClient;
    private final YahooFinanceApiClient yahooFinanceApiClient;

    public ExternalApiHealthIndicator(FinnhubApiClient finnhubApiClient,
                                     YahooFinanceApiClient yahooFinanceApiClient) {
        this.finnhubApiClient = finnhubApiClient;
        this.yahooFinanceApiClient = yahooFinanceApiClient;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        boolean finnhubHealthy = checkFinnhubHealth();
        boolean yahooHealthy = checkYahooFinanceHealth();

        if (finnhubHealthy && yahooHealthy) {
            healthBuilder.up();
        } else if (!finnhubHealthy && !yahooHealthy) {
            healthBuilder.down();
        } else {
            healthBuilder.status("DEGRADED");
        }

        healthBuilder
            .withDetail("finnhub", finnhubHealthy ? "UP" : "DOWN")
            .withDetail("yahooFinance", yahooHealthy ? "UP" : "DOWN")
            .withDetail("message", getStatusMessage(finnhubHealthy, yahooHealthy));

        return healthBuilder.build();
    }

    private boolean checkFinnhubHealth() {
        try {
            // Try to fetch a quote for a known stock (AAPL) with a short timeout
            finnhubApiClient.getQuote("AAPL");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkYahooFinanceHealth() {
        try {
            // Test both candles and search/news endpoints
            yahooFinanceApiClient.getCandles("AAPL", "1d",
                    System.currentTimeMillis() / 1000 - 86400,
                    System.currentTimeMillis() / 1000);
            yahooFinanceApiClient.search("AAPL");
            return true;
        } catch (Exception e) {
            System.err.println("Yahoo Finance API health check failed: " + e.getMessage());
            return false;
        }
    }

    private String getStatusMessage(boolean finnhubHealthy, boolean yahooHealthy) {
        if (finnhubHealthy && yahooHealthy) {
            return "All external APIs are operational";
        } else if (!finnhubHealthy && !yahooHealthy) {
            return "All external APIs are down";
        } else if (!finnhubHealthy) {
            return "FinnHub API is down";
        } else {
            return "Yahoo Finance API is down";
        }
    }
}
