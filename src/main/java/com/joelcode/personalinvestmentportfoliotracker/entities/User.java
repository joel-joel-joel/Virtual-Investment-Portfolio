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

    // Constructor

    public User (UUID userId, String email, String username, String password, String fullName) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public User () {}

    // Key fields

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(updatable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roles = Role.ROLE_USER;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();


    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }

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

