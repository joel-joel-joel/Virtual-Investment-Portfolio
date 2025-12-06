/**
 * Stock Entity Service
 * Handles all stock-related API calls (CRUD operations)
 * All endpoints require JWT authentication
 */

import { apiFetch } from './api';
import {
  StockDTO,
  CreateStockRequest,
  UpdateStockRequest,
  FinnhubQuoteDTO,
  FinnhubCompanyProfileDTO,
} from '../types/api';

// ============================================================================
// Stock CRUD Operations (Require Authentication)
// ============================================================================

/**
 * Get all stocks from the portfolio tracker
 * @returns Array of stocks
 */
export const getAllStocks = async (): Promise<StockDTO[]> => {
  return apiFetch<StockDTO[]>('/api/stocks', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get a specific stock by its UUID
 * @param stockId - UUID of the stock
 * @returns Stock details
 */
export const getStockById = async (stockId: string): Promise<StockDTO> => {
  return apiFetch<StockDTO>(`/api/stocks/${stockId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get current price for a stock
 * Fetches from FinnHub in real-time, falls back to database value
 * @param stockId - UUID of the stock
 * @returns Current price as a number
 */
export const getStockPrice = async (stockId: string): Promise<number> => {
  return apiFetch<number>(`/api/stocks/${stockId}/price`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new stock in the portfolio tracker
 * @param stock - Stock creation data
 * @returns Created stock with generated UUID
 */
export const createStock = async (
  stock: CreateStockRequest
): Promise<StockDTO> => {
  return apiFetch<StockDTO>('/api/stocks', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(stock),
  });
};

/**
 * Update an existing stock
 * @param stockId - UUID of the stock to update
 * @param stock - Updated stock data
 * @returns Updated stock
 */
export const updateStock = async (
  stockId: string,
  stock: UpdateStockRequest
): Promise<StockDTO> => {
  return apiFetch<StockDTO>(`/api/stocks/${stockId}`, {
    method: 'PUT',
    requireAuth: true,
    body: JSON.stringify(stock),
  });
};

/**
 * Delete a stock from the portfolio tracker
 * @param stockId - UUID of the stock to delete
 * @returns void (204 No Content)
 */
export const deleteStock = async (stockId: string): Promise<void> => {
  return apiFetch<void>(`/api/stocks/${stockId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

// ============================================================================
// FinnHub Real-Time Data (No Authentication Required)
// ============================================================================

/**
 * Get real-time stock quote from FinnHub
 * No authentication required - public endpoint
 * @param symbol - Stock ticker symbol (e.g., "AAPL", "MSFT")
 * @returns Real-time quote data
 */
export const getStockQuote = async (
  symbol: string
): Promise<FinnhubQuoteDTO> => {
  return apiFetch<FinnhubQuoteDTO>(`/api/stocks/finnhub/quote/${symbol}`, {
    method: 'GET',
    requireAuth: false,
  });
};

/**
 * Get company profile from FinnHub
 * No authentication required - public endpoint
 * @param symbol - Stock ticker symbol (e.g., "AAPL", "MSFT")
 * @returns Company profile data
 */
export const getCompanyProfile = async (
  symbol: string
): Promise<FinnhubCompanyProfileDTO> => {
  return apiFetch<FinnhubCompanyProfileDTO>(
    `/api/stocks/finnhub/profile/${symbol}`,
    {
      method: 'GET',
      requireAuth: false,
    }
  );
};

// ============================================================================
// Batch Operations (Helper Functions)
// ============================================================================

/**
 * Get quotes for multiple stocks at once
 * @param symbols - Array of stock ticker symbols
 * @returns Array of quotes (matching order of symbols)
 */
export const getBatchStockQuotes = async (
  symbols: string[]
): Promise<FinnhubQuoteDTO[]> => {
  const promises = symbols.map((symbol) => getStockQuote(symbol));
  return Promise.all(promises);
};

/**
 * Get company profiles for multiple stocks at once
 * @param symbols - Array of stock ticker symbols
 * @returns Array of company profiles (matching order of symbols)
 */
export const getBatchCompanyProfiles = async (
  symbols: string[]
): Promise<FinnhubCompanyProfileDTO[]> => {
  const promises = symbols.map((symbol) => getCompanyProfile(symbol));
  return Promise.all(promises);
};

// ============================================================================
// Error Handling Wrappers (Optional - for specific error handling)
// ============================================================================

/**
 * Get stock quote with fallback handling
 * Returns null if the stock symbol is invalid or API fails
 * @param symbol - Stock ticker symbol
 * @returns Quote data or null on error
 */
export const getStockQuoteSafe = async (
  symbol: string
): Promise<FinnhubQuoteDTO | null> => {
  try {
    return await getStockQuote(symbol);
  } catch (error) {
    console.error(`Failed to fetch quote for ${symbol}:`, error);
    return null;
  }
};

/**
 * Get company profile with fallback handling
 * Returns null if the stock symbol is invalid or API fails
 * @param symbol - Stock ticker symbol
 * @returns Company profile or null on error
 */
export const getCompanyProfileSafe = async (
  symbol: string
): Promise<FinnhubCompanyProfileDTO | null> => {
  try {
    return await getCompanyProfile(symbol);
  } catch (error) {
    console.error(`Failed to fetch profile for ${symbol}:`, error);
    return null;
  }
};
