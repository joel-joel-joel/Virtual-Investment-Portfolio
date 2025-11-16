package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DividendRepository extends JpaRepository<Dividend, UUID> {

    // Find dividends by stock
    List<Dividend> findByStock(Stock stock);

    List<Dividend> findByStock_StockId(UUID stockId);

    List<Dividend> findByStock_StockCode(String stockCode);

    List<Dividend> findByStock_CompanyName(String companyName);

    // Find dividends by account
    Optional<Dividend> findByAccountIdAndStockId(UUID accountId, UUID stockId);

    List<Dividend> findAllByAccountId(UUID accountId);

    // Existence check
    boolean existsByStockAndPayDate(Stock stock, LocalDateTime payDate);

    // Filter by amount per share
    List<Dividend> findByAmountPerShareGreaterThan(BigDecimal amountPerShare);

    List<Dividend> findByAmountPerShareLessThan(BigDecimal amountPerShare);

    // Filter by date
    List<Dividend> findByPayDate(LocalDateTime payDate);

    List<Dividend> findByPayDateAfter(LocalDateTime payDate);

    List<Dividend> findByPayDateBefore(LocalDateTime payDate);

    List<Dividend> findByPayDateBetween(LocalDateTime start, LocalDateTime end);
}
