package com.joelcode.personalinvestmentportfoliotracker.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    // Login request DTO (input)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private final String username;

    @NotBlank (message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private final String password;


    // Jackson-compatible constructor
    @JsonCreator
    public LoginRequest(
            @JsonProperty ("username") String username,
            @JsonProperty ("password") String password){
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getUsername() { return username; }

    public String getPassword() { return password; }
}
