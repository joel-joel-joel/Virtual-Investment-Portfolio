package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.AccountMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    // Define key fields
    private final AccountRepository accountRepository;
    private final AccountValidationService accountValidationService;

    // Constructor
    public AccountServiceImpl(AccountRepository accountRepository, AccountValidationService accountValidationService){
        this.accountRepository = accountRepository;
        this.accountValidationService = accountValidationService;
    }

    // Interface functions

    // Create a new account and show essential information
    @Override
    public AccountDTO createAccount(AccountCreateRequest request) {
        accountValidationService.validateAccountExists(request.getUserId());

        // Map accuont creation request to entity
        Account account = AccountMapper.toEntity(request);

        // Save account to db
        account = accountRepository.save(account);

        // Map entity back to dto
        return AccountMapper.toDTO(account);
    }

    // Find account by ID
    @Override
    public AccountDTO getAccountById(UUID accountId) {
        Account account = accountValidationService.validateAccountExists(accountId);

        return AccountMapper.toDTO(account);
    }

    // Generate a list of all the accounts inclusive of their information
    @Override
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(AccountMapper::toDTO)
                .toList();
    }

    // Update user entity by given userId
    @Override
    public AccountDTO updateAccount(UUID accountId, AccountUpdateRequest request) {
        Account account = accountValidationService.validateAccountExists(accountId);

        AccountMapper.updateEntity(account, request);

        account = accountRepository.save(account);

        return AccountMapper.toDTO(account);
    }

    // Delete account
    @Override
    public void deleteAccount(UUID accountId) {
        Account account = accountValidationService.validateAccountExists(accountId);

        accountRepository.delete(account);
    }
}

