package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountDTO createAccount(AccountCreateRequest request)       ;

    AccountDTO getAccountById(UUID accountId);

    List<AccountDTO> getAllAccounts();

    AccountDTO updateAccount(UUID acccountId, AccountUpdateRequest request);

    void deleteAccount(UUID accountId);
}

