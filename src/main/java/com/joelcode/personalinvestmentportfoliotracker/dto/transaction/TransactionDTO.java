package com.joelcode.personalinvestmentportfoliotracker.dto.transaction;

import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionDTO {

    // Transaction response DTO (output)
    private final UUID transactionId;
    private final UUID stockId;
    private final UUID accountId;
    private final BigDecimal shareQuantity;
    private final BigDecimal pricePerShare;

    // Constructors
    public TransactionDTO(UUID transactionId, UUID stockId, UUID accountId, BigDecimal shareQuantity, BigDecimal pricePerShare) {
        this.transactionId = transactionId;
        this.stockId = stockId;
        this.accountId = accountId;
        this.shareQuantity = shareQuantity;
        this.pricePerShare = pricePerShare;
    }

    public TransactionDTO(Transaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.stockId = transaction.getStock().getStockId();
        this.accountId = transaction.getAccount().getAccountId();
        this.shareQuantity = transaction.getShareQuantity();
        this.pricePerShare = transaction.getPricePerShare();
    }


    // Getters
    public UUID getTransactionId() {return transactionId;}

    public UUID getStockCode() {return stockId;}

    public UUID getAccountId() {return accountId;}

    public BigDecimal getShareQuantity() {return shareQuantity;}

    public BigDecimal getPricePerShare() {return pricePerShare;}
}
