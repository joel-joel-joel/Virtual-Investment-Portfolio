package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.performance;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import com.joelcode.personalinvestmentportfoliotracker.services.portfolio.model.PortfolioPerformanceDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioPerformanceServiceImpl implements PortfolioPerformanceService{

    // Define key fields
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final DividendCalculationService dividendCalculationService;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    // Constructor
    public PortfolioPerformanceServiceImpl (AccountService accountService, HoldingService holdingService,
                                            DividendCalculationService dividendCalculationService,
                                            PortfolioSnapshotRepository snapshotRepository,
                                            AccountRepository accountRepository, HoldingRepository holdingRepository) {
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.dividendCalculationService = dividendCalculationService;
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    @Override
    @Transactional
    public PortfolioPerformanceDTO calculatePortfolioPerformance(UUID accountId){

        // Find account and retrieve holdings
        Account account = accountRepository.findByAccountId(accountId);
        List<Holding> holdings = holdingRepository.getHoldingsEntitiesByAccount(accountId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnrealizedGain = BigDecimal.ZERO;
        BigDecimal totalRealizedGain = BigDecimal.ZERO;
        BigDecimal totalHoldingsValue = BigDecimal.ZERO;

        // Calculate value from holdings
        for (Holding h : holdings) {
            totalInvested = totalInvested.add(h.getTotalCostBasis());
            totalUnrealizedGain = totalUnrealizedGain.add(h.getUnrealizedGain(h.getCurrentValue(BigDecimal.valueOf(h.getStock().getStockValue()))));
            totalRealizedGain = totalRealizedGain.add(h.getRealizedGain());
            totalHoldingsValue = totalHoldingsValue.add(h.getCurrentValue(h.getCurrentValue(BigDecimal.valueOf(h.getStock().getStockValue()))));
        }

        // Fetch dividends
        BigDecimal totalDividends = dividendCalculationService.calculateTotalDividends(accountId);

        // Fetch cash balance
        BigDecimal cashBalance = account.getAccountBalance();
        if (cashBalance == null) {
            cashBalance = BigDecimal.ZERO;
        }

        // Total portfilio balance
        BigDecimal totalPortfolioValue = cashBalance.add(totalHoldingsValue);

        // Calculate roi
        BigDecimal roi = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            roi = totalPortfolioValue.subtract(totalInvested)
                    .divide(totalInvested, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Build dto
        PortfolioPerformanceDTO dto = new PortfolioPerformanceDTO();
        dto.setAccountId(accountId);
        dto.setTotalPortfolioValue(totalPortfolioValue);
        dto.setTotalInvested(totalInvested);
        dto.setTotalRealizedGain(totalRealizedGain);
        dto.setTotalUnrealizedGain(totalUnrealizedGain);
        dto.setTotalDividends(totalDividends);
        dto.setCashBalance(cashBalance);
        dto.setRoiPercentage(roi);

        return dto;
    }

    @Override
    @Transactional
    public void createPortfolioSnapshot(UUID accountId){
        PortfolioPerformanceDTO performance = calculatePortfolioPerformance(accountId);

        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setAccount(accountRepository.findByAccountId(accountId));
        snapshot.setTotalValue(performance.getTotalPortfolioValue());
        snapshot.setCashBalance(performance.getCashBalance());
        snapshot.setTotalInvested(performance.getTotalInvested());
        snapshot.setRealizedGain(performance.getTotalRealizedGain());
        snapshot.setUnrealizedGain(performance.getTotalUnrealizedGain());
        snapshot.setTotalDividends(performance.getTotalDividends());
        snapshot.setRoiPercentage(performance.getRoiPercentage());
        snapshot.setSnapshotDate(java.time.LocalDate.now());

        snapshotRepository.save(snapshot);

    }



}
