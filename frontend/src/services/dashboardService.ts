/**
 * Dashboard Service
 * Handles dashboard-specific data aggregation and analytics
 *
 * NOTE: This is a placeholder implementation.
 * Your backend does not currently expose dashboard/analytics endpoints.
 * See "Missing Endpoints" section for required backend implementations.
 */

import { apiFetch } from './api';
import { PortfolioHolding } from './portfolioService';

// ============================================================================
// Type Definitions
// ============================================================================

export interface DashboardSummary {
  totalValue: number;
  totalGain: number;
  totalGainPercent: number;
  dayChange: number;
  dayChangePercent: number;
  topGainers: PortfolioHolding[];
  topLosers: PortfolioHolding[];
  sectorAllocation: SectorAllocation[];
  recentActivity: RecentActivity[];
}

export interface SectorAllocation {
  sector: string;
  value: number;
  percentage: number;
  change: number;
  changePercent: number;
}

export interface RecentActivity {
  activityId: string;
  type: 'BUY' | 'SELL' | 'DIVIDEND' | 'PRICE_ALERT';
  stockCode: string;
  companyName: string;
  description: string;
  timestamp: string;
  amount?: number;
}

export interface PerformanceData {
  date: string;
  portfolioValue: number;
  gain: number;
  gainPercent: number;
}

export interface EarningsData {
  stockCode: string;
  companyName: string;
  earningsDate: string;
  estimatedEPS: number;
  actualEPS?: number;
  reportTime: 'BMO' | 'AMC'; // Before Market Open / After Market Close
}

// ============================================================================
// Dashboard Data Endpoints (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Get dashboard summary with key metrics
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/summary
 * Headers: Authorization: Bearer <token>
 * Response: DashboardSummary
 */
