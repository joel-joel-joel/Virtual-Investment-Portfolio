package com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DividendPaymentCreateRequest {

    // Request to record a dividend payment to an account
    // Dividend create request dto (input)
    @NotNull(message = "Account ID is required")
    private final UUID accountId;

    @NotNull(message = "Dividend ID is required")
    private final UUID dividendId;

    @NotNull(message = "Share quantity is required")
    @Positive(message = "Share quantity must be positive")
    private final BigDecimal shareQuantity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime paymentDate;

    // Constructor
    @JsonCreator
    public DividendPaymentCreateRequest(
            @JsonProperty("accountId") UUID accountId,
            @JsonProperty("dividendId") UUID dividendId,
            @JsonProperty("shareQuantity") BigDecimal shareQuantity,
            @JsonProperty("paymentDate") LocalDateTime paymentDate) {
        this.accountId = accountId;
        this.dividendId = dividendId;
        this.shareQuantity = shareQuantity;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDateTime.now();
    }

    // Getters
    public UUID getAccountId() {return accountId;}

    public UUID getDividendId() {return dividendId;}

    public BigDecimal getShareQuantity() {return shareQuantity;}

    public LocalDateTime getPaymentDate() {return paymentDate;}
}