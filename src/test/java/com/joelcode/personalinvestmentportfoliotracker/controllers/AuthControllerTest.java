package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.dto.auth.LoginRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.LoginResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.RegistrationRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.jwt.JwtTokenProvider;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        // Initialize controller and inject mocked dependencies
        authController = new AuthController(
                authenticationManager,
                jwtTokenProvider,
                userRepository,
                passwordEncoder
        );
    }

    // Test successful login with correct credentials
    @Test
    void testLogin_Success() {
        // Setup login request and mock user data
        LoginRequest request = new LoginRequest("john_doe", "password123");
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setRoles(User.Role.ROLE_USER);

        // Mock authentication and token generation
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn("mock-jwt-token");

        // Run method
        ResponseEntity<LoginResponseDTO> response = authController.login(request);

        // Assert response variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("john_doe", response.getBody().getUsername());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenProvider, times(1)).generateToken(user);
    }

    // Test login with invalid credentials
    @Test
    void testLogin_InvalidCredentials() {
        // Setup login request and mock exception
        LoginRequest request = new LoginRequest("john_doe", "wrongpassword");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        // Run method and assert exception is thrown
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> {
            authController.login(request);
        });
        verify(authenticationManager, times(1)).authenticate(any());
    }

    // Test login when user is not found
    @Test
    void testLogin_UserNotFound() {
        // Setup login request and mock empty repository response
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Run method and assert exception is thrown
        assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });
        verify(authenticationManager, times(1)).authenticate(any());
    }

    // Test registering a new user successfully
    @Test
    void testRegister_Success() {
        // Setup registration request and stub repository/password encoder
        RegistrationRequest request = new RegistrationRequest("newuser", "newuser@example.com", "password123", "New User");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        // Run method
        ResponseEntity<?> response = authController.register(request);

        // Assert response variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Test registering a user when username already exists
    @Test
    void testRegister_UsernameAlreadyExists() {
        // Setup registration request and stub repository to indicate username exists
        RegistrationRequest request = new RegistrationRequest("existing_user", "newuser@example.com", "New User", "password123");
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Run method
        ResponseEntity<?> response = authController.register(request);

        // Assert response variables are correct
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already exists", response.getBody());
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // Test registering multiple users sequentially
    @Test
    void testRegister_MultipleUsers() {
        // Setup multiple registration requests
        RegistrationRequest request1 = new RegistrationRequest("user1", "user1@example.com", "User One", "password1");
        RegistrationRequest request2 = new RegistrationRequest("user2", "user2@example.com", "User Two", "password2");

        // Stub repository and password encoder
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // Run methods
        ResponseEntity<?> response1 = authController.register(request1);
        ResponseEntity<?> response2 = authController.register(request2);

        // Assert response variables and verify repository interactions
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(userRepository, times(2)).save(any(User.class));
    }
}
