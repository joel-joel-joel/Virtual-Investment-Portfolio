package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooNewsItem {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("publisher")
    private String publisher;

    @JsonProperty("link")
    private String link;

    @JsonProperty("providerPublishTime")
    private Long providerPublishTime;

    @JsonProperty("type")
    private String type;

    @JsonProperty("thumbnail")
    private YahooNewsThumbnail thumbnail;

    public YahooNewsItem() {}

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public Long getProviderPublishTime() { return providerPublishTime; }
    public void setProviderPublishTime(Long providerPublishTime) { this.providerPublishTime = providerPublishTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public YahooNewsThumbnail getThumbnail() { return thumbnail; }
    public void setThumbnail(YahooNewsThumbnail thumbnail) { this.thumbnail = thumbnail; }
}
