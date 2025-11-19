package com.joelcode.personalinvestmentportfoliotracker.exceptions;

import com.joelcode.personalinvestmentportfoliotracker.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bad credentials/fail authentication exceptions

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

    // Validation exceptions

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

    // Handle spring validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ApiResponse.validationError(errors)
        );
    }

    // Not found exceptions

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

    // Handle IllegalArgumentException (for not found scenarios)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        // Check if it's a "not found" scenario
        String message = ex.getMessage().toLowerCase();
        if (message.contains("not found") || message.contains("does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(ex.getMessage(), 404)
            );
        }

        // Otherwise treat as bad request
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(ex.getMessage(), 400)
        );
    }

    // Business logic exceptions

    // Handle runtime exceptions (business logic errors)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        // Check for common business logic error patterns
        String message = ex.getMessage();

        if (message != null) {
            // Insufficient balance, already exists, etc.
            if (message.toLowerCase().contains("insufficient") ||
                    message.toLowerCase().contains("already exists") ||
                    message.toLowerCase().contains("cannot")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ApiResponse.error(message, 400)
                );
            }
        }

        // Generic runtime error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("An unexpected error occurred: " + message, 500)
        );
    }

    // Genetic Exception

    // Catch-all for any unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        // Log the full stack trace for debugging
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("An unexpected error occurred. Please try again later.", 500)
        );
    }

    // Custom Exceptions

    // Handle insufficient balance specifically
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientBalance(
            InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(ex.getMessage(), 400)
        );
    }

    // Handle duplicate entity
    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEntity(
            DuplicateEntityException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error(ex.getMessage(), 409)
        );
    }
}

