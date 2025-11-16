package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, UUID> {

    // Find by account
    List<Holding> findByAccount(Account account);

    List<Holding> findByAccountOrderByTotalCostBasisDesc(Account account);

    List<Holding> findAllByAccountId(UUID accountId);

    // Find by stock
    List<Holding> findByStock(Stock stock);

    Optional<Holding> findByAccountAndStock(Account account, Stock stock);

    // Find specific id
    Optional<Holding> findByHoldingId(UUID holdingId);

    // Existence checks
    boolean existsByAccountAndStock(Account account, Stock stock);

    // Filter by quantity
    List<Holding> findByQuantityGreaterThan(BigDecimal quantity);

    List<Holding> findByAccountAndQuantityGreaterThan(Account account, BigDecimal quantity);

    // Filter by date
    List<Holding> findByFirstPurchaseDateAfter(LocalDateTime firstPurchaseDate);

    List<Holding> findByFirstPurchaseDateBetween(LocalDateTime start, LocalDateTime end);

    // Custom queries for analytics
    @Query("SELECT SUM(h.totalCostBasis) FROM Holding h WHERE h.account = :account")
    BigDecimal sumTotalCostBasisByAccount(@Param("account") Account account);

    @Query("SELECT COUNT(h) FROM Holding h WHERE h.account = :account")
    Long countByAccount(@Param("account") Account account);
}