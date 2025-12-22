package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.DividendPayment;
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
public interface DividendPaymentRepository extends JpaRepository<DividendPayment, UUID> {

    // Finding by account/dividend/stock
    List<DividendPayment> findByAccount_AccountId(UUID accountId);

    List<DividendPayment> findByStock_StockId(UUID stockId);

    List<DividendPayment> findByDividend_DividendId(UUID dividendId);

    Optional<DividendPayment> findByAccount_AccountIdAndDividend_DividendId(UUID accountId, UUID dividendId);

    // Security fix: Filter dividend payments by user through account → user relationship
    List<DividendPayment> findByAccount_User_UserId(UUID userId);

    // Check if payment exists
    boolean existsByAccountAndDividend(Account account, Dividend dividend);


    // Account specific queries

    // Get all payments for account ordered by date
    @Query("SELECT dp FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "ORDER BY dp.paymentDate DESC")
    List<DividendPayment> findPaymentsByAccountOrderByDate(@Param("accountId") UUID accountId);

    // Get payments for account in date range
    @Query("SELECT dp FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.paymentDate BETWEEN :start AND :end")
    List<DividendPayment> findPaymentsByAccountAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Get payments by account and stock
    @Query("SELECT dp FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.stock.stockId = :stockId " +
            "ORDER BY dp.paymentDate DESC")
    List<DividendPayment> findPaymentsByIdAccountAndStockId(
            @Param("accountId") UUID accountId,
            @Param("stockId") UUID stockId);


    // Aggregation queries

    // Calculate total dividends received by account
    @Query("SELECT COALESCE(SUM(dp.totalAmount), 0) FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.status = 'PAID'")
    BigDecimal calculateTotalDividendsByAccount(@Param("accountId") UUID accountId);

    // Calculate total dividends for account by stock
    @Query("SELECT COALESCE(SUM(dp.totalAmount), 0) FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.stock.stockId = :stockId " +
            "AND dp.status = 'PAID'")
    BigDecimal calculateTotalDividendsByAccountAndStock(
            @Param("accountId") UUID accountId,
            @Param("stockId") UUID stockId);

    // Calculate total dividends in date range
    @Query("SELECT COALESCE(SUM(dp.totalAmount), 0) FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.paymentDate BETWEEN :start AND :end " +
            "AND dp.status = 'PAID'")
    BigDecimal calculateDividendsInDateRange(
            @Param("accountId") UUID accountId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


    // Status queries

    // Find pending payments for account
    @Query("SELECT dp FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.status = :status " +
            "ORDER BY dp.paymentDate DESC")
    List<DividendPayment> findPaymentsByAccountAndStatus(
            @Param("accountId") UUID accountId,
            @Param("status") DividendPayment.PaymentStatus status);

    // Find all pending payments across all accounts
    List<DividendPayment> findByStatus(DividendPayment.PaymentStatus status);


    // Reporting queries

    // Get top dividend-paying stocks for an account
    @Query("SELECT dp.stock.stockCode, SUM(dp.totalAmount) as total " +
            "FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.status = 'PAID' " +
            "GROUP BY dp.stock.stockCode " +
            "ORDER BY total DESC")
    List<Object[]> getTopDividendPayingStocks(@Param("accountId") UUID accountId);

    // Get monthly dividend summary for account
    @Query("SELECT YEAR(dp.paymentDate), MONTH(dp.paymentDate), SUM(dp.totalAmount) " +
            "FROM DividendPayment dp " +
            "WHERE dp.account.accountId = :accountId " +
            "AND dp.status = 'PAID' " +
            "GROUP BY YEAR(dp.paymentDate), MONTH(dp.paymentDate) " +
            "ORDER BY YEAR(dp.paymentDate) DESC, MONTH(dp.paymentDate) DESC")
    List<Object[]> getMonthlyDividendSummary(@Param("accountId") UUID accountId);

    // Count payments by account
    @Query("SELECT COUNT(dp) FROM DividendPayment dp WHERE dp.account.accountId = :accountId")
    Long countPaymentsByAccount(@Param("accountId") UUID accountId);
}
