package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // By account
    List<Transaction> findByAccount(Account account);

    List<Transaction> findByAccount_AccountId(UUID accountId);

    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account);

    List<Transaction> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId);

    // Security fix: Filter transactions by user through account relationship
    List<Transaction> findByAccount_User_UserId(UUID userId);

    // By stock
    List<Transaction> findByStock(Stock stock);

    List<Transaction> findByStock_StockId(UUID stockId);

    List<Transaction> findByStock_StockCode(String stockCode);

    List<Transaction> findByStock_CompanyName(String companyName);


    // By account AND stock
    List<Transaction> findByAccountAndStock(Account account, Stock stock);

    List<Transaction> findByAccount_AccountIdAndStock_StockId(UUID accountId, UUID stockId);

    List<Transaction> findByAccount_AccountIdAndStock_StockCode(UUID accountId, String stockCode);

    List<Transaction> findByAccount_AccountIdAndStock_CompanyName(UUID accountId, String companyName);


    // Filter by share quantity
    List<Transaction> findByShareQuantity(BigDecimal shareQuantity);

    List<Transaction> findByShareQuantityGreaterThan(BigDecimal amount);

    List<Transaction> findByShareQuantityLessThan(BigDecimal amount);


    // Filter by price per share
    List<Transaction> findByPricePerShare(BigDecimal price);

    List<Transaction> findByPricePerShareGreaterThan(BigDecimal price);

    List<Transaction> findByPricePerShareLessThan(BigDecimal price);


    // Filter by created date
    List<Transaction> findByCreatedAtAfter(LocalDateTime createdAt);

    List<Transaction> findByCreatedAtBefore(LocalDateTime createdAt);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByAccount_AccountIdAndCreatedAtBetween(UUID accountId, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByStock_StockIdAndCreatedAtBetween(UUID stockId, LocalDateTime start, LocalDateTime end);
}
