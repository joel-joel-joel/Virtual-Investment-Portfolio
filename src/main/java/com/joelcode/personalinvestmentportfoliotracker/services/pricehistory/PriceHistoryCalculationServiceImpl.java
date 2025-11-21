package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("!test")
public class PriceHistoryCalculationServiceImpl implements PriceHistoryCalculationService{

    // For common calculation operations

    // Calculate average price
    @Override
    public double calculateAveragePrice(List<PriceHistory> priceHistories){
        if (priceHistories== null || priceHistories.isEmpty()) return 0.0;
        double sum = priceHistories.stream()
                .mapToDouble(PriceHistory -> PriceHistory.getClosePrice().doubleValue())
                .sum();
        return sum / priceHistories.size();
    }

    // Calculate price change
    @Override
    public double calculatePriceChange(PriceHistory previous, PriceHistory latest){
        return latest.getClosePrice().doubleValue() - previous.getClosePrice().doubleValue();
    }

    // Calculate percentage change
    @Override
    public double calculatePercentageChange(PriceHistory previous, PriceHistory latest){
        if (previous.getClosePrice().doubleValue() == 0) return 0;
        return (latest.getClosePrice().doubleValue() - previous.getClosePrice().doubleValue()) / previous.getClosePrice().doubleValue() * 100;
    }

    // Find highest price
    @Override
    public double findHighestPrice(List<PriceHistory> priceHistories){
        if (priceHistories == null || priceHistories.isEmpty()) return 0.0;
        return priceHistories.stream()
                .mapToDouble(PriceHistory -> PriceHistory.getClosePrice().doubleValue())
                .max()
                .orElse(0.0);
    }

    // Find lowest price
    @Override
    public double findLowestPrice(List<PriceHistory> priceHistories) {
        if (priceHistories == null || priceHistories.isEmpty()) return 0.0;
        return priceHistories.stream()
                .mapToDouble(PriceHistory -> PriceHistory.getClosePrice().doubleValue())
                .min()
                .orElse(0.0);
    }


}