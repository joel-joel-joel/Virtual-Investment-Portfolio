package com.joelcode.personalinvestmentportfoliotracker.services.customuserdetails;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

// This class queries repository to retrieve the information needed for a user entity
@Service
@Profile("!test")
public class CustomUserDetailsService implements UserDetailsService {

    // Define key field
    private final UserRepository userRepository;


    // Constructor
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // Load user by username
    @Override
    public CustomUserDetails loadUserByUsername(String username) {
        // Fetch user using repository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        // Wrap user with mapper
        return new CustomUserDetails(user);
    }

}
