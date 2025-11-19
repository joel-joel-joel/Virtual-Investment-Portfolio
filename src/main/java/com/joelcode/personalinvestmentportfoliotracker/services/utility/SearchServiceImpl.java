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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

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

    @Override
    public SearchDTO search(String query, UUID userId) {

        // Search Stocks
        List<StockDTO> stocks = stockRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(StockMapper::toDTO)
                .collect(Collectors.toList());

        // Search Accounts
        List<Account> accountEntities;
        if (userId != null) {
            accountEntities = accountRepository.findByUser_UserIdAndNameContainingIgnoreCase(userId, query);
        } else {
            accountEntities = accountRepository.findByNameContainingIgnoreCase(query);
        }

        List<AccountDTO> accounts = accountEntities.stream()
                .map(AccountMapper::toDTO)
                .collect(Collectors.toList());

        // Search Holdings
        List<Holding> holdingEntities;
        if (userId != null) {
            holdingEntities = holdingRepository.findByAccount_User_UserIdAndStock_NameContainingIgnoreCase(userId, query);
        } else {
            holdingEntities = holdingRepository.findByStock_NameContainingIgnoreCase(query);
        }

        List<HoldingDTO> holdings = holdingEntities.stream()
                .map(h -> HoldingMapper.toDTO(h, priceHistoryService.getCurrentPrice(h.getStock().getStockId())))
                .collect(Collectors.toList());



        return new SearchDTO(stocks, accounts, holdings);
    }
}
