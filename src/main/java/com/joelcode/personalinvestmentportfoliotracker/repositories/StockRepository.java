package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository  extends JpaRepository<Stock, Long> {

    // Finding specific stocks
    Optional<Stock> findByCode(String code);

    Optional<Stock> findByCompanyName(String companyName);


    // Filter lists of stocks
    @Query("SELECT s FROM Stock s WHERE CAST(s.stockId AS string) LIKE %?1%")
    List<Stock> findByStockIdLike(Long stockId);

    @Query ("SELECT s FROM Stock s WHERE s.stockCode LIKE %?1%")
    List<Stock> findByStockCodeLike(String stockCode);

    List<Stock> findByCompanyNameContainingIgnoreCase(String companyName);

    // Existence checks
    boolean existsByStockCode(String stockCode);

    boolean existsByCompanyName(String companyName);

    boolean existsByStockId(Long stockId);

    // Filter by date
    @Query("SELECT s FROM Stock s WHERE s.createdAt > ?1")
    List<Stock> findByCreatedAtAfter(LocalDateTime date);

    @Query ("SELECT s FROM Stock s WHERE s.createdAt < ?1")
    List<Stock> findByCreatedAtBefore(LocalDateTime date);


    // Finding by ID
    @Query ("SELECT s FROM Stock s WHERE s.account.accountId = ?1")
    List<Stock> findByAccountId(UUID accountId);


    // Filter by stock value
    List<Stock> findByStockValueGreaterThan(Double stockValue);

    List<Stock> findByStockValueLessThan(Double stockValue);

}
