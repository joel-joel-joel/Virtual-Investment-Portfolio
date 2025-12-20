package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "watchlist", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_id"}))
public class Watchlist {

    public Watchlist() {}

    public Watchlist(User user, Stock stock) {
        this.user = user;
        this.stock = stock;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "watchlist_id")
    private UUID watchlistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    public UUID getWatchlistId() { return watchlistId; }
    public void setWatchlistId(UUID watchlistId) { this.watchlistId = watchlistId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Stock getStock() { return stock; }
    public void setStock(Stock stock) { this.stock = stock; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
