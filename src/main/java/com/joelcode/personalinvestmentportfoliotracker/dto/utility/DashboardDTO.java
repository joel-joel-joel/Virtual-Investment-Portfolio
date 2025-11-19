package com.joelcode.personalinvestmentportfoliotracker.dto.utility;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioOverviewDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.PortfolioPerformanceDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;

import java.util.List;

public class DashboardDTO {

    private PortfolioOverviewDTO portfolioOverview;
    private PortfolioPerformanceDTO portfolioPerformance;
    private List<AllocationBreakdownDTO> allocations;
    private List<TransactionDTO> recentTransactions;

    public DashboardDTO() {}

    public DashboardDTO(PortfolioOverviewDTO portfolioOverview,
                        PortfolioPerformanceDTO portfolioPerformance,
                        List<AllocationBreakdownDTO> allocations,
                        List<TransactionDTO> recentTransactions) {
        this.portfolioOverview = portfolioOverview;
        this.portfolioPerformance = portfolioPerformance;
        this.allocations = allocations;
        this.recentTransactions = recentTransactions;
    }

    public PortfolioOverviewDTO getPortfolioOverview() {return portfolioOverview;}

    public void setPortfolioOverview(PortfolioOverviewDTO portfolioOverview) {this.portfolioOverview = portfolioOverview;}

    public PortfolioPerformanceDTO getPortfolioPerformance() {return portfolioPerformance;}

    public void setPortfolioPerformance(PortfolioPerformanceDTO portfolioPerformance) {this.portfolioPerformance = portfolioPerformance;}

    public List<AllocationBreakdownDTO> getAllocations() {return allocations;}

    public void setAllocations(List<AllocationBreakdownDTO> allocations) {this.allocations = allocations;}

    public List<TransactionDTO> getRecentTransactions() {return recentTransactions;}

    public void setRecentTransactions(List<TransactionDTO> recentTransactions) {this.recentTransactions = recentTransactions;}
}
