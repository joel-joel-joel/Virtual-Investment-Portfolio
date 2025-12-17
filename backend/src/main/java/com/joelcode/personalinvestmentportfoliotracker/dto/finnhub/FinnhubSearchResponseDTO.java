package com.joelcode.personalinvestmentportfoliotracker.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class FinnhubSearchResponseDTO {

    @JsonProperty("result")
    private List<FinnhubSearchResultDTO> result;

    @JsonProperty("count")
    private Integer count;

    public FinnhubSearchResponseDTO() {}

    public FinnhubSearchResponseDTO(List<FinnhubSearchResultDTO> result, Integer count) {
        this.result = result;
        this.count = count;
    }

    public List<FinnhubSearchResultDTO> getResult() { return result; }
    public void setResult(List<FinnhubSearchResultDTO> result) { this.result = result; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
