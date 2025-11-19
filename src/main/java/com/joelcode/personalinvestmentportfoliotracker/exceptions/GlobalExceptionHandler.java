package com.joelcode.personalinvestmentportfoliotracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles custom authentication failures with 401 Unauthorized response
    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<?> handleCustomAuthException(CustomAuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "error", "AUTHENTICATION_FAILED",
                        "message", ex.getMessage()
                )
        );
    }

    // Handles bad credentials (invalid username/password) with 401 Unauthorized response
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "error", "BAD_CREDENTIALS",
                        "message", "Invalid username or password"
                )
        );
    }

    // Handles general authentication errors with 401 Unauthorized response
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "error", "AUTH_ERROR",
                        "message", ex.getMessage()
                )
        );
    }

    // Handles validation errors (invalid input data) with 400 Bad Request response
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "error", "VALIDATION_ERROR",
                        "message", ex.getMessage()
                )
        );
    }

    // Handles not found errors (entity not found in database) with 404 Not Found response
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "error", "NOT_FOUND",
                        "message", ex.getMessage()
                )
        );
    }

    // Handles all other unexpected exceptions with 500 Internal Server Error response
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "error", "INTERNAL_SERVER_ERROR",
                        "message", "An unexpected error occurred"
                )
        );
    }
}
