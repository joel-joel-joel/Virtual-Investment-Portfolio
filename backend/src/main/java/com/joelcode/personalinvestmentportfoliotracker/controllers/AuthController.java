package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.dto.auth.AuthResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.ChangePasswordRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.ForgotPasswordRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.LoginRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.LoginResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.PasswordResetRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.RegistrationRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.RefreshTokenRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.jwt.JwtTokenProvider;
import com.joelcode.personalinvestmentportfoliotracker.model.CustomUserDetails;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Profile("!test")
public class AuthController {

    // Define key fields
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // In-memory store for password reset tokens (userId -> resetToken)
    private static final Map<UUID, String> resetTokens = new HashMap<>();

    // Constructor
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Login to existing account - POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Find user by email first
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid email or password"));

            // Create a new authentication with username and password. Check validity with authenticationmanager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );

            // Store authentication object into context holder
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate token for the user
            String token = jwtTokenProvider.generateToken(user);

            // Create new login response DTO for frontend with user information and roles
            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    "Bearer",
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    List.of(user.getRoles())
            );

            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            // Handle authentication failures (bad password, disabled account, etc.)
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Username already exists"));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email already exists"));
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(User.Role.ROLE_USER);

        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        // Calculate token expiration time
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationDate(token);

        // Build response DTO
        AuthResponseDTO responseDTO = new AuthResponseDTO(
                token,
                user.getUserId(),
                user.getEmail(),
                expiresAt
        );

        return ResponseEntity.ok(responseDTO);
    }


    // GET /api/auth/me - Get current user profile
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();

        UserDTO dto = new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getCreatedAt()
        );
        return ResponseEntity.ok(dto);
    }


    // POST /api/auth/refresh - Refresh JWT token
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            jwtTokenProvider.validateToken(refreshToken);

            UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newToken = jwtTokenProvider.generateToken(user);
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
            AuthResponseDTO response = new AuthResponseDTO(newToken, user.getUserId(), user.getEmail(), expiresAt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // GET /api/auth/verify - Verify if token is valid
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("valid", false));
        }

        try {
            String token = authHeader.substring(7);
            jwtTokenProvider.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", true));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    // POST /api/auth/logout - Logout (optional server-side validation)
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        // Note: JWT tokens are stateless, so logout is typically client-side (discard token)
        // This endpoint can be used for server-side token blacklisting if implemented
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    // POST /api/auth/change-password - Change user password
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        User dbUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), dbUser.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        dbUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(dbUser);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // POST /api/auth/forgot-password - Request password reset token
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a simple reset token (in production, use UUID and store with expiration)
        String resetToken = UUID.randomUUID().toString();
        resetTokens.put(user.getUserId(), resetToken);

        // Return the token to user (in production, send via email instead)
        return ResponseEntity.ok(Map.of(
                "message", "Password reset token generated. Use this token to reset your password.",
                "resetToken", resetToken,
                "email", user.getEmail()
        ));
    }

    // POST /api/auth/reset-password - Reset password with token
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        // Find user by reset token
        UUID userId = null;
        for (Map.Entry<UUID, String> entry : resetTokens.entrySet()) {
            if (entry.getValue().equals(request.getResetToken())) {
                userId = entry.getKey();
                break;
            }
        }

        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired reset token"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Remove used token
        resetTokens.remove(userId);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    // GET /api/auth/verify-reset-token - Verify if reset token is valid
    @GetMapping("/verify-reset-token/{token}")
    public ResponseEntity<Map<String, Boolean>> verifyResetToken(@PathVariable String token) {
        boolean isValid = resetTokens.values().contains(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

}
