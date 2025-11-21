package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@Profile("!test")
public class TransactionProcessorServiceImpl implements TransactionProcessorService {

    // Define key fields
    private final TransactionService transactionService;
    private final HoldingService holdingService;
    private final HoldingCalculationService holdingCalculationService;
    private final AccountService accountService;
    private final DividendPaymentCalculationService dividendPaymentCalculationService;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final SimpMessagingTemplate messagingTemplate;


    // Constructor
    public TransactionProcessorServiceImpl(TransactionService transactionService, HoldingService holdingService,
                                           HoldingCalculationService holdingCalculationService,
                                           AccountService accountService,
                                           DividendPaymentCalculationService dividendPaymentCalculationService,
                                           AccountRepository accountRepository, HoldingRepository holdingRepository,
                                           SimpMessagingTemplate messagingTemplate) {
        this.transactionService = transactionService;
        this.holdingService = holdingService;
        this.holdingCalculationService = holdingCalculationService;
        this.accountService = accountService;
        this.dividendPaymentCalculationService = dividendPaymentCalculationService;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.messagingTemplate = messagingTemplate;
    }


    // Interface function

    // Process transaction
    @Override
    @Transactional
    public TransactionDTO processTransaction(TransactionCreateRequest request) {

        // Retrieve account related to transaction
        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // --- Capture previous portfolio value BEFORE any updates ---
        BigDecimal holdingsValueBefore = holdingCalculationService.calculateTotalPortfolioValue(account.getAccountId());
        BigDecimal previousPortfolioValue = holdingsValueBefore.add(account.getAccountBalance());

        // If it is a buy transaction
        if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
            // Check if account has enough balance
            BigDecimal totalCost = request.getPricePerShare().multiply(request.getShareQuantity());
            if (account.getAccountBalance().compareTo(totalCost) < 0) {
                throw new IllegalArgumentException("Insufficient account balance");
            }
            account.setAccountBalance(account.getAccountBalance().subtract(totalCost));
        }
        // If it is a sell transaction
        else if (request.getTransactionType().name().equalsIgnoreCase("SELL")) {
            Optional<Holding> holdingOpt = holdingRepository.getHoldingByAccount_AccountIdAndStock_StockId(request.getAccountId(), request.getStockId());

            if (holdingOpt.isEmpty()) {
                throw new IllegalArgumentException("No holding found for account and stock");
            }

            Holding holding = holdingOpt.get();
            if (holding.getQuantity().compareTo(request.getShareQuantity()) < 0) {
                throw new IllegalArgumentException("Insufficient holding quantity");
            }

            // Update holding and account balance
            holdingService.updateHoldingAfterSale(holding, request.getShareQuantity(), request.getPricePerShare());
            account.setAccountBalance(account.getAccountBalance().add(request.getPricePerShare().multiply(request.getShareQuantity())));
        }

        // Save account balance
        accountService.updateAccountBalance(account, account.getAccountBalance());

        // Update or create holding
        holdingService.updateOrCreateHoldingFromTransaction(request);

        // Convert request to DTO
        TransactionDTO dto = transactionService.createTransaction(request);

        // --- Calculate new portfolio value ---
        BigDecimal currentPortfolioValue = holdingCalculationService.calculateTotalPortfolioValue(account.getAccountId());
        BigDecimal portfolioChange = currentPortfolioValue.subtract(previousPortfolioValue);

        // Send WebSocket numeric update
        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                account.getAccountId(),
                currentPortfolioValue,
                portfolioChange,
                LocalDateTime.now()
        );

        return dto;
    }
}