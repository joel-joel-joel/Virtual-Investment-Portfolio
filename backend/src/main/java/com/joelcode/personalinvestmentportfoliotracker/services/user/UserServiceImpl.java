package com.joelcode.personalinvestmentportfoliotracker.services.user;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserPreferencesDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserPreferencesUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.AccountMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.UserMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
@Transactional
public class UserServiceImpl implements UserService{

    // Define key fields
    private final UserRepository userRepository;
    private final UserValidationService userValidationService;
    private final PasswordEncoder passwordEncoder;


    // Constructor
    public UserServiceImpl(UserRepository userRepository, UserValidationService userValidationService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userValidationService = userValidationService;
        this.passwordEncoder = passwordEncoder;
    }


    // Interface functions

    // Create a new user and show its essential information
    @Override
    public UserDTO createUser(UserCreateRequest request) {

        // Check if user exists
        userValidationService.validateEmailUnique(request.getEmail());

        // Check if username is unique
        userValidationService.validateUsernameUnique(request.getUsername());

        // Map request to entity (process creation request to an entity)
        User user = UserMapper.toEntity(request);

        // Save user to db
        user = userRepository.save(user);

        // Map entity to dto (show information of the user)
        return UserMapper.toDTO(user);
    }

    // Find a user by their ID
    @Override
    public UserDTO getUserById(UUID userId) {

        // Check if user ID is in the repository
        User user = userValidationService.validateUserExists(userId);

        // Map entity to dto (show information of the user)
        return UserMapper.toDTO(user);
    }

    // Generate a list of all the users inclusive of their information
    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Update user entity by given userId
    @Override
    public UserDTO updateUser(UUID userId, UserUpdateRequest request) {

        // Check if user exists
        User user = userValidationService.validateUserExists(userId);

        // Update fields from request
        UserMapper.updateEntity(user, request);

        user = userRepository.save(user);

        // Map entity to dto
        return UserMapper.toDTO(user);
    }

    // Delete user by userId
    @Override
    public void deleteUser(UUID userId) {
        User user = userValidationService.validateUserExists(userId);

        userRepository.delete(user);
    }

    // Get all accounts by user
    public List<AccountDTO> getAllAccountsForUser(UUID userId) {
        // Validate user exists
        User user = userValidationService.validateUserExists(userId);

        // Get accounts from user entity
        List<Account> accounts = user.getAccounts(); // assuming a OneToMany relationship

        // Map to DTOs
        List<AccountDTO> accountDTOs = accounts.stream()
                .map(AccountMapper::toDTO)  // use your mapper
                .collect(Collectors.toList());
        return accountDTOs;
    }

    // Get user preferences
    @Override
    public UserPreferencesDTO getUserPreferences(UUID userId) {
        User user = userValidationService.validateUserExists(userId);
        return UserMapper.toPreferencesDTO(user);
    }

    // Update user preferences
    @Override
    public UserPreferencesDTO updateUserPreferences(UUID userId, UserPreferencesUpdateRequest request) {
        User user = userValidationService.validateUserExists(userId);

        UserMapper.updatePreferences(user, request);
        user = userRepository.save(user);

        return UserMapper.toPreferencesDTO(user);
    }

    // Get current authenticated user's preferences
    @Override
    public UserPreferencesDTO getCurrentUserPreferences() {
        try {
            // Check SecurityContext exists
            SecurityContext context = SecurityContextHolder.getContext();
            if (context == null) {
                throw new RuntimeException("Security context not available");
            }

            // Check Authentication exists
            Authentication authentication = context.getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("User not authenticated");
            }

            // Check Principal is valid CustomUserDetails
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                throw new RuntimeException("Invalid authentication principal");
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;

            // Check User entity exists
            User user = userDetails.getUser();
            if (user == null) {
                throw new RuntimeException("User entity not found in authentication");
            }

            // Ensure preferences are not null (defensive)
            ensurePreferencesNotNull(user);

            return UserMapper.toPreferencesDTO(user);

        } catch (ClassCastException e) {
            throw new RuntimeException("Authentication principal is not a CustomUserDetails instance", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve user preferences: " + e.getMessage(), e);
        }
    }

    // Update current authenticated user's preferences
    @Override
    public UserPreferencesDTO updateCurrentUserPreferences(UserPreferencesUpdateRequest request) {
        try {
            // Check SecurityContext exists
            SecurityContext context = SecurityContextHolder.getContext();
            if (context == null) {
                throw new RuntimeException("Security context not available");
            }

            // Check Authentication exists
            Authentication authentication = context.getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("User not authenticated");
            }

            // Check Principal is valid CustomUserDetails
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof CustomUserDetails)) {
                throw new RuntimeException("Invalid authentication principal");
            }

            CustomUserDetails userDetails = (CustomUserDetails) principal;

            // Check User entity exists
            User user = userDetails.getUser();
            if (user == null) {
                throw new RuntimeException("User entity not found in authentication");
            }

            // Ensure preferences are not null before updating (defensive)
            ensurePreferencesNotNull(user);

            UserMapper.updatePreferences(user, request);
            user = userRepository.save(user);

            return UserMapper.toPreferencesDTO(user);

        } catch (ClassCastException e) {
            throw new RuntimeException("Authentication principal is not a CustomUserDetails instance", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user preferences: " + e.getMessage(), e);
        }
    }

    // Helper method to ensure preferences have valid values
    private void ensurePreferencesNotNull(User user) {
        if (user.getPriceAlerts() == null) user.setPriceAlerts(true);
        if (user.getPortfolioUpdates() == null) user.setPortfolioUpdates(true);
        if (user.getMarketNews() == null) user.setMarketNews(false);
        if (user.getDividendNotifications() == null) user.setDividendNotifications(true);
        if (user.getEarningSeason() == null) user.setEarningSeason(false);
    }
}