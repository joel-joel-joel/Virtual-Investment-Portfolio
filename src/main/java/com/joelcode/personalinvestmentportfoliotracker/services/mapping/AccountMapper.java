package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;

public class AccountMapper {

    // Convert account creation request DTO to entity
    public static Account toEntity(AccountCreateRequest request) {
        Account account = new Account();
        account.setAccountName(request.getAccountName());
        account.setUserId(request.getUserId());
        return account;
    }

    // Update account entity from update request DTO
    public static void updateEntity(Account account, AccountUpdateRequest request) {
        if (request.getAccountName() != null) {account.setAccountName(request.getAccountName());}
        if (request.getUserId() != null) {account.setUserId(request.getUserId());}
    }

    // Convert account entity to response account DTO
    public static AccountDTO toDTO(Account account) {
        if (account == null) return null;
        return new AccountDTO(account.getAccountName(),
                account.getAccountId(),
                UserMapper.toDTO(account.getUser()),
                account.getAccountBalance());
    }
}
