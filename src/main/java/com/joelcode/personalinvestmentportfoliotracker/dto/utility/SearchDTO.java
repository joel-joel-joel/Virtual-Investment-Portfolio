package com.joelcode.personalinvestmentportfoliotracker.dto.utility;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.account.AccountDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;

import java.util.List;

public class SearchDTO {

    private List<StockDTO> stocks;
    private List<AccountDTO> accounts;
    private List<HoldingDTO> holdings;

    public SearchDTO() {}

    public SearchDTO(List<StockDTO> stocks, List<AccountDTO> accounts, List<HoldingDTO> holdings) {
        this.stocks = stocks;
        this.accounts = accounts;
        this.holdings = holdings;
    }

    public List<StockDTO> getStocks() {return stocks;}

    public void setStocks(List<StockDTO> stocks) {this.stocks = stocks;}

    public List<AccountDTO> getAccounts() {return accounts;}

    public void setAccounts(List<AccountDTO> accounts) {this.accounts = accounts;}

    public List<HoldingDTO> getHoldings() {return holdings;}

    public void setHoldings(List<HoldingDTO> holdings) {this.holdings = holdings;}
}
