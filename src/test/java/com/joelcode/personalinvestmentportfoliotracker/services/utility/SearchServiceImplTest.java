package com.joelcode.personalinvestmentportfoliotracker.services.utility;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.SearchDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.AccountRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.HoldingRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.StockMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.AccountMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private PriceHistoryServiceImpl priceHistoryService;

    @InjectMocks
    private SearchServiceImpl searchService;

    private UUID userId;
    private Stock stock;
    private Account account;
    private Holding holding;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();

        stock = new Stock();
        stock.setStockId(UUID.randomUUID());
        stock.setCompanyName("Apple");

        account = new Account();
        account.setAccountId(UUID.randomUUID());
        account.setAccountName("Cash Wallet");

        holding = new Holding();
        holding.setHoldingId(UUID.randomUUID());
        holding.setStock(stock);
        holding.setAccount(account);
    }

    @Test
    void testSearch_ReturnsCorrectResults() {
        String query = "app";

        // Mock stock search
        when(stockRepository.findByNameContainingIgnoreCase(query))
                .thenReturn(List.of(stock));

        // Mock account search
        when(accountRepository.findByUser_UserIdAndNameContainingIgnoreCase(userId, query))
                .thenReturn(List.of(account));

        // Mock holdings search
        when(holdingRepository.findByAccount_User_UserIdAndStock_NameContainingIgnoreCase(userId, query))
                .thenReturn(List.of(holding));

        // Mock price
        when(priceHistoryService.getCurrentPrice(stock.getStockId()))
                .thenReturn(BigDecimal.TEN);

        // Mock static mappers
        StockDTO stockDTO = new StockDTO();
        AccountDTO accountDTO = new AccountDTO();
        HoldingDTO holdingDTO = new HoldingDTO();

        try (MockedStatic<StockMapper> mockedStockMapper = Mockito.mockStatic(StockMapper.class);
             MockedStatic<AccountMapper> mockedAccountMapper = Mockito.mockStatic(AccountMapper.class);
             MockedStatic<HoldingMapper> mockedHoldingMapper = Mockito.mockStatic(HoldingMapper.class)) {

            mockedStockMapper.when(() -> StockMapper.toDTO(stock)).thenReturn(stockDTO);
            mockedAccountMapper.when(() -> AccountMapper.toDTO(account)).thenReturn(accountDTO);
            mockedHoldingMapper.when(() -> HoldingMapper.toDTO(holding, BigDecimal.TEN))
                    .thenReturn(holdingDTO);

            // Execute
            SearchDTO result = searchService.search(query, userId);

            // Assertions
            assertNotNull(result);
            assertEquals(1, result.getStocks().size());
            assertEquals(1, result.getAccounts().size());
            assertEquals(1, result.getHoldings().size());
        }
    }
}
