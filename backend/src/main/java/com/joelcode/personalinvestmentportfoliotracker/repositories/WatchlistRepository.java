package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.entities.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {

    List<Watchlist> findByUser(User user);

    List<Watchlist> findByUserId(UUID userId);

    Optional<Watchlist> findByUserIdAndStockId(UUID userId, UUID stockId);

    void deleteByUserIdAndStockId(UUID userId, UUID stockId);

    boolean existsByUserIdAndStockId(UUID userId, UUID stockId);
}
