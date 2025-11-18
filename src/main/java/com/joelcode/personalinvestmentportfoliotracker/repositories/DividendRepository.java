package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DividendRepository extends JpaRepository<Dividend, UUID> {

    // Stock-level dividend queries only (NO account linkage)

    List<Dividend> findByStock(Stock stock);

    List<Dividend> findByStock_StockId(UUID stockId);

    List<Dividend> findByStock_StockCode(String stockCode);

    boolean existsByStockAndPayDate(Stock stock, LocalDateTime payDate);

    List<Dividend> findByAmountPerShareGreaterThan(BigDecimal amountPerShare);

    List<Dividend> findByAmountPerShareLessThan(BigDecimal amountPerShare);

    List<Dividend> findByPayDate(LocalDateTime payDate);

    List<Dividend> findByPayDateAfter(LocalDateTime payDate);

    List<Dividend> findByPayDateBefore(LocalDateTime payDate);

    List<Dividend> findByPayDateBetween(LocalDateTime start, LocalDateTime end);

    // Find all upcoming dividends
    @Query("SELECT d FROM Dividend d WHERE d.payDate > :currentDate ORDER BY d.payDate ASC")
    List<Dividend> findUpcomingDividends(@Param("currentDate") LocalDateTime currentDate);

    // Find dividends announced in date range
    @Query("SELECT d FROM Dividend d WHERE d.announcementDate BETWEEN :start AND :end")
    List<Dividend> findDividendsAnnouncedBetween(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}