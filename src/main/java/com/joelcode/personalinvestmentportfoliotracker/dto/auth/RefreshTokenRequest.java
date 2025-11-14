package com.joelcode.personalinvestmentportfoliotracker.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RefreshTokenRequest {

    // Refresh token request DTO (input)
    @NotBlank(message = "Refresh token is required")
    @Size(min = 20, message = "Refresh token seems invalid")
    private String refreshToken;

    // Jackson-compatible constructor
    @JsonCreator
    public RefreshTokenRequest(@JsonProperty ("refreshToken") String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getter and Setter
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
