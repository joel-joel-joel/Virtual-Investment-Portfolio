package com.joelcode.personalinvestmentportfoliotracker.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {

    // Register request DTO (input)
    @NotBlank (message = "Email is required")
    @Email (message = "Must be a valid email")
    private final String email;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private final String username;

    @NotBlank (message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private final String password;

    @NotBlank (message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private final String fullName;

    // Jackson-compatible constructor
    @JsonCreator
    public RegistrationRequest(
            @JsonProperty ("email") String email,
            @JsonProperty ("username") String username,
            @JsonProperty ("password") String password,
            @JsonProperty ("fullName") String fullName){
        this.email = email;
        this.username = username;
        this.password = password;
        this.fullName = fullName;

    }

    // Getters
    public String getEmail() {return email;}

    public String getUsername() {return username;}

    public String getPassword() {return password;}

    public String getFullName() {return fullName;}
}
