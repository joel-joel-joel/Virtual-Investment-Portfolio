package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation;

import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.model.PortfolioOverviewDTO;

import java.util.UUID;

public interface PortfolioAggregationService {

    // Aggregate the full portfolio overview for an account. Inclusive of holdings, cash, dividends, un/realized gains
    PortfolioOverviewDTO getPortfolioOverview(UUID accountId);

}
