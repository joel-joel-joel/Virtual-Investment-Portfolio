package com.joelcode.personalinvestmentportfoliotracker.services.dividend;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.TransactionRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.account.AccountService;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DividendCalculationServiceImpl implements DividendCalculationService {

    // Define key fields
    private final TransactionRepository transactionRepository;
    private final DividendRepository dividendRepository;
    private final AccountService accountService;
    private final HoldingService holdingService;
    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;

    // Constructor
    public DividendCalculationServiceImpl(TransactionRepository transactionRepository, DividendRepository dividendRepository, AccountService accountService, HoldingService holdingService, AccountRepository accountRepository, HoldingRepository holdingRepository) {
        this.transactionRepository = transactionRepository;
        this.dividendRepository = dividendRepository;
        this.accountService = accountService;
        this.holdingService = holdingService;
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
    }

    // Calculating the total dividend for an account
    @Override
    public BigDecimal calculateTotalDividends(UUID accountId) {

        // Get all account transactions
        List<Transaction> transactions = transactionRepository.findByAccount_AccountId(accountId);

        // Map stockId → sharesOwned
        Map<UUID, BigDecimal> sharesOwnedMap = new HashMap<>();

        for (Transaction t : transactions) {

            UUID stockId = t.getStock().getStockId();
            sharesOwnedMap.putIfAbsent(stockId, BigDecimal.ZERO);

            // BUY adds shares
            if (t.getType().name().equalsIgnoreCase("BUY")) {
                sharesOwnedMap.put(stockId,
                        sharesOwnedMap.get(stockId).add(t.getShareQuantity()));
            }

            // SELL subtracts shares
            if (t.getType().name().equalsIgnoreCase("SELL")) {
                sharesOwnedMap.put(stockId,
                        sharesOwnedMap.get(stockId).subtract(t.getShareQuantity()));
            }
        }

        BigDecimal totalDividends = BigDecimal.ZERO;

        // For each stock owned, multiply dividends × shares owned
        for (UUID stockId : sharesOwnedMap.keySet()) {

            BigDecimal sharesOwned = sharesOwnedMap.get(stockId);

            // If the user owns no shares of this stock
            if (sharesOwned.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Pull all dividends for that stock
            List<Dividend> dividends = dividendRepository.findByStock_StockId(stockId);

            // Sum = dividendPerShare × sharesOwned
            for (Dividend dividend : dividends) {
                BigDecimal payout = dividend.getAmountPerShare().multiply(sharesOwned);
                totalDividends = totalDividends.add(payout);
            }
        }

        return totalDividends;
    }


    @Override
    @Transactional
    public void recalculateDividends(UUID accountId) {

        // Fetch account and holdings
        Account account = accountRepository.findByAccountId(accountId);
        List<Holding> holdings = holdingRepository.getHoldingsEntitiesByAccount(accountId);

        // Iterate holdings and update/create dividends
        for (Holding h : holdings) {
            BigDecimal dividendPerShare = h.getStock().getDividendPerShare(); // assume stock has dividend info
            BigDecimal totalDividend = dividendPerShare.multiply(h.getQuantity());

            // Find existing dividend
            Dividend dividend = dividendRepository.findByAccountIdAndStockId(accountId, h.getStock().getStockId())
                    .orElseGet(() -> {
                        Dividend newDiv = new Dividend();
                        newDiv.setStock(h.getStock());
                        return newDiv;
                    });

            // Update dividend value
            dividend.setAmountPerShare(totalDividend);

            dividendRepository.save(dividend);
        }
    }
}
