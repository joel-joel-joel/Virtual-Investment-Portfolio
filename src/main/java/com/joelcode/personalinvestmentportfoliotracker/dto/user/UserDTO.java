package com.joelcode.personalinvestmentportfoliotracker.dto.user;

import com.joelcode.personalinvestmentportfoliotracker.entities.User;

import java.util.UUID;

public class UserDTO {

    // User response DTO (output)
    private final UUID userId;
    private final String username;
    private final String email;

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

    // Getters
    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {return email;}

}
