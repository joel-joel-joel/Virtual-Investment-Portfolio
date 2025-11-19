package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountDTO createAccount(AccountCreateRequest request)       ;

    AccountDTO getAccountById(UUID accountId);

    List<AccountDTO> getAllAccounts();

    AccountDTO updateAccount(UUID acccountId, AccountUpdateRequest request);

    void deleteAccount(UUID accountId);

    void updateAccountBalance(Account account, BigDecimal amount);

    List<TransactionDTO> getTransactionsForAccount(UUID accountId);

    List<HoldingDTO> getHoldingsForAccount(UUID accountId);

}

