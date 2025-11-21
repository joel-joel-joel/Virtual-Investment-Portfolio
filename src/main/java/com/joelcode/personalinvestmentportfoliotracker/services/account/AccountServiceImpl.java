package com.joelcode.personalinvestmentportfoliotracker.services.account;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.AccountMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.TransactionMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryServiceImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class AccountServiceImpl implements AccountService {

    // Define key fields
    private final AccountRepository accountRepository;
    private final AccountValidationService accountValidationService;
    private final TransactionMapper transactionMapper;
    private final HoldingMapper holdingMapper;
    private final PriceHistoryService priceHistoryService;
    private final SimpMessagingTemplate messagingTemplate;


    // Constructor
    public AccountServiceImpl(AccountRepository accountRepository, AccountValidationService accountValidationService,
                              TransactionMapper transactionMapper, HoldingMapper holdingMapper,
                              PriceHistoryService priceHistoryService,
                              SimpMessagingTemplate messagingTemplate) {
        this.accountRepository = accountRepository;
        this.accountValidationService = accountValidationService;
        this.transactionMapper = transactionMapper;
        this.holdingMapper = holdingMapper;
        this.priceHistoryService = priceHistoryService;
        this.messagingTemplate = messagingTemplate;
    }


    // Interface functions

    // Create a new account and show essential information
    @Override
    public AccountDTO createAccount(AccountCreateRequest request) {
        accountValidationService.validateAccountExistsByName(request.getAccountName());

        // Map accuont creation request to entity
        Account account = AccountMapper.toEntity(request);

        // Save account to db
        account = accountRepository.save(account);

        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                account.getAccountId(),
                BigDecimal.ZERO, // New account is created with a balance of zero
                BigDecimal.ZERO, // Zero change as it is a creation
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + account.getAccountId(),
                updateMessage
        );

        // Map entity back to dto
        return AccountMapper.toDTO(account);
    }

    // Find account by ID
    @Override
    public AccountDTO getAccountById(UUID accountId) {
        Account account = accountValidationService.validateAccountExistsById(accountId);

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
        Account account = accountValidationService.validateAccountExistsById(accountId);

        BigDecimal previousBalance = account.getAccountBalance();

        AccountMapper.updateEntity(account, request);

        account = accountRepository.save(account);

        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                account.getAccountId(),
                account.getAccountBalance(),
                account.getAccountBalance().subtract(previousBalance), // Zero change as it is a creation
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + account.getAccountId(),
                updateMessage
        );

        return AccountMapper.toDTO(account);
    }

    // Delete account
    @Override
    public void deleteAccount(UUID accountId) {
        Account account = accountValidationService.validateAccountExistsById(accountId);

        accountRepository.delete(account);
    }


    // Transactional related methods

    // Update account balance include zero case
    @Override
    public void updateAccountBalance(Account account, BigDecimal amount){
        BigDecimal currentBalance = account.getAccountBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        account.setAccountBalance(newBalance);
        accountRepository.save(account);

        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                account.getAccountId(),
                account.getAccountBalance(),
                newBalance.subtract(currentBalance), // Zero change as it is a creation
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + account.getAccountId(),
                updateMessage
        );

    }

    // Retrieve all transaction for an account
    @Override
    public List<TransactionDTO> getTransactionsForAccount(UUID accountId) {
        // Validate account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Get transactions from account entity
        List<Transaction> transactions = account.getTransactions(); // assuming OneToMany

        // Map to DTOs
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionMapper::toDTO)
                .collect(Collectors.toList());

        return transactionDTOs;
    }

    // Get all the holdings for an account
    @Override
    public List<HoldingDTO> getHoldingsForAccount(UUID accountId) {
        // Validate the account exists
        Account account = accountValidationService.validateAccountExistsById(accountId);

        // Stream through holdings and map to DTOs with current price
        List<HoldingDTO> holdingDTOs = account.getHoldings().stream()
                .map(h -> {
                    BigDecimal currentPrice = priceHistoryService.getCurrentPrice(h.getStock().getStockId());
                    return HoldingMapper.toDTO(h, currentPrice);
                })
                .collect(Collectors.toList());

        return holdingDTOs;
    }
}




