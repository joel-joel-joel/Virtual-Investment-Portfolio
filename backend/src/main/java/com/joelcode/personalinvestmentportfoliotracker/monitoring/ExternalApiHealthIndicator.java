package com.joelcode.personalinvestmentportfoliotracker.monitoring;

import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import com.joelcode.personalinvestmentportfoliotracker.services.news.MarketAuxApiClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for External APIs (FinnHub and MarketAux)
 *
 * This component checks the health of external API dependencies and reports
 * their status through Spring Boot Actuator's health endpoint.
 *
 * Usage: Access via /actuator/health endpoint
 */
@Component("externalApis")
public class ExternalApiHealthIndicator implements HealthIndicator {

    private final FinnhubApiClient finnhubApiClient;
    private final MarketAuxApiClient marketAuxApiClient;

    public ExternalApiHealthIndicator(FinnhubApiClient finnhubApiClient,
                                     MarketAuxApiClient marketAuxApiClient) {
        this.finnhubApiClient = finnhubApiClient;
        this.marketAuxApiClient = marketAuxApiClient;
    }

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        boolean finnhubHealthy = checkFinnhubHealth();
        boolean marketAuxHealthy = checkMarketAuxHealth();

        if (finnhubHealthy && marketAuxHealthy) {
            healthBuilder.up();
        } else if (!finnhubHealthy && !marketAuxHealthy) {
            healthBuilder.down();
        } else {
            healthBuilder.status("DEGRADED");
        }

        healthBuilder
            .withDetail("finnhub", finnhubHealthy ? "UP" : "DOWN")
            .withDetail("marketaux", marketAuxHealthy ? "UP" : "DOWN")
            .withDetail("message", getStatusMessage(finnhubHealthy, marketAuxHealthy));

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

    private boolean checkMarketAuxHealth() {
        try {
            // Try to fetch news with minimal limit
            marketAuxApiClient.getAllNews(1);
            return true;
        } catch (Exception e) {
            // Log the error for debugging but don't fail the health check completely
            System.err.println("MarketAux API health check failed: " + e.getMessage());
            return false;
        }
    }

    private String getStatusMessage(boolean finnhubHealthy, boolean marketAuxHealthy) {
        if (finnhubHealthy && marketAuxHealthy) {
            return "All external APIs are operational";
        } else if (!finnhubHealthy && !marketAuxHealthy) {
            return "All external APIs are down";
        } else if (!finnhubHealthy) {
            return "FinnHub API is down";
        } else {
            return "MarketAux API is down";
        }
    }
}