export const getDashboardSummary = async (): Promise<DashboardSummary> => {
  return apiFetch<DashboardSummary>('/api/dashboard/summary', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get sector allocation breakdown
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/sectors
 * Headers: Authorization: Bearer <token>
 * Response: Array of SectorAllocation
 */
export const getSectorAllocation = async (): Promise<SectorAllocation[]> => {
  return apiFetch<SectorAllocation[]>('/api/dashboard/sectors', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get recent portfolio activity
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/activity
 * Headers: Authorization: Bearer <token>
 * Query Params: limit?
 * Response: Array of RecentActivity
 */
export const getRecentActivity = async (
  limit: number = 10
): Promise<RecentActivity[]> => {
  return apiFetch<RecentActivity[]>(`/api/dashboard/activity?limit=${limit}`, {
    method: 'GET',
    requireAuth: true,
  });
};

// ============================================================================
// Performance & Analytics (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Get portfolio performance over time
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/performance
 * Headers: Authorization: Bearer <token>
 * Query Params: period? (1D, 1W, 1M, 3M, 1Y, ALL)
 * Response: Array of PerformanceData
 */
export const getPortfolioPerformance = async (
  period: '1D' | '1W' | '1M' | '3M' | '1Y' | 'ALL' = '1M'
): Promise<PerformanceData[]> => {
  return apiFetch<PerformanceData[]>(
    `/api/dashboard/performance?period=${period}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

/**
 * Get top gainers in portfolio
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/top-gainers
 * Headers: Authorization: Bearer <token>
 * Query Params: limit?
 * Response: Array of PortfolioHolding
 */
export const getTopGainers = async (
  limit: number = 5
): Promise<PortfolioHolding[]> => {
  return apiFetch<PortfolioHolding[]>(
    `/api/dashboard/top-gainers?limit=${limit}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

/**
 * Get top losers in portfolio
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/top-losers
 * Headers: Authorization: Bearer <token>
 * Query Params: limit?
 * Response: Array of PortfolioHolding
 */
export const getTopLosers = async (
  limit: number = 5
): Promise<PortfolioHolding[]> => {
  return apiFetch<PortfolioHolding[]>(
    `/api/dashboard/top-losers?limit=${limit}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

// ============================================================================
// Earnings Calendar (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Get upcoming earnings for portfolio stocks
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/earnings
 * Headers: Authorization: Bearer <token>
 * Query Params: days? (number of days ahead to look)
 * Response: Array of EarningsData
 */
export const getUpcomingEarnings = async (
  days: number = 7
): Promise<EarningsData[]> => {
  return apiFetch<EarningsData[]>(`/api/dashboard/earnings?days=${days}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get earnings for all stocks (not just portfolio)
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/dashboard/earnings/all
 * Query Params: days?, sector?
 * Response: Array of EarningsData
 */
export const getAllUpcomingEarnings = async (params?: {
  days?: number;
  sector?: string;
}): Promise<EarningsData[]> => {
  const queryParams = new URLSearchParams();

  if (params?.days) queryParams.append('days', String(params.days));
  if (params?.sector) queryParams.append('sector', params.sector);

  const queryString = queryParams.toString();
  const endpoint = queryString
    ? `/api/dashboard/earnings/all?${queryString}`
    : '/api/dashboard/earnings/all';

  return apiFetch<EarningsData[]>(endpoint, {
    method: 'GET',
    requireAuth: false,
  });
};

// ============================================================================
// Alerts & Notifications (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

export interface PriceAlert {
  alertId: string;
  stockId: string;
  stockCode: string;
  type: 'ABOVE' | 'BELOW';
  targetPrice: number;
  currentPrice: number;
  isActive: boolean;
  createdAt: string;
  triggeredAt?: string;
}

/**
 * Get user's price alerts
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/alerts
 * Headers: Authorization: Bearer <token>
 * Response: Array of PriceAlert
 */
export const getPriceAlerts = async (): Promise<PriceAlert[]> => {
  return apiFetch<PriceAlert[]>('/api/alerts', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new price alert
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/alerts
 * Headers: Authorization: Bearer <token>
 * Body: { stockId: string, type: 'ABOVE' | 'BELOW', targetPrice: number }
 * Response: PriceAlert
 */
export const createPriceAlert = async (
  stockId: string,
  type: 'ABOVE' | 'BELOW',
  targetPrice: number
): Promise<PriceAlert> => {
  return apiFetch<PriceAlert>('/api/alerts', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify({ stockId, type, targetPrice }),
  });
};

/**
 * Delete a price alert
 *
 * BACKEND ENDPOINT MISSING:
 * DELETE /api/alerts/{alertId}
 * Headers: Authorization: Bearer <token>
 * Response: void (204 No Content)
 */
export const deletePriceAlert = async (alertId: string): Promise<void> => {
  return apiFetch<void>(`/api/alerts/${alertId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Calculate sector allocation from holdings
 * Client-side helper for when backend endpoint isn't available
 */
export const calculateSectorAllocationFromHoldings = (
  holdings: PortfolioHolding[]
): SectorAllocation[] => {
  const totalValue = holdings.reduce((sum, h) => sum + h.totalValue, 0);

  // Group by sector (you'd need sector data in holdings)
  const sectorMap = new Map<string, { value: number; gain: number }>();

  holdings.forEach((holding) => {
    // NOTE: This assumes holdings have a 'sector' property
    // You may need to fetch this from company profile
    const sector = 'Unknown'; // Placeholder

    const current = sectorMap.get(sector) || { value: 0, gain: 0 };
    sectorMap.set(sector, {
      value: current.value + holding.totalValue,
      gain: current.gain + holding.totalGain,
    });
  });

  return Array.from(sectorMap.entries()).map(([sector, data]) => ({
    sector,
    value: data.value,
    percentage: (data.value / totalValue) * 100,
    change: data.gain,
    changePercent: data.value > 0 ? (data.gain / (data.value - data.gain)) * 100 : 0,
  }));
};

/**
 * Sort holdings by performance
 */
export const sortHoldingsByPerformance = (
  holdings: PortfolioHolding[],
  direction: 'gainers' | 'losers' = 'gainers'
): PortfolioHolding[] => {
  return [...holdings].sort((a, b) => {
    if (direction === 'gainers') {
      return b.totalGainPercent - a.totalGainPercent;
    } else {
      return a.totalGainPercent - b.totalGainPercent;
    }
  });
};
