package com.joelcode.personalinvestmentportfoliotracker.services.pricehistory;

import com.joelcode.personalinvestmentportfoliotracker.entities.PriceHistory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceHistoryCalculationServiceImpl implements PriceHistoryCalculationService{

    // For common calculation operations

    @Override
    public double calculateAveragePrice(List<PriceHistory> priceHistories){
        if (priceHistories== null || priceHistories.isEmpty()) return 0.0;
        double sum = priceHistories.stream()
                .mapToDouble(PriceHistory -> PriceHistory.getClosePrice().doubleValue())
                .sum();
        return sum / priceHistories.size();
    }

    @Override
    public double calculatePriceChange(PriceHistory previous, PriceHistory latest){
        return latest.getClosePrice().doubleValue() - previous.getClosePrice().doubleValue();
    }

    @Override
    public double calculatePercentageChange(PriceHistory previous, PriceHistory latest){
        if (previous.getClosePrice().doubleValue() == 0) return 0;
        return (latest.getClosePrice().doubleValue() - previous.getClosePrice().doubleValue()) / previous.getClosePrice().doubleValue() * 100;
    }

    @Override
    public double findHighestPrice(List<PriceHistory> priceHistories){
        if (priceHistories == null || priceHistories.isEmpty()) return 0.0;
        return priceHistories.stream()
                .mapToDouble(PriceHistory -> PriceHistory.getClosePrice().doubleValue())
                .max()
                .orElse(0.0);
    }

    @Override
    public double findLowestPrice(List<PriceHistory> priceHistories) {
        if (priceHistories == null || priceHistories.isEmpty()) return 0.0;
        return priceHistories.stream()
                .mapToDouble(PriceHistory -> PriceHistory.getClosePrice().doubleValue())
                .min()
                .orElse(0.0);
    }


}
