package com.joelcode.personalinvestmentportfoliotracker.dto.news;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewsArticleDTO {

    @JsonProperty("sector")
    private String sector;

    @JsonProperty("title")
    private String title;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("url")
    private String url;

    @JsonProperty("publishedAt")
    private String publishedAt;

    @JsonProperty("imageUrl")
    private String imageUrl;

    public NewsArticleDTO() {}

    public NewsArticleDTO(String sector, String title, String summary, String url, String publishedAt, String imageUrl) {
        this.sector = sector;
        this.title = title;
        this.summary = summary;
        this.url = url;
        this.publishedAt = publishedAt;
        this.imageUrl = imageUrl;
    }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
