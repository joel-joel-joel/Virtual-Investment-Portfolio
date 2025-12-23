package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserPreferencesDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserPreferencesUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Profile("!test")
public class UserController {

    private final UserService userService;

    // Constructor injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users (admin only ideally)
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get a user
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id) {
        UserDTO user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update a user profile
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID id,
            @RequestBody UserUpdateRequest updatedUser
    ) {
        UserDTO user = userService.updateUser(id, updatedUser);

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Get all accounts from specific user
    @GetMapping("/{id}/accounts")
    public ResponseEntity<?> getUserAccounts(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getAllAccountsForUser(id));
    }

    // Get user preferences for specific user (admin/self)
    @GetMapping("/{id}/preferences")
    public ResponseEntity<UserPreferencesDTO> getUserPreferences(@PathVariable UUID id) {
        UserPreferencesDTO preferences = userService.getUserPreferences(id);
        return ResponseEntity.ok(preferences);
    }

    // Update user preferences for specific user (admin/self)
    @PutMapping("/{id}/preferences")
    public ResponseEntity<UserPreferencesDTO> updateUserPreferences(
            @PathVariable UUID id,
            @RequestBody @Valid UserPreferencesUpdateRequest request
    ) {
        UserPreferencesDTO preferences = userService.updateUserPreferences(id, request);
        return ResponseEntity.ok(preferences);
    }

    // Get current authenticated user's preferences
    @GetMapping("/me/preferences")
    public ResponseEntity<UserPreferencesDTO> getCurrentUserPreferences() {
        UserPreferencesDTO preferences = userService.getCurrentUserPreferences();
        return ResponseEntity.ok(preferences);
    }

    // Update current authenticated user's preferences
    @PutMapping("/me/preferences")
    public ResponseEntity<UserPreferencesDTO> updateCurrentUserPreferences(
            @RequestBody @Valid UserPreferencesUpdateRequest request
    ) {
        UserPreferencesDTO preferences = userService.updateCurrentUserPreferences(request);
        return ResponseEntity.ok(preferences);
    }
}
