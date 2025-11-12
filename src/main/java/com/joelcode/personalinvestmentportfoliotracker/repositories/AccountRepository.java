package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    // Find account
    Optional<Account> findByAccountName(String accountName);

    // Existence checks
    boolean existsByAccountId(UUID accountId);

    boolean existsByAccountName(String accountName);

    // Filter by date
    List<Account> findByCreatedAtAfter(LocalDateTime createdAtAfter);

    @Query("SELECT a FROM Account a WHERE a.createdAt BETWEEN :start AND :end")
    List<Account> findAccountWithinDateRange(LocalDateTime start, LocalDateTime end);

    // Filter list of accounts
    List<Account> findByUser_UserId(UUID userId, Pageable pageable);

    List<Account> findByAccountNameContainingIgnoreCase(String accountNameFragment);

    List<Account> findByUser_UserIdAndAccountNameContainingIgnoreCase(UUID userId, String accountNameFragment);

    // Filter by value
    List<Account> findByAccountBalanceGreaterThan(BigDecimal accountBalance);

    List<Account> findByAccountBalanceLessThan(BigDecimal accountBalance);

}
