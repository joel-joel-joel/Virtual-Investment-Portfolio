package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class TransactionProcessorServiceImpl implements TransactionProcessorService{

    // Define key fields
    private final TransactionService transactionService;
    private final HoldingService holdingService;
    private final HoldingCalculationService holdingCalculationService;
    private final AccountService accountService;
    private final DividendCalculationService dividendCalculationService;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;


    // Constructor
    public TransactionProcessorServiceImpl (TransactionService transactionService, HoldingService holdingService,
                                            HoldingCalculationService holdingCalculationService,
                                            AccountService accountService,
                                            DividendCalculationService dividendCalculationService, AccountRepository accountRepository, HoldingRepository holdingRepository) {
        this.transactionService = transactionService;
        this.holdingService = holdingService;
        this.holdingCalculationService = holdingCalculationService;
        this.accountService = accountService;
        this.dividendCalculationService = dividendCalculationService;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    // Interface function
    @Override
    @Transactional
    public TransactionDTO processTransaction(TransactionCreateRequest request){

        // Retrieve account related to transaction
        Account account = accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // If it is a buy transaction
        if (request.getTransactionType().name().equalsIgnoreCase("BUY")) {

            // Check if account has enough balance for a buy
            if (account.getAccountBalance().doubleValue() < request.getPricePerShare().doubleValue() * request.getShareQuantity().doubleValue()) {
                throw new IllegalArgumentException("Insufficient account balance");
            } else {
                // Subtract buy amount from balance
                account.setAccountBalance(account.getAccountBalance().subtract(request.getPricePerShare().multiply(request.getShareQuantity())));
            }
        }
        // If it is a sell transaction
        else if (request.getTransactionType().name().equalsIgnoreCase("SELL")) {
            // Retrieve the requested holding for a sale
            Optional<Holding> holdingOpt = holdingRepository.getHoldingByAccountIdAndStockId(request.getAccountId(), request.getStockId());

            // Check if the holding exists
            if (holdingOpt == null){
                throw new IllegalArgumentException("No holding found for account and stock");
            } else {
                Holding holding = holdingOpt.get();
                // Check account has sufficient shares
                if (holding.getQuantity().doubleValue() < request.getShareQuantity().doubleValue()) {
                    throw new IllegalArgumentException("Insufficient holding quantity");
                }

                // Update account holding for the share
                holdingService.updateHoldingAfterSale(holding, request.getShareQuantity(), request.getPricePerShare());

                // Add sale to balance
                account.setAccountBalance(account.getAccountBalance().add(request.getPricePerShare().multiply(request.getShareQuantity())));
            }
        }

        // Save account balance
        accountService.updateAccountBalance(accountRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found")), account.getAccountBalance());

        // Update or create holding
        holdingService.updateOrCreateHoldingFromTransaction(request);

        // Convert request to dto
        TransactionDTO dto = transactionService.createTransaction(request);

        return dto;
    }


}
