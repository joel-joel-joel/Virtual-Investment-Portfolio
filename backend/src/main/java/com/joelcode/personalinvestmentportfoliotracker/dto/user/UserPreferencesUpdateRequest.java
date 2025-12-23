package com.joelcode.personalinvestmentportfoliotracker.dto.user;

public class UserPreferencesUpdateRequest {
    // Notification Settings (all optional for partial updates)
    private Boolean priceAlerts;
    private Boolean portfolioUpdates;
    private Boolean marketNews;
    private Boolean dividendNotifications;
    private Boolean earningSeason;

    // Default constructor
    public UserPreferencesUpdateRequest() {}

    // Getters and Setters
    public Boolean getPriceAlerts() { return priceAlerts; }
    public void setPriceAlerts(Boolean priceAlerts) { this.priceAlerts = priceAlerts; }

    public Boolean getPortfolioUpdates() { return portfolioUpdates; }
    public void setPortfolioUpdates(Boolean portfolioUpdates) { this.portfolioUpdates = portfolioUpdates; }

    public Boolean getMarketNews() { return marketNews; }
    public void setMarketNews(Boolean marketNews) { this.marketNews = marketNews; }

    public Boolean getDividendNotifications() { return dividendNotifications; }
    public void setDividendNotifications(Boolean dividendNotifications) { this.dividendNotifications = dividendNotifications; }

    public Boolean getEarningSeason() { return earningSeason; }
    public void setEarningSeason(Boolean earningSeason) { this.earningSeason = earningSeason; }
}
