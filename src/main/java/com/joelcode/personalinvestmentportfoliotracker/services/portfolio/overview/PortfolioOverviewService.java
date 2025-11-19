package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.overview;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;

import java.util.UUID;

public interface PortfolioOverviewService {

    PortfolioOverviewDTO getPortfolioOverviewForUser(UUID userId);

    PortfolioOverviewDTO getPortfolioOverviewForAccount(UUID accountId);
}
