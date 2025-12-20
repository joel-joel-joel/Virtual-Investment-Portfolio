package com.joelcode.personalinvestmentportfoliotracker.dto.marketaux;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketAuxArticleDTO {

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("url")
    private String url;

    @JsonProperty("published_on")
    private String publishedOn;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("industries")
    private String[] industries;

    public MarketAuxArticleDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPublishedOn() { return publishedOn; }
    public void setPublishedOn(String publishedOn) { this.publishedOn = publishedOn; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String[] getIndustries() { return industries; }
    public void setIndustries(String[] industries) { this.industries = industries; }
}
