package com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment;

import com.joelcode.personalinvestmentportfoliotracker.entities.DividendPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DividendPaymentDTO {

    // Response DTO for dividend payment information
    // Response DTO (output)
    private final UUID paymentId;
    private final UUID accountId;
    private final String accountName;
    private final UUID stockId;
    private final String stockCode;
    private final UUID dividendId;
    private final BigDecimal amountPerShare;
    private final BigDecimal shareQuantity;
    private final BigDecimal totalAmount;
    private final LocalDateTime paymentDate;
    private final LocalDateTime recordedAt;
    private final DividendPayment.PaymentStatus status;

    // Constructor
    public DividendPaymentDTO(UUID paymentId, UUID accountId, String accountName,
                              UUID stockId, String stockCode, UUID dividendId,
                              BigDecimal amountPerShare, BigDecimal shareQuantity,
                              BigDecimal totalAmount, LocalDateTime paymentDate,
                              LocalDateTime recordedAt, DividendPayment.PaymentStatus status) {
        this.paymentId = paymentId;
        this.accountId = accountId;
        this.accountName = accountName;
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.dividendId = dividendId;
        this.amountPerShare = amountPerShare;
        this.shareQuantity = shareQuantity;
        this.totalAmount = totalAmount;
        this.paymentDate = paymentDate;
        this.recordedAt = recordedAt;
        this.status = status;
    }

    public DividendPaymentDTO(DividendPayment payment) {
        this.paymentId = payment.getPaymentId();
        this.accountId = payment.getAccountId();
        this.accountName = payment.getAccount() != null ? payment.getAccount().getAccountName() : null;
        this.stockId = payment.getStockId();
        this.stockCode = payment.getStock() != null ? payment.getStock().getStockCode() : null;
        this.dividendId = payment.getDividendId();
        this.amountPerShare = payment.getDividend() != null ? payment.getDividend().getAmountPerShare() : null;
        this.shareQuantity = payment.getShareQuantity();
        this.totalAmount = payment.getTotalAmount();
        this.paymentDate = payment.getPaymentDate();
        this.recordedAt = payment.getRecordedAt();
        this.status = payment.getStatus();
    }

    // Getters

    public UUID getPaymentId() {return paymentId;}

    public UUID getAccountId() {return accountId;}

    public String getAccountName() {return accountName;}

    public UUID getStockId() {return stockId;}

    public String getStockCode() {return stockCode;}

    public UUID getDividendId() {return dividendId;}

    public BigDecimal getAmountPerShare() {return amountPerShare;}

    public BigDecimal getShareQuantity() {return shareQuantity;}

    public BigDecimal getTotalAmount() {return totalAmount;}

    public LocalDateTime getPaymentDate() {return paymentDate;}

    public LocalDateTime getRecordedAt() {return recordedAt;}

    public DividendPayment.PaymentStatus getStatus() {return status;}
}