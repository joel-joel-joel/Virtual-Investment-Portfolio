package com.joelcode.personalinvestmentportfoliotracker.dto.marketaux;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MarketAuxNewsResponseDTO {

    @JsonProperty("data")
    private MarketAuxArticleDTO[] data;

    @JsonProperty("status")
    private String status;

    public MarketAuxNewsResponseDTO() {}

    public MarketAuxArticleDTO[] getData() { return data; }
    public void setData(MarketAuxArticleDTO[] data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
