package com.joelcode.personalinvestmentportfoliotracker.services.earnings;

import com.joelcode.personalinvestmentportfoliotracker.dto.earnings.EarningsDTO;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for fetching and processing earnings data
 * Handles getting upcoming earnings for users and their accounts
 */
public interface EarningsService {

    /**
     * Get upcoming earnings for all accounts belonging to a user
     * @param userId UUID of the user
     * @return List of earnings DTOs for all user accounts
     */
    List<EarningsDTO> getUpcomingEarningsForUser(UUID userId);

    /**
     * Get upcoming earnings for a specific account
     * @param accountId UUID of the account
     * @param userId UUID of the user (for authorization)
     * @return List of earnings DTOs for the account
     */
    List<EarningsDTO> getUpcomingEarningsForAccount(UUID accountId, UUID userId);
}
