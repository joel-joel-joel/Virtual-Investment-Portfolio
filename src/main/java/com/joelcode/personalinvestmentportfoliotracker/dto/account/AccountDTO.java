package com.joelcode.personalinvestmentportfoliotracker.dto.account;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountDTO {

    // Account response DTO (output)
    private String accountName;
    private UUID accountId;
    private UserDTO user;
    private BigDecimal cashBalance;

    // Constructor
    public AccountDTO(String accountName, UUID accountId, UserDTO user, BigDecimal cashBalance) {
        this.accountName = accountName;
        this.accountId = accountId;
        this.user = user;
        this.cashBalance = cashBalance;
    }

    public AccountDTO(Account account) {
        this.accountName = account.getAccountName();
        this.accountId = account.getAccountId();
        this.user = new UserDTO(account.getUser());
        this.cashBalance = account.getAccountBalance();
    }

    public AccountDTO() {}

    // Getters and setters
    public String getAccountName() {return accountName;}

    public UUID getAccountId() {return accountId;}

    public UserDTO getUser() {return user;}

    public BigDecimal getCashBalance() {return cashBalance;}

    public void setAccountName(String accountName) {this.accountName = accountName;}

    public void setAccountId(UUID accountId) {this.accountId = accountId;}

    public void setUser(UserDTO user) {this.user = user;}

    public void setCashBalance(BigDecimal cashBalance) {this.cashBalance = cashBalance;}
}
