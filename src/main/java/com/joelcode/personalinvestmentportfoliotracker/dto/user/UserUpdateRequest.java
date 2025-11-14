package com.joelcode.personalinvestmentportfoliotracker.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {

    // User update request DTO (input)
    // Non-mandatory fields for single variable updates
    @Email(message = "Must be a valid email")
    private String email;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    //  Constructor
    public UserUpdateRequest(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public UserUpdateRequest() {}

    //Getters and setters
    public String getEmail() {return email;}

    public void setEmail(String email) {}

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}
}
