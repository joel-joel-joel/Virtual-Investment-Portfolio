package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.entities.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

    // Find by user
    List<Watchlist> findByUser(User user);

    List<Watchlist> findByUser_UserId(UUID userId);

    List<Watchlist> findByUserOrderByAddedAtDesc(User user);

    @Query("SELECT w FROM Watchlist w WHERE w.user.userId = :userId ORDER BY w.addedAt DESC")
    List<Watchlist> findAllByUserIdOrderByAddedAtDesc(@Param("userId") UUID userId);


    // Find by stock
    List<Watchlist> findByStock(Stock stock);

    List<Watchlist> findByStock_StockId(UUID stockId);


    // Find specific watchlist entry
    Optional<Watchlist> findByUserAndStock(User user, Stock stock);

    Optional<Watchlist> findByUser_UserIdAndStock_StockId(UUID userId, UUID stockId);

    Optional<Watchlist> findByWatchlistId(UUID watchlistId);


    // Existence checks
    boolean existsByUserAndStock(User user, Stock stock);

    boolean existsByUser_UserIdAndStock_StockId(UUID userId, UUID stockId);


    // Filter by date
    List<Watchlist> findByAddedAtAfter(LocalDateTime addedAt);

    List<Watchlist> findByAddedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Watchlist> findByUserAndAddedAtAfter(User user, LocalDateTime addedAt);


    // Custom queries
    @Query("SELECT COUNT(w) FROM Watchlist w WHERE w.user = :user")
    Long countByUser(@Param("user") User user);

    @Query("SELECT COUNT(w) FROM Watchlist w WHERE w.user.userId = :userId")
    Long countByUserId(@Param("userId") UUID userId);
}
