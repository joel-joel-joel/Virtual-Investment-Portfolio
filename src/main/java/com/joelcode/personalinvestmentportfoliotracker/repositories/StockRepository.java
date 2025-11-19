package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository  extends JpaRepository<Stock, UUID> {

    // Finding specific stocks
    Optional<Stock> findByStockCode(String stockCode);

    Optional<Stock> findByStockId(UUID stockId);

    Optional<Stock> findByCompanyName(String companyName);

    // Filter lists of stocks
    List<Stock> findByCompanyNameContainingIgnoreCase(String companyName);

    List<Stock> findByNameContainingIgnoreCase(String name);

    // Existence checks
    boolean existsByStockCode(String stockCode);

    boolean existsByCompanyName(String companyName);

    // Filter by date
    List<Stock> findByCreatedAtAfter(LocalDateTime date);

    List<Stock> findByCreatedAtBefore(LocalDateTime date);

    List<Stock> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);


    // Finding by ID
    List<Stock> findByAccount_AccountId(UUID accountId);

    // Filter by stock value
    List<Stock> findByStockValueGreaterThan(Double stockValue);

    List<Stock> findByStockValueLessThan(Double stockValue);
}
