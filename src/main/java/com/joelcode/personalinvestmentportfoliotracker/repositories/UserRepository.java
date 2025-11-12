package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find specific id
    Optional<User> findByUserId(UUID userId);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String usernameFragment);

    // Existence checks
    boolean existsByUserID(UUID userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);


    // Filter by date
    List<User> findByCreatedAtAfter(LocalDateTime createdAtAfter);

    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
