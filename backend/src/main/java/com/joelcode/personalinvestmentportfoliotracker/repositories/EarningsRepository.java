package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Earnings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EarningsRepository extends JpaRepository<Earnings, UUID> {

    List<Earnings> findByStockId(UUID stockId);

    @Query("SELECT e FROM Earnings e WHERE e.earningsDate BETWEEN :startDate AND :endDate ORDER BY e.earningsDate ASC")
    List<Earnings> findUpcomingEarnings(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Earnings e WHERE e.stock.stockId IN :stockIds AND e.earningsDate BETWEEN :startDate AND :endDate ORDER BY e.earningsDate ASC")
    List<Earnings> findUpcomingEarningsForStocks(@Param("stockIds") List<UUID> stockIds, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Earnings> findByEarningsDateGreaterThanEqualOrderByEarningsDateAsc(LocalDate date);
}
