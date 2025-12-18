package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooThumbnailResolution {

    @JsonProperty("url")
    private String url;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    public YahooThumbnailResolution() {}

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
}
