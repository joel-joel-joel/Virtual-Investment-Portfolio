package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // Get all accounts
    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    // Get account by ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable UUID id) {
        AccountDTO account = accountService.getAccountById(id);
        if (account != null) {
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create new account
    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody AccountCreateRequest request) {
        AccountDTO created = accountService.createAccount(request);
        return ResponseEntity.ok(created);
    }

    // Update account
    @PutMapping("/{id}")
    public ResponseEntity<AccountDTO> updateAccount(
            @PathVariable UUID id,
            @RequestBody AccountUpdateRequest request
    ) {
        AccountDTO updated = accountService.updateAccount(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete account
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    // Get transactions for an account
    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getAccountTransactions(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getTransactionsForAccount(id));
    }

    // Get holdings for an account
    @GetMapping("/{id}/holdings")
    public ResponseEntity<?> getAccountHoldings(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getHoldingsForAccount(id));
    }
}
