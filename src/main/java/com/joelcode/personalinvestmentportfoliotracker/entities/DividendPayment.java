package com.joelcode.personalinvestmentportfoliotracker.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dividend_payments")
public class DividendPayment {

    // This entity tracks ACTUAL dividend payments to specific accounts
    // Links: Account + Stock + Dividend + actual shares held

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockId", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dividendId", nullable = false)
    private Dividend dividend;

    @Column(nullable = false)
    private BigDecimal shareQuantity;  // How many shares they owned on pay date

    @Column(nullable = false)
    private BigDecimal totalAmount;  // shareQuantity × amountPerShare

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private LocalDateTime recordedAt;  // When this payment was recorded in system

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    // Constructors
    public DividendPayment() {
        this.recordedAt = LocalDateTime.now();
    }

    public DividendPayment(Account account, Stock stock, Dividend dividend,
                           BigDecimal shareQuantity, BigDecimal totalAmount,
                           LocalDateTime paymentDate) {
        this.account = account;
        this.stock = stock;
        this.dividend = dividend;
        this.shareQuantity = shareQuantity;
        this.totalAmount = totalAmount;
        this.paymentDate = paymentDate;
        this.recordedAt = LocalDateTime.now();
        this.status = PaymentStatus.PAID;
    }

    // Calculate total amount automatically
    public void calculateTotalAmount() {
        if (dividend != null && shareQuantity != null) {
            this.totalAmount = dividend.getAmountPerShare().multiply(shareQuantity);
        }
    }

    public enum PaymentStatus {
        PENDING,    // Dividend announced but not yet paid
        PAID,       // Payment completed
        CANCELLED   // Dividend cancelled
    }

    // Getters and Setters
    public UUID getPaymentId() {return paymentId;}

    public void setPaymentId(UUID paymentId) {this.paymentId = paymentId;}

    public Account getAccount() {return account;}

    public void setAccount(Account account) {this.account = account;}

    public UUID getAccountId() {return account != null ? account.getAccountId() : null;}

    public Stock getStock() {return stock;}

    public void setStock(Stock stock) {this.stock = stock;}

    public UUID getStockId() {return stock != null ? stock.getStockId() : null;}

    public Dividend getDividend() {return dividend;}

    public void setDividend(Dividend dividend) {this.dividend = dividend;}

    public UUID getDividendId() {return dividend != null ? dividend.getDividendId() : null;}

    public BigDecimal getShareQuantity() {return shareQuantity;}

    public void setShareQuantity(BigDecimal shareQuantity) {this.shareQuantity = shareQuantity;}

    public BigDecimal getTotalAmount() {return totalAmount;}

    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}

    public LocalDateTime getPaymentDate() {return paymentDate;}

    public void setPaymentDate(LocalDateTime paymentDate) {this.paymentDate = paymentDate;}

    public LocalDateTime getRecordedAt() {return recordedAt;}

    public void setRecordedAt(LocalDateTime recordedAt) {this.recordedAt = recordedAt;}

    public PaymentStatus getStatus() {return status;}

    public void setStatus(PaymentStatus status) {this.status = status;}
}
