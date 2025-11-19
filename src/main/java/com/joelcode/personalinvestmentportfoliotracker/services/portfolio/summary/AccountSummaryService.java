package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.summary;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfolio.AccountSummaryDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface AccountSummaryService {

    AccountSummaryDTO getAccountSummary(UUID accountId);

    List<AccountSummaryDTO> getAccountSummariesForUser(UUID userId);

}
