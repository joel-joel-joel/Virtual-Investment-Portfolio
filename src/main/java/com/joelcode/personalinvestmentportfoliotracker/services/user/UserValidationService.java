package com.joelcode.personalinvestmentportfoliotracker.services.user;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;

import java.util.UUID;

public class UserValidationService {

    // Define user repository
    private final UserRepository userRepository;

    // Constructor
    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Check if email is already used
    public void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with this email already exists");
        }
    }

    // Validate user exists
    public User validateUserExists(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
