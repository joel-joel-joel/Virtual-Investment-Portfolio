package com.joelcode.personalinvestmentportfoliotracker.dto.finnhub;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DTO for Finnhub Earnings Calendar API response
 * Represents the entire earnings calendar response containing a list of earnings events
 */
public class FinnhubEarningsCalendarDTO {
    @JsonProperty("earningsCalendar")
    private List<FinnhubEarningsEventDTO> earningsCalendar;

    public FinnhubEarningsCalendarDTO() {}

    public FinnhubEarningsCalendarDTO(List<FinnhubEarningsEventDTO> earningsCalendar) {
        this.earningsCalendar = earningsCalendar;
    }

    public List<FinnhubEarningsEventDTO> getEarningsCalendar() {
        return earningsCalendar;
    }

    public void setEarningsCalendar(List<FinnhubEarningsEventDTO> earningsCalendar) {
        this.earningsCalendar = earningsCalendar;
    }
}
