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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Profile("!test")
public class TransactionProcessorServiceImpl implements TransactionProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessorServiceImpl.class);

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


    @Override
    @Transactional
    public TransactionDTO processTransaction(TransactionCreateRequest request) {

        // Retrieve account
        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // --- Capture previous portfolio value ---
        BigDecimal holdingsValueBefore = holdingCalculationService.calculateTotalPortfolioValue(account.getAccountId());
        BigDecimal previousPortfolioValue = holdingsValueBefore.add(account.getCashBalance());

        // Handle BUY transactions
        if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {
            logger.info("💰 Processing BUY transaction - Account: {}, Stock: {}, Shares: {}",
                    request.getAccountId(), request.getStockId(), request.getShareQuantity());

            BigDecimal totalCost = request.getPricePerShare().multiply(request.getShareQuantity());

            // Check if account has enough balance
            if (account.getCashBalance().compareTo(totalCost) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient account balance. Need: A$" + totalCost.toPlainString() +
                                ", Have: A$" + account.getCashBalance().toPlainString()
                );
            }

            // Deduct from balance
            account.setCashBalance(account.getCashBalance().subtract(totalCost));
            logger.info("✅ BUY transaction validated - Balance updated: A${}", account.getCashBalance());
        }
        // Handle SELL transactions
        else if (request.getTransactionType().name().equalsIgnoreCase("SELL")) {
            logger.info("💵 Processing SELL transaction - Account: {}, Stock: {}, Shares: {}",
                    request.getAccountId(), request.getStockId(), request.getShareQuantity());

            Optional<Holding> holdingOpt = holdingRepository.getHoldingByAccount_AccountIdAndStock_StockId(
                    request.getAccountId(),
                    request.getStockId()
            );

            if (holdingOpt.isEmpty()) {
                throw new IllegalArgumentException("No holding found for this stock");
            }

            Holding holding = holdingOpt.get();
            if (holding.getQuantity().compareTo(request.getShareQuantity()) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient shares. Own: " + holding.getQuantity().toPlainString() +
                                ", Trying to sell: " + request.getShareQuantity().toPlainString()
                );
            }

            // Update holding and add proceeds to balance
            holdingService.updateHoldingAfterSale(holding, request.getShareQuantity(), request.getPricePerShare());
            BigDecimal saleProceeds = request.getPricePerShare().multiply(request.getShareQuantity());
            account.setCashBalance(account.getCashBalance().add(saleProceeds));

            logger.info("✅ SELL transaction validated - Proceeds added: A${}, New balance: A${}",
                    saleProceeds, account.getCashBalance());

            // ✅ NEW: Check if all shares were sold and delete holding if so
            BigDecimal remainingQuantity = holding.getQuantity().subtract(request.getShareQuantity());
            if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                logger.info("🗑️ All shares sold - Deleting holding - Holding ID: {}, Stock: {}",
                        holding.getHoldingId(), holding.getStock().getStockCode());

                try {
                    holdingRepository.delete(holding);
                    logger.info("✅ Holding deleted successfully - Holding ID: {}", holding.getHoldingId());
                } catch (Exception e) {
                    logger.error("❌ Failed to delete holding - Holding ID: {}, Error: {}",
                            holding.getHoldingId(), e.getMessage());
                    throw new RuntimeException("Failed to delete holding after sale", e);
                }
            } else {
                logger.info("✅ Holding updated - Remaining shares: {}", remainingQuantity);
            }
        }

        // 🔑 KEY: Save account with updated balance
        account = accountRepository.save(account);

        // Create transaction and update/create holding
        holdingService.updateOrCreateHoldingFromTransaction(request);
        TransactionDTO dto = transactionService.createTransaction(request);

        // --- Calculate and broadcast portfolio change ---
        BigDecimal currentPortfolioValue = holdingCalculationService.calculateTotalPortfolioValue(account.getAccountId());
        BigDecimal portfolioChange = currentPortfolioValue.subtract(previousPortfolioValue);

        WebSocketController.PortfolioUpdateMessage updateMessage = new WebSocketController.PortfolioUpdateMessage(
                account.getAccountId(),
                currentPortfolioValue,
                portfolioChange,
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/portfolio/" + account.getAccountId(), updateMessage);

        logger.info("✅ Transaction completed successfully - Transaction ID: {}, Portfolio change: A${}",
                dto.getTransactionId(), portfolioChange);

        return dto;
    }
}