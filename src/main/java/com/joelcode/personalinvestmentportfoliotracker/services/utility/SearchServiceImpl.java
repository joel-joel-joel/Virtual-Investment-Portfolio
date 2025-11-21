package com.joelcode.personalinvestmentportfoliotracker.services.utility;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.SearchDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.StockMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.AccountMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class SearchServiceImpl implements SearchService {

    // Define key fields
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private HoldingMapper holdingMapper;

    @Autowired
    private PriceHistoryServiceImpl priceHistoryService;


    // Interface function

    // Search across stocks, accounts and holdings
    @Override
    public SearchDTO search(String query, UUID userId) {

        // Search Stocks by company name (partial, case-insensitive)
        List<StockDTO> stocks = stockRepository.findByCompanyNameContainingIgnoreCase(query)
                .stream()
                .map(StockMapper::toDTO)
                .collect(Collectors.toList());

        // Search Accounts
        List<Account> accountEntities;
        if (userId != null) {
            accountEntities = accountRepository.findByUser_UserIdAndAccountNameContainingIgnoreCase(userId, query);
        } else {
            accountEntities = accountRepository.findByAccountNameContainingIgnoreCase(query);
        }

        List<AccountDTO> accounts = accountEntities.stream()
                .map(AccountMapper::toDTO)
                .collect(Collectors.toList());

        // Search Holdings
        List<Holding> holdingEntities;
        if (userId != null) {
            holdingEntities = holdingRepository.findByAccount_User_UserIdAndStock_CompanyNameContainingIgnoreCase(userId, query);
        } else {
            holdingEntities = holdingRepository.findByStock_CompanyNameContainingIgnoreCase(query);
        }

        List<HoldingDTO> holdings = holdingEntities.stream()
                .map(h -> HoldingMapper.toDTO(h, priceHistoryService.getCurrentPrice(h.getStock().getStockId())))
                .collect(Collectors.toList());

        return new SearchDTO(stocks, accounts, holdings);
    }
}