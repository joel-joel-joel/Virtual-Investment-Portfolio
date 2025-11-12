package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // Find transactions
    List<Transaction> findByAccount_AccountId(Account accountId);

    List<Transaction> findByStock_StockId(Long stockId);

    List<Transaction> findByStock_StockCode(String stockCode);

    List<Transaction> findByStock_CompanyName(String companyName);

    List<Transaction> findByTrnactionId(UUID transactionId);

    // Find by stock and account
    List<Transaction> findByAccount_AccountIdAndStock_StockId(UUID accountId, Long stockId);

    List<Transaction> findByAccount_AccountIdAndStock_StockCode(UUID accountId, String stockCode);

    List<Transaction> findByAccount_AccountIdAndStock_CompanyName(UUID accountId, String companyName);

    // Existence check
    boolean existsByTransactionId(UUID transactionId);

    // Filter by share quantity
    List<Transaction> findByShareQuantity (BigDecimal shareQuantity);

    List<Transaction> findByShareQuantityGreaterThan(BigDecimal amountShareQuantity);

    List<Transaction> findByShareQuantityLessThan(BigDecimal amountShareQuantity);

    // Filter by price per share
    List<Transaction> findByPricePerShare (BigDecimal pricePerShare);

    List<Transaction> findByPricePerShareGreaterThan(BigDecimal amountPricePerShare);

    List<Transaction> findByPricePerShareLessThan(BigDecimal amountPricePerShare);

    // Filter by date
    List<Transaction> findByCreatedAtAfter(LocalDateTime createdAtAfter);

    List<Transaction> findByCreatedAtBefore(LocalDateTime createdAtBefore);

    List<Transaction> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
