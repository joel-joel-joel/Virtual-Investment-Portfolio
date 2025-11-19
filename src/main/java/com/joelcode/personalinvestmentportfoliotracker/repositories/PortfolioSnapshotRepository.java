package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, UUID> {


    // Find by user
    List<PortfolioSnapshot> findByUser_IdOrderByDateDesc(UUID userId);

    // Find by account
    List<PortfolioSnapshot> findByAccount(Account account);

    List<PortfolioSnapshot> findByAccountOrderBySnapshotDateDesc(Account account);

    List<PortfolioSnapshot> findByAccountOrderBySnapshotDateAsc(Account account);

    List<PortfolioSnapshot> findByAccount_IdOrderByDateDesc(UUID accountId);

    // Find by date
    Optional<PortfolioSnapshot> findByAccountAndSnapshotDate(Account account, LocalDate snapshotDate);

    List<PortfolioSnapshot> findBySnapshotDateAfter(LocalDate snapshotDate);

    List<PortfolioSnapshot> findBySnapshotDateBetween(LocalDate start, LocalDate end);

    List<PortfolioSnapshot> findByAccountAndSnapshotDateBetween(Account account, LocalDate start, LocalDate end);

    // Find specific id
    Optional<PortfolioSnapshot> findBySnapshotId(UUID snapshotId);

    // Existence checks
    boolean existsByAccountAndSnapshotDate(Account account, LocalDate snapshotDate);

    // Filter by created date
    List<PortfolioSnapshot> findByCreatedAtAfter(LocalDateTime createdAtAfter);

    List<PortfolioSnapshot> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Custom queries for analytics
    @Query("SELECT ps FROM PortfolioSnapshot ps WHERE ps.account = :account ORDER BY ps.snapshotDate DESC LIMIT 1")
    Optional<PortfolioSnapshot> findLatestByAccount(@Param("account") Account account);

    @Query("SELECT ps FROM PortfolioSnapshot ps WHERE ps.account = :account ORDER BY ps.snapshotDate ASC LIMIT 1")
    Optional<PortfolioSnapshot> findEarliestByAccount(@Param("account") Account account);

    @Query("SELECT AVG(ps.totalValue) FROM PortfolioSnapshot ps WHERE ps.account = :account AND ps.snapshotDate BETWEEN :start AND :end")
    BigDecimal averageTotalValueByAccountAndDateRange(@Param("account") Account account, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(ps) FROM PortfolioSnapshot ps WHERE ps.account = :account")
    Long countByAccount(@Param("account") Account account);
}