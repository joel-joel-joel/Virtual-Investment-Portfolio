package com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class YahooNewsThumbnail {

    @JsonProperty("resolutions")
    private List<YahooThumbnailResolution> resolutions;

    public YahooNewsThumbnail() {}

    public List<YahooThumbnailResolution> getResolutions() { return resolutions; }
    public void setResolutions(List<YahooThumbnailResolution> resolutions) { this.resolutions = resolutions; }
}
