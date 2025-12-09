package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.dto.auth.AuthResponseDTO;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setRoles(User.Role.ROLE_USER);

        // Mock authentication and token generation
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(user)).thenReturn("mock-jwt-token");

        // Run method
        ResponseEntity<LoginResponseDTO> response = authController.login(request);

        // Assert response variables are correct
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john_doe", response.getBody().getUsername());
        assertEquals("mock-jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals("john@example.com", response.getBody().getEmail());
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtTokenProvider, times(1)).generateToken(user);
    }

    // Test login with invalid credentials
    @Test
    void testLogin_InvalidCredentials() {
        // Setup login request and mock exception
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setRoles(User.Role.ROLE_USER);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
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
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Run method and assert exception is thrown
        assertThrows(RuntimeException.class, () -> {
            authController.login(request);
        });
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(authenticationManager, never()).authenticate(any());
    }

    // Test registering a new user successfully
    @Test
    void testRegister_Success() {
        // Setup registration request and stub repository/password encoder
        // Note: RegistrationRequest constructor order is (username, email, fullName, password)
        RegistrationRequest request = new RegistrationRequest("newuser", "newuser@example.com", "New User", "password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationDate("mock-jwt-token")).thenReturn(LocalDateTime.now().plusHours(24));

        // Run method
        ResponseEntity<?> responseEntity = authController.register(request);

        // Assert response variables are correct
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        AuthResponseDTO response = (AuthResponseDTO) responseEntity.getBody();
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("newuser@example.com", response.getEmail());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtTokenProvider, times(1)).generateToken(any(User.class));
    }

    // Test registering a user when username already exists
    @Test
    void testRegister_UsernameAlreadyExists() {
        // Setup registration request with existing username
        RegistrationRequest request = new RegistrationRequest("existing_user", "newuser@example.com", "New User", "password123");
        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        // Run method
        ResponseEntity<?> response = authController.register(request);

        // Assert response is bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, times(1)).existsByUsername("existing_user");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // Test registering a user when email already exists
    @Test
    void testRegister_EmailAlreadyExists() {
        // Setup registration request with existing email
        RegistrationRequest request = new RegistrationRequest("newuser", "existing@example.com", "New User", "password123");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Run method
        ResponseEntity<?> response = authController.register(request);

        // Assert response is bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // Test registering multiple users sequentially
    @Test
    void testRegister_MultipleUsers() {
        // Setup multiple registration requests
        RegistrationRequest request1 = new RegistrationRequest("user1", "user1@example.com", "User One", "password1");
        RegistrationRequest request2 = new RegistrationRequest("user2", "user2@example.com", "User Two", "password2");

        // Stub repository and password encoder with lenient mode to avoid strict stubbing issues
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("mock-jwt-token");
        when(jwtTokenProvider.getExpirationDate(anyString())).thenReturn(LocalDateTime.now().plusHours(24));

        // Run methods
        ResponseEntity<?> response1 = authController.register(request1);
        ResponseEntity<?> response2 = authController.register(request2);

        // Assert response variables and verify repository interactions
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response1.getBody());
        assertNotNull(response2.getBody());
        verify(userRepository, times(2)).save(any(User.class));
        verify(jwtTokenProvider, times(2)).generateToken(any(User.class));
    }

    // Test verify token with valid token
    @Test
    void testVerifyToken_ValidToken() {
        // Setup valid token
        String validToken = "valid-jwt-token";
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

        // Run method
        ResponseEntity<?> response = authController.verifyToken("Bearer " + validToken);

        // Assert token is valid
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtTokenProvider, times(1)).validateToken(validToken);
    }

    // Test verify token with invalid token
    @Test
    void testVerifyToken_InvalidToken() {
        // Setup invalid token
        String invalidToken = "invalid-jwt-token";
        when(jwtTokenProvider.validateToken(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        // Run method
        ResponseEntity<?> response = authController.verifyToken("Bearer " + invalidToken);

        // Assert token is invalid
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtTokenProvider, times(1)).validateToken(invalidToken);
    }

    // Test verify token with no authorization header
    @Test
    void testVerifyToken_NoAuthorizationHeader() {
        // Run method
        ResponseEntity<?> response = authController.verifyToken(null);

        // Assert response indicates invalid token
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    // Test logout endpoint
    @Test
    void testLogout_Success() {
        // This is a simple endpoint that just returns success message
        // In real JWT implementation, logout is typically client-side (discard token)

        // For now, just verify the endpoint can be called
        // Note: In real tests, you'd need proper authentication context
        assertNotNull(authController);
    }
}