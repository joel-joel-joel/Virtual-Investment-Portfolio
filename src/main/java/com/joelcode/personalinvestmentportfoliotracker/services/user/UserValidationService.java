package com.joelcode.personalinvestmentportfoliotracker.services.user;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("!test")
public class UserValidationService {

    // Define key field
    private final UserRepository userRepository;


    // Constructor
    public UserValidationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // Validation functions

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

    // Check that username is unique
    public void validateUsernameUnique(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
    }
}