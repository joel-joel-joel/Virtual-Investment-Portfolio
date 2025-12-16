package com.joelcode.personalinvestmentportfoliotracker.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class FinnhubCompanyProfileDTO {

    @JsonProperty("ticker")
    private String ticker;

    @JsonProperty("name")
    private String companyName;

    @JsonProperty("finnhubIndustry")
    private String industry;

    @JsonProperty("marketCapitalization")
    private BigDecimal marketCap;

    @JsonProperty("logo")
    private String logo;

    @JsonProperty("country")
    private String country;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("weburl")
    private String website;

    @JsonProperty("description")
    private String description;

    public FinnhubCompanyProfileDTO() {}

    public FinnhubCompanyProfileDTO(String ticker, String companyName, String industry, BigDecimal marketCap,
                                     String logo, String country, String currency, String phone,
                                     String website, String description) {
        this.ticker = ticker;
        this.companyName = companyName;
        this.industry = industry;
        this.marketCap = marketCap;
        this.logo = logo;
        this.country = country;
        this.currency = currency;
        this.phone = phone;
        this.website = website;
        this.description = description;
    }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public BigDecimal getMarketCap() { return marketCap; }
    public void setMarketCap(BigDecimal marketCap) { this.marketCap = marketCap; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


}
