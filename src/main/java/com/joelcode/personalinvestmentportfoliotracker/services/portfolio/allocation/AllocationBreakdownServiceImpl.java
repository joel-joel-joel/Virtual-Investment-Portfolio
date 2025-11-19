package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class AllocationBreakdownServiceImpl implements AllocationBreakdownService {

    // Define key fields
    private final HoldingService holdingService;
    private final HoldingCalculationService holdingCalcService;
    private final HoldingRepository holdingRepository;

    // Constructor
    public AllocationBreakdownServiceImpl(HoldingService holdingService,
                                          HoldingCalculationService holdingCalcService,
                                          HoldingRepository holdingRepository) {
        this.holdingService = holdingService;
        this.holdingCalcService = holdingCalcService;
        this.holdingRepository = holdingRepository;
    }

    // Interface methods
    @Override
    public List<AllocationBreakdownDTO> getAllocationForAccount(UUID accountId) {

        List<Holding> holdings = holdingRepository.getHoldingsEntitiesByAccount(accountId);

        BigDecimal totalValue = holdings.stream()
                .map(holdingCalcService::calculateCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return holdings.stream()
                .map(h -> {
                    BigDecimal value = holdingCalcService.calculateCurrentValue(h);
                    BigDecimal percentage =
                            totalValue.compareTo(BigDecimal.ZERO) == 0
                                    ? BigDecimal.ZERO
                                    : value.multiply(BigDecimal.valueOf(100))
                                    .divide(totalValue, 4, RoundingMode.HALF_UP);

                    AllocationBreakdownDTO dto = new AllocationBreakdownDTO();
                    dto.setStockCode(h.getStock().getStockCode());
                    dto.setPercentage(percentage);
                    dto.setCurrentValue(value);
                    return dto;
                })
                .toList();
    }
}

