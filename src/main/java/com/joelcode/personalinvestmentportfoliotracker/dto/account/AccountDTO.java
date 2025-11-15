package com.joelcode.personalinvestmentportfoliotracker.dto.account;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountDTO {

    // Account response DTO (output)
    private final String accountName;
    private final UUID accountId;
    private final UserDTO user;
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

    // Getters
    public String getAccountName() {return accountName;}

    public UUID getAccountId() {return accountId;}

    public UserDTO getUser() {return user;}

    public BigDecimal getCashBalance() {return cashBalance;}
}
