package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;

import java.util.List;

public interface PriceHistoryCalculationService {

    double calculateAveragePrice(List<PriceHistory> priceHistories);

    double calculatePriceChange(PriceHistory previous, PriceHistory latest);

    double calculatePercentageChange(PriceHistory previous, PriceHistory latest);

    double findHighestPrice(List<PriceHistory> priceHistories);

    double findLowestPrice(List<PriceHistory> priceHistories);
}
