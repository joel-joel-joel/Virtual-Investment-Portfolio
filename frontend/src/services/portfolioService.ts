/**
 * Portfolio Service
 * Handles user portfolio and transaction operations
 *
 * NOTE: This is a placeholder implementation.
 * Your backend does not currently expose portfolio/transaction endpoints.
 * See "Missing Endpoints" section for required backend implementations.
 */

import { apiFetch } from './api';

// ============================================================================
// Type Definitions
// ============================================================================

export interface Portfolio {
  portfolioId: string;
  userId: string;
  totalValue: number;
  totalGain: number;
  totalGainPercent: number;
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioHolding {
  holdingId: string;
  portfolioId: string;
  stockId: string;
  stockCode: string;
  companyName: string;
  shares: number;
  averageCost: number;
  currentPrice: number;
  totalValue: number;
  totalCost: number;
  totalGain: number;
  totalGainPercent: number;
  updatedAt: string;
}

export interface Transaction {
  transactionId: string;
  userId: string;
  portfolioId: string;
  stockId: string;
  stockCode: string;
  companyName: string;
  type: 'BUY' | 'SELL';
  shares: number;
  pricePerShare: number;
  totalAmount: number;
  fees: number;
  netAmount: number;
  orderType: 'MARKET' | 'LIMIT';
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  executedAt?: string;
}

export interface CreateTransactionRequest {
  stockId: string;
  type: 'BUY' | 'SELL';
  shares: number;
  pricePerShare: number;
  orderType: 'MARKET' | 'LIMIT';
}

export interface WatchlistItem {
  watchlistId: string;
  userId: string;
  stockId: string;
  stockCode: string;
  companyName: string;
  currentPrice: number;
  priceChange: number;
  priceChangePercent: number;
  addedAt: string;
}

// ============================================================================
// Portfolio Management (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Get user's portfolio summary
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/portfolio
 * Headers: Authorization: Bearer <token>
 * Response: Portfolio object
 */
export const getPortfolio = async (): Promise<Portfolio> => {
  return apiFetch<Portfolio>('/api/portfolio', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get all holdings in user's portfolio
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/portfolio/holdings
 * Headers: Authorization: Bearer <token>
 * Response: Array of PortfolioHolding
 */
export const getPortfolioHoldings = async (): Promise<PortfolioHolding[]> => {
  return apiFetch<PortfolioHolding[]>('/api/portfolio/holdings', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get a specific holding by stock ID
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/portfolio/holdings/{stockId}
 * Headers: Authorization: Bearer <token>
 * Response: PortfolioHolding
 */
export const getHoldingByStockId = async (
  stockId: string
): Promise<PortfolioHolding> => {
  return apiFetch<PortfolioHolding>(`/api/portfolio/holdings/${stockId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

// ============================================================================
// Transaction Management (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Get all transactions for the user
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/transactions
 * Headers: Authorization: Bearer <token>
 * Query Params: limit?, offset?, stockId?, type?, startDate?, endDate?
 * Response: Array of Transaction
 */
export const getAllTransactions = async (params?: {
  limit?: number;
  offset?: number;
  stockId?: string;
  type?: 'BUY' | 'SELL';
  startDate?: string;
  endDate?: string;
}): Promise<Transaction[]> => {
  const queryParams = new URLSearchParams();

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        queryParams.append(key, String(value));
      }
    });
  }

  const queryString = queryParams.toString();
  const endpoint = queryString ? `/api/transactions?${queryString}` : '/api/transactions';

  return apiFetch<Transaction[]>(endpoint, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get transactions for a specific stock
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/transactions/stock/{stockId}
 * Headers: Authorization: Bearer <token>
 * Response: Array of Transaction
 */
export const getTransactionsByStock = async (
  stockId: string
): Promise<Transaction[]> => {
  return apiFetch<Transaction[]>(`/api/transactions/stock/${stockId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new transaction (buy or sell)
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/transactions
 * Headers: Authorization: Bearer <token>
 * Body: CreateTransactionRequest
 * Response: Transaction
 */
export const createTransaction = async (
  transaction: CreateTransactionRequest
): Promise<Transaction> => {
  return apiFetch<Transaction>('/api/transactions', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(transaction),
  });
};

/**
 * Get a specific transaction by ID
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/transactions/{transactionId}
 * Headers: Authorization: Bearer <token>
 * Response: Transaction
 */
export const getTransactionById = async (
  transactionId: string
): Promise<Transaction> => {
  return apiFetch<Transaction>(`/api/transactions/${transactionId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Cancel a pending transaction
 *
 * BACKEND ENDPOINT MISSING:
 * PUT /api/transactions/{transactionId}/cancel
 * Headers: Authorization: Bearer <token>
 * Response: Transaction
 */
export const cancelTransaction = async (
  transactionId: string
): Promise<Transaction> => {
  return apiFetch<Transaction>(`/api/transactions/${transactionId}/cancel`, {
    method: 'PUT',
    requireAuth: true,
  });
};

// ============================================================================
// Watchlist Management (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Get user's watchlist
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/watchlist
 * Headers: Authorization: Bearer <token>
 * Response: Array of WatchlistItem
 */
export const getWatchlist = async (): Promise<WatchlistItem[]> => {
  return apiFetch<WatchlistItem[]>('/api/watchlist', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Add stock to watchlist
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/watchlist
 * Headers: Authorization: Bearer <token>
 * Body: { stockId: string }
 * Response: WatchlistItem
 */
export const addToWatchlist = async (stockId: string): Promise<WatchlistItem> => {
  return apiFetch<WatchlistItem>('/api/watchlist', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify({ stockId }),
  });
};

/**
 * Remove stock from watchlist
 *
 * BACKEND ENDPOINT MISSING:
 * DELETE /api/watchlist/{stockId}
 * Headers: Authorization: Bearer <token>
 * Response: void (204 No Content)
 */
export const removeFromWatchlist = async (stockId: string): Promise<void> => {
  return apiFetch<void>(`/api/watchlist/${stockId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

/**
 * Check if stock is in watchlist
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/watchlist/check/{stockId}
 * Headers: Authorization: Bearer <token>
 * Response: { inWatchlist: boolean }
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
// Helper Functions
// ============================================================================

/**
 * Calculate portfolio statistics from holdings
 */
export const calculatePortfolioStats = (holdings: PortfolioHolding[]) => {
  const totalValue = holdings.reduce((sum, holding) => sum + holding.totalValue, 0);
  const totalCost = holdings.reduce((sum, holding) => sum + holding.totalCost, 0);
  const totalGain = totalValue - totalCost;
  const totalGainPercent = totalCost > 0 ? (totalGain / totalCost) * 100 : 0;

  return {
    totalValue,
    totalCost,
    totalGain,
    totalGainPercent,
    holdingsCount: holdings.length,
  };
};

/**
 * Group transactions by date
 */
export const groupTransactionsByDate = (transactions: Transaction[]) => {
  return transactions.reduce((grouped, transaction) => {
    const date = new Date(transaction.createdAt).toDateString();
    if (!grouped[date]) {
      grouped[date] = [];
    }
    grouped[date].push(transaction);
    return grouped;
  }, {} as Record<string, Transaction[]>);
};

/**
 * Calculate total fees from transactions
 */
export const calculateTotalFees = (transactions: Transaction[]): number => {
  return transactions.reduce((sum, transaction) => sum + transaction.fees, 0);
};
