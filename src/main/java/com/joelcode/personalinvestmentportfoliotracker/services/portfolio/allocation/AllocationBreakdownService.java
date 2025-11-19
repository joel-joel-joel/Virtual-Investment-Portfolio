package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.allocation;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AllocationBreakdownDTO;

import java.util.List;
import java.util.UUID;

public interface AllocationBreakdownService {
    List<AllocationBreakdownDTO> getAllocationForAccount(UUID accountId);
}

