package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "watchlist",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "stock_id"}))
public class Watchlist {

    // This entity tracks stocks that users are watching
    // A user can only have one watchlist entry per stock

    // Constructors
    public Watchlist(UUID watchlistId, User user, Stock stock, LocalDateTime addedAt) {
        this.watchlistId = watchlistId;
        this.user = user;
        this.stock = stock;
        this.addedAt = addedAt;
    }

    public Watchlist() {}


    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID watchlistId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime addedAt;


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;


    // Getters and Setters
    public UUID getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(UUID watchlistId) {
        this.watchlistId = watchlistId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }


    // Helper Functions
    @PrePersist
    public void prePersist() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
    }
}
