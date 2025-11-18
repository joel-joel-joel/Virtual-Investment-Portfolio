package com.joelcode.personalinvestmentportfoliotracker.controllers;

import com.joelcode.personalinvestmentportfoliotracker.dto.auth.AuthResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.LoginRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.LoginResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.auth.RegistrationRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.jwt.JwtTokenProvider;
import com.joelcode.personalinvestmentportfoliotracker.repositories.UserRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

        // Create a new authentication with raw username and password. Check validity with authenticationmanager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Store authentication object into context holder
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate token for the user
        String token = jwtTokenProvider.generateToken(userRepository.findByUsername(request.getUsername()).orElseThrow());

        // Retrieve user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
    }

    // Register a new user - POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {

        // Check whether username is available
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // Create and populate user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(User.Role.ROLE_USER);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

}
