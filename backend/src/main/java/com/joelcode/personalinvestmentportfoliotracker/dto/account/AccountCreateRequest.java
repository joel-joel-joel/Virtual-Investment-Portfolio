package com.joelcode.personalinvestmentportfoliotracker.dto.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountCreateRequest {

    // Account creation request DTO (input)
    @NotBlank(message = "Account name is required")
    @Size(min = 3, max = 50, message = "Account name must be between 3 and 50 characters")
    private String accountName;

    @NotNull(message = "User id is required")
    private UUID userId;

    // Initial cash balance (optional, defaults to 0)
    private BigDecimal cashBalance;

    // Jackson-compatible constructor
    @JsonCreator
    public AccountCreateRequest(
            @JsonProperty("accountName") String accountName,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("cashBalance") BigDecimal cashBalance) {
        this.accountName = accountName;
        this.userId = userId;
        this.cashBalance = cashBalance;
    }

    public AccountCreateRequest() {}

    // Getters and setters
    public String getAccountName() {
        return accountName;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setAccountName(String accountName) {this.accountName = accountName;}

    public void setUserId(UUID userId) {this.userId = userId;}

    public void setCashBalance(BigDecimal cashBalance) {this.cashBalance = cashBalance;}

}
