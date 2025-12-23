package com.joelcode.personalinvestmentportfoliotracker.dto.user;

public class UserPreferencesDTO {
    // Notification Settings
    private Boolean priceAlerts;
    private Boolean portfolioUpdates;
    private Boolean marketNews;
    private Boolean dividendNotifications;
    private Boolean earningSeason;

    // Constructor
    public UserPreferencesDTO(
        Boolean priceAlerts,
        Boolean portfolioUpdates,
        Boolean marketNews,
        Boolean dividendNotifications,
        Boolean earningSeason
    ) {
        this.priceAlerts = priceAlerts;
        this.portfolioUpdates = portfolioUpdates;
        this.marketNews = marketNews;
        this.dividendNotifications = dividendNotifications;
        this.earningSeason = earningSeason;
    }

    // Default constructor
    public UserPreferencesDTO() {}

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
