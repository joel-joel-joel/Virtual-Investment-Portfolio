package com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PortfolioSnapshotValidationService {

    private final PortfolioSnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;

    public PortfolioSnapshotValidationService(PortfolioSnapshotRepository snapshotRepository,
                                              AccountRepository accountRepository) {
        this.snapshotRepository = snapshotRepository;
        this.accountRepository = accountRepository;
    }

    public PortfolioSnapshot validateSnapshotExists(UUID snapshotId) {
        return snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new RuntimeException("Portfolio snapshot not found with ID: " + snapshotId));
    }

    public Account validateAccountExists(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
    }

    public void validateSnapshotDoesNotExist(Account account, LocalDate snapshotDate) {
        if (snapshotRepository.existsByAccountAndSnapshotDate(account, snapshotDate)) {
            throw new RuntimeException("Snapshot already exists for this account on date: " + snapshotDate);
        }
    }

    public void validateCreateRequest(BigDecimal totalValue, BigDecimal cashBalance, BigDecimal totalInvested, LocalDate snapshotDate) {
        validateTotalValue(totalValue);
        validateCashBalance(cashBalance);
        validateTotalInvested(totalInvested);
        validateSnapshotDate(snapshotDate);
    }

    public void validateTotalValue(BigDecimal totalValue) {
        if (totalValue == null || totalValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Total value cannot be negative.");
        }
    }

    public void validateCashBalance(BigDecimal cashBalance) {
        if (cashBalance == null || cashBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Cash balance cannot be negative.");
        }
    }

    public void validateTotalInvested(BigDecimal totalInvested) {
        if (totalInvested == null || totalInvested.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Total invested cannot be negative.");
        }
    }

    public void validateSnapshotDate(LocalDate snapshotDate) {
        if (snapshotDate == null) {
            throw new RuntimeException("Snapshot date cannot be null.");
        }
        if (snapshotDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("Snapshot date cannot be in the future.");
        }
    }

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date cannot be null.");
        }
        if (startDate.isAfter(endDate)) {
            throw new RuntimeException("Start date cannot be after end date.");
        }
    }
}