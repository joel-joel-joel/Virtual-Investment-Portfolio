package com.joelcode.personalinvestmentportfoliotracker.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuthResponseDTO {

    @JsonProperty("token")
    private String token;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, UUID userId, String email, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
