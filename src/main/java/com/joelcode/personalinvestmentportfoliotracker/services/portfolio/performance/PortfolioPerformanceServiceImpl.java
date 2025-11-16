package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance;

import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.model.PortfolioPerformanceDTO;

import java.util.UUID;

public class PortfolioPerformanceServiceImpl implements PortfolioPerformanceService{

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final DividendCalculationService dividendCalculationService;
    private final PortfolioSnapshotRepository snapshotRepository;

    // Constructor
    public PortfolioPerformanceServiceImpl (AccountService accountService, HoldingService holdingService,
                                            DividendCalculationService dividendCalculationService,
                                            PortfolioSnapshotRepository snapshotRepository) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.dividendCalculationService = dividendCalculationService;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public PortfolioPerformanceDTO getPortfolioPerformance(UUID accountId){

    }


}
