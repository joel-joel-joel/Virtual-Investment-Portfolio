package com.joelcode.personalinvestmentportfoliotracker.entities;

import io.micrometer.common.KeyValues;
import jakarta.persistence.*;
import net.minidev.json.annotate.JsonIgnore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    // This entity stores is the overarching account for the app that can store different investing accounts

    // Constructors
    public User (UUID userId, String email, String username, String password, String fullName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public User () {}

    // Columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", updatable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roles = Role.ROLE_USER;

    // Notification Preferences
    @Column(name = "price_alerts", nullable = false)
    private Boolean priceAlerts = true;

    @Column(name = "portfolio_updates", nullable = false)
    private Boolean portfolioUpdates = true;

    @Column(name = "market_news", nullable = false)
    private Boolean marketNews = false;

    @Column(name = "dividend_notifications", nullable = false)
    private Boolean dividendNotifications = true;

    @Column(name = "earning_season", nullable = false)
    private Boolean earningSeason = false;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }


    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();


    // Getters and Setters

    public UUID getUserId() {return userId;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public Role getRoles() {return roles;}

    public void setRoles(Role role) {this.roles = role;}

    // Notification Preferences Getters/Setters
    public Boolean getPriceAlerts() { return priceAlerts; }
    public void setPriceAlerts(Boolean priceAlerts) { this.priceAlerts = priceAlerts; }

    public Boolean getPortfolioUpdates() { return portfolioUpdates; }
    public void setPortfolioUpdates(Boolean portfolioUpdates) { this.portfolioUpdates = portfolioUpdates; }

    public Boolean getMarketNews() { return marketNews; }
    public void setMarketNews(Boolean marketNews) { this.marketNews = marketNews; }

    public Boolean getDividendNotifications() { return dividendNotifications; }
    public void setDividendNotifications(Boolean dividendNotifications) { this.dividendNotifications = dividendNotifications; }

    public Boolean getEarningSeason() { return earningSeason; }
    public void setEarningSeason(Boolean earningSeason) { this.earningSeason = earningSeason; }

    public LocalDateTime getCreatedAt() {return createdAt;}

    // Helper Functions

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }

    public String getRoleNames() {return roles.name();}

    @PrePersist
    public void prePersist() {

        if (this.fullName == null || this.fullName.isBlank()) {
            this.fullName = "Anonymous User";
        }

        if (this.password == null || this.password.isBlank()) {
            this.password = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12);
        }

        if (this.email == null || this.email.isBlank()) {
            String random = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12);
            this.email = "user-" + random + "@auto.local";
        }
    }


}

