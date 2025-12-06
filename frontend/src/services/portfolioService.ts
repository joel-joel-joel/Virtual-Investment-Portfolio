/**
 * Portfolio & Account Service
 * Handles portfolio, account, holdings, transactions, and watchlist operations
 * Backend uses Account-based architecture (User → Accounts → Holdings)
 */

import { apiFetch, buildQueryString } from './api';
import type {
  AccountDTO,
  CreateAccountRequest,
  UpdateAccountRequest,
  HoldingDTO,
  CreateHoldingRequest,
  UpdateHoldingRequest,
  TransactionDTO,
  CreateTransactionRequest,
  WatchlistDTO,
  AddToWatchlistRequest,
  PortfolioOverviewDTO,
} from '../types/api';

// ============================================================================
// Account Management (Multi-Account Support)
// ============================================================================

/**
 * Get all accounts for the authenticated user
 * @returns Array of accounts
 */
export const getAllAccounts = async (): Promise<AccountDTO[]> => {
  return apiFetch<AccountDTO[]>('/api/accounts', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get a specific account by ID
 * @param accountId - UUID of the account
 * @returns Account details
 */
export const getAccountById = async (accountId: string): Promise<AccountDTO> => {
  return apiFetch<AccountDTO>(`/api/accounts/${accountId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new account
 * @param account - Account creation data
 * @returns Created account
 */
export const createAccount = async (
    account: CreateAccountRequest
): Promise<AccountDTO> => {
    return apiFetch<AccountDTO>('/api/accounts', {
        method: 'POST',
        requireAuth: true,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(account),
    });
};


/**
 * Update an existing account
 * @param accountId - UUID of the account
 * @param account - Updated account data
 * @returns Updated account
 */
export const updateAccount = async (
  accountId: string,
  account: UpdateAccountRequest
): Promise<AccountDTO> => {
  return apiFetch<AccountDTO>(`/api/accounts/${accountId}`, {
    method: 'PUT',
    requireAuth: true,
    body: JSON.stringify(account),
  });
};

/**
 * Delete an account
 * @param accountId - UUID of the account
 * @returns void (204 No Content)
 */
export const deleteAccount = async (accountId: string): Promise<void> => {
  return apiFetch<void>(`/api/accounts/${accountId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

// ============================================================================
// Holdings Management
// ============================================================================

/**
 * Get all holdings for a specific account
 * @param accountId - UUID of the account
 * @returns Array of holdings
 */
export const getAccountHoldings = async (
  accountId: string
): Promise<HoldingDTO[]> => {
  return apiFetch<HoldingDTO[]>(`/api/accounts/${accountId}/holdings`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get all holdings (across all accounts)
 * @returns Array of holdings
 */
export const getAllHoldings = async (): Promise<HoldingDTO[]> => {
  return apiFetch<HoldingDTO[]>('/api/holdings', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get a specific holding by ID
 * @param holdingId - UUID of the holding
 * @returns Holding details
 */
export const getHoldingById = async (holdingId: string): Promise<HoldingDTO> => {
  return apiFetch<HoldingDTO>(`/api/holdings/${holdingId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new holding
 * @param holding - Holding creation data
 * @returns Created holding
 */
export const createHolding = async (
  holding: CreateHoldingRequest
): Promise<HoldingDTO> => {
  return apiFetch<HoldingDTO>('/api/holdings', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(holding),
  });
};

/**
 * Update an existing holding
 * @param holdingId - UUID of the holding
 * @param holding - Updated holding data
 * @returns Updated holding
 */
export const updateHolding = async (
  holdingId: string,
  holding: UpdateHoldingRequest
): Promise<HoldingDTO> => {
  return apiFetch<HoldingDTO>(`/api/holdings/${holdingId}`, {
    method: 'PUT',
    requireAuth: true,
    body: JSON.stringify(holding),
  });
};

/**
 * Delete a holding
 * @param holdingId - UUID of the holding
 * @returns void (204 No Content)
 */
export const deleteHolding = async (holdingId: string): Promise<void> => {
  return apiFetch<void>(`/api/holdings/${holdingId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

// ============================================================================
// Transaction Management
// ============================================================================

/**
 * Get all transactions for the authenticated user
 * @returns Array of transactions
 */
export const getAllTransactions = async (): Promise<TransactionDTO[]> => {
  return apiFetch<TransactionDTO[]>('/api/transactions', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get transactions for a specific account
 * @param accountId - UUID of the account
 * @returns Array of transactions
 */
export const getAccountTransactions = async (
  accountId: string
): Promise<TransactionDTO[]> => {
  return apiFetch<TransactionDTO[]>(`/api/accounts/${accountId}/transactions`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get a specific transaction by ID
 * @param transactionId - UUID of the transaction
 * @returns Transaction details
 */
export const getTransactionById = async (
  transactionId: string
): Promise<TransactionDTO> => {
  return apiFetch<TransactionDTO>(`/api/transactions/${transactionId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new transaction (buy or sell)
 * @param transaction - Transaction creation data
 * @returns Created transaction
 */
export const createTransaction = async (
  transaction: CreateTransactionRequest
): Promise<TransactionDTO> => {
  return apiFetch<TransactionDTO>('/api/transactions', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(transaction),
  });
};

/**
 * Delete a transaction
 * Note: Backend uses DELETE instead of cancel/status update
 * @param transactionId - UUID of the transaction
 * @returns void (204 No Content)
 */
export const deleteTransaction = async (
  transactionId: string
): Promise<void> => {
  return apiFetch<void>(`/api/transactions/${transactionId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

/**
 * Get transactions for a specific stock (client-side filtering)
 * Backend doesn't have /api/transactions/stock/{stockId} endpoint
 * @param stockId - UUID of the stock
 * @returns Array of transactions for the stock
 */
export const getTransactionsByStock = async (
  stockId: string
): Promise<TransactionDTO[]> => {
  const allTransactions = await getAllTransactions();
  return allTransactions.filter((t) => t.stockId === stockId);
};

// ============================================================================
// Watchlist Management
// ============================================================================

/**
 * Get user's watchlist
 * @returns Array of watchlist items
 */
export const getWatchlist = async (): Promise<WatchlistDTO[]> => {
  return apiFetch<WatchlistDTO[]>('/api/watchlist', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Add stock to watchlist
 * @param stockId - UUID of the stock
 * @returns Created watchlist item
 */
export const addToWatchlist = async (
  stockId: string
): Promise<WatchlistDTO> => {
  const request: AddToWatchlistRequest = { stockId };
  return apiFetch<WatchlistDTO>('/api/watchlist', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(request),
  });
};

/**
 * Remove stock from watchlist
 * @param stockId - UUID of the stock
 * @returns void (204 No Content)
 */
export const removeFromWatchlist = async (stockId: string): Promise<void> => {
  return apiFetch<void>(`/api/watchlist/${stockId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

/**
 * Check if stock is in watchlist
 * @param stockId - UUID of the stock
 * @returns True if in watchlist, false otherwise
 */
export const isInWatchlist = async (stockId: string): Promise<boolean> => {
  const response = await apiFetch<{ inWatchlist: boolean }>(
    `/api/watchlist/check/${stockId}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
  return response.inWatchlist;
};

// ============================================================================
// Portfolio Overview (Compatibility Layer)
// ============================================================================

/**
 * Get portfolio overview for user (aggregated across all accounts)
 * Maps to backend's user-level portfolio overview
 * @param userId - UUID of the user
 * @returns Portfolio overview
 */
export const getPortfolioOverview = async (
  userId: string
): Promise<PortfolioOverviewDTO> => {
  return apiFetch<PortfolioOverviewDTO>(`/portfolio/overview/user/${userId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get portfolio overview for specific account
 * @param accountId - UUID of the account
 * @returns Portfolio overview for the account
 */
export const getAccountOverview = async (
  accountId: string
): Promise<PortfolioOverviewDTO> => {
  return apiFetch<PortfolioOverviewDTO>(
    `/portfolio/overview/account/${accountId}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Calculate portfolio statistics from holdings
 */
export const calculatePortfolioStats = (holdings: HoldingDTO[]) => {
  const totalValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);
  const totalCost = holdings.reduce((sum, h) => sum + h.totalCostBasis, 0);
  const totalUnrealizedGain = holdings.reduce(
    (sum, h) => sum + h.unrealizedGain,
    0
  );
  const totalRealizedGain = holdings.reduce((sum, h) => sum + h.realizedGain, 0);
  const totalGain = totalUnrealizedGain + totalRealizedGain;
  const totalGainPercent = totalCost > 0 ? (totalGain / totalCost) * 100 : 0;

  return {
    totalValue,
    totalCost,
    totalUnrealizedGain,
    totalRealizedGain,
    totalGain,
    totalGainPercent,
    holdingsCount: holdings.length,
  };
};

/**
 * Group transactions by date
 */
export const groupTransactionsByDate = (transactions: TransactionDTO[]) => {
  return transactions.reduce((grouped, transaction) => {
    // Use transaction date if available, otherwise use today
    const date = new Date().toDateString();
    if (!grouped[date]) {
      grouped[date] = [];
    }
    grouped[date].push(transaction);
    return grouped;
  }, {} as Record<string, TransactionDTO[]>);
};

/**
 * Calculate total transaction value
 */
export const calculateTotalTransactionValue = (
  transactions: TransactionDTO[]
): number => {
  return transactions.reduce(
    (sum, t) => sum + t.shareQuantity * t.pricePerShare,
    0
  );
};

/**
 * Get holdings for a specific stock across all accounts
 */
export const getHoldingsByStock = async (
  stockId: string
): Promise<HoldingDTO[]> => {
  const allHoldings = await getAllHoldings();
  return allHoldings.filter((h) => h.stockId === stockId);
};

/**
 * Sort holdings by value (descending)
 */
export const sortHoldingsByValue = (
  holdings: HoldingDTO[]
): HoldingDTO[] => {
  return [...holdings].sort((a, b) => b.currentValue - a.currentValue);
};

/**
 * Sort holdings by gain percent (descending)
 */
export const sortHoldingsByGainPercent = (
  holdings: HoldingDTO[]
): HoldingDTO[] => {
  return [...holdings].sort(
    (a, b) => b.unrealizedGainPercent - a.unrealizedGainPercent
  );
};
