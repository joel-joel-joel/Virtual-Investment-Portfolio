package com.joelcode.personalinvestmentportfoliotracker.dto.user;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;

import java.util.UUID;

public class UserDTO {

    // User response DTO (output)
    private UUID userId;
    private String username;
    private String email;

    // Constructor
    public UserDTO(UUID userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }

    public UserDTO() {}

    // Getters and setters
    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {return email;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public void setUsername(String username) {this.username = username;}

    public void setEmail(String email) {this.email = email;}


}
