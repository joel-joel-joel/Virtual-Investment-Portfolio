package com.joelcode.personalinvestmentportfoliotracker.dto.account;

import com.joelcode.personalinvestmentportfoliotracker.dto.user.UserDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;

import java.util.UUID;

public class AccountDTO {

    // Account response DTO  (output)
    private final String accountName;
    private final UUID accountId;
    private final UserDTO user;

    // Constructor
    public AccountDTO(String accountName, UUID accountId, UserDTO user) {
        this.accountName = accountName;
        this.accountId = accountId;
        this.user = user;
    }

    public AccountDTO(Account account) {
        this.accountName = account.getAccountName();
        this.accountId = account.getAccountId();
        this.user = new UserDTO(account.getUser());
    }

    // Getters
    public String getAccountName() {return accountName;}

    public UUID getAccountId() {return accountId;}

    public UserDTO getUser() {return user;}
}
