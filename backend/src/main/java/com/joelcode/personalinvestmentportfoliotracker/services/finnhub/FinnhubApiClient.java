package com.joelcode.personalinvestmentportfoliotracker.services.finnhub;

import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCandleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubCompanyProfileDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubMetricsDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubQuoteDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.finnhub.FinnhubSearchResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface FinnhubApiClient {

    FinnhubQuoteDTO getQuote(String symbol);

    FinnhubCompanyProfileDTO getCompanyProfile(String symbol);

    FinnhubMetricsDTO getMetrics(String symbol);

    FinnhubCandleDTO getCandles(String symbol, String resolution, long from, long to);

    BigDecimal getCurrentPrice(String symbol);

    FinnhubSearchResponseDTO searchCompanies(String query);
}
