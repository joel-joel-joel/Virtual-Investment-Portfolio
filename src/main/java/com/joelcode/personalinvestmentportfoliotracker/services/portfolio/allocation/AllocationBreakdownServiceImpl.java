package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserValidationService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class AllocationBreakdownServiceImpl implements AllocationBreakdownService {

    // Define key fields
    private final HoldingCalculationService holdingCalcService;
    private final HoldingRepository holdingRepository;
    private final UserValidationService userValidationService;


    // Constructor
    public AllocationBreakdownServiceImpl(HoldingCalculationService holdingCalcService,
                                               HoldingRepository holdingRepository,
                                               UserValidationService userValidationService) {
        this.holdingCalcService = holdingCalcService;
        this.holdingRepository = holdingRepository;
        this.userValidationService = userValidationService;
    }


    // Interface functions

    // get allocation for an account
    @Override
    public List<AllocationBreakdownDTO> getAllocationForAccount(UUID accountId) {
        List<Holding> holdings = holdingRepository.findByAccount_AccountId(accountId);

        // EDGE CASE: Empty holdings
        if (holdings == null || holdings.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate total value
        BigDecimal totalValue = BigDecimal.ZERO;
        for (Holding h : holdings) {
            if (h != null && h.getStock() != null) {
                BigDecimal value = holdingCalcService.calculateCurrentValue(h);
                if (value != null) {
                    totalValue = totalValue.add(value);
                }
            }
        }

        // EDGE CASE: Zero total value
        final BigDecimal finalTotalValue = totalValue;
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            // Return allocations with 0% for each holding
            return holdings.stream()
                    .filter(h -> h != null && h.getStock() != null)
                    .map(h -> {
                        AllocationBreakdownDTO dto = new AllocationBreakdownDTO();
                        dto.setStockCode(h.getStock().getStockCode());
                        dto.setPercentage(BigDecimal.ZERO);
                        dto.setCurrentValue(BigDecimal.ZERO);
                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        // Calculate allocations
        return holdings.stream()
                .filter(h -> h != null && h.getStock() != null)
                .map(h -> {
                    BigDecimal value = holdingCalcService.calculateCurrentValue(h);

                    // EDGE CASE: Null value check
                    if (value == null) {
                        value = BigDecimal.ZERO;
                    }

                    BigDecimal percentage = value
                            .multiply(BigDecimal.valueOf(100))
                            .divide(finalTotalValue, 4, RoundingMode.HALF_UP)
                            .setScale(2, RoundingMode.HALF_UP);

                    AllocationBreakdownDTO dto = new AllocationBreakdownDTO();
                    dto.setStockCode(h.getStock().getStockCode());
                    dto.setPercentage(percentage);
                    dto.setCurrentValue(value);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Get allocation breakdown on user level
    @Override
    public List<AllocationBreakdownDTO> getAllocationForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        // EDGE CASE: No accounts
        if (user.getAccounts() == null || user.getAccounts().isEmpty()) {
            return new ArrayList<>();
        }

        // Aggregate allocations across all accounts
        return user.getAccounts().stream()
                .filter(account -> account != null && account.getAccountId() != null)
                .flatMap(account -> getAllocationForAccount(account.getAccountId()).stream())
                .collect(Collectors.toList());
    }
}