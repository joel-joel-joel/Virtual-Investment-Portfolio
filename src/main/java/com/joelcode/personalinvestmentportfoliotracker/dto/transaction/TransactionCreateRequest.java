package com.joelcode.personalinvestmentportfoliotracker.dto.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionCreateRequest {

    // Transaction request creation DTO (input)
    @NotBlank (message = "Stock code is required")
    private final UUID stockId;

    @NotNull(message = "Account id is required")
    private final UUID accountId;

    @NotNull(message = "Share quantity is required")
    @Positive (message = "Share quantity must be positive or zero")
    private final BigDecimal shareQuantity;

    @NotNull (message = "Price per share is required")
    @Positive(message = "Price per share must be positive or zero")
    private final BigDecimal pricePerShare;

    @NotNull (message = "Transaction type is required")
    private final Transaction.type transactionType;



    // Jackson-compatible constructor
    @JsonCreator
    public TransactionCreateRequest(
            @JsonProperty ("stockId") UUID stockId,
            @JsonProperty ("accountId") UUID accountId,
            @JsonProperty ("shareQuantity") BigDecimal shareQuantity,
            @JsonProperty ("pricePerShare") BigDecimal pricePerShare,
            @JsonProperty ("transactionType") Transaction.type transactionType) {
        this.stockId = stockId;
        this.accountId = accountId;
        this.shareQuantity = shareQuantity;
        this.pricePerShare = pricePerShare;
        this.transactionType = transactionType;
    }

    // Getters
    public UUID getStockId() {return stockId;}

    public UUID getAccountId() {return accountId;}

    public BigDecimal getShareQuantity() {return shareQuantity;}

    public BigDecimal getPricePerShare() {return pricePerShare;}

    public Transaction.type getTransactionType() {return transactionType;}
}
