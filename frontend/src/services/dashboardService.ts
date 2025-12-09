/**
 * Dashboard Service
 * Handles dashboard, analytics, price alerts, and earnings data
 * Updated to match actual backend endpoints
 */

import { apiFetch } from './api';
import type {
  DashboardDTO,
  PortfolioOverviewDTO,
  PortfolioPerformanceDTO,
  AllocationBreakdownDTO,
  PriceAlertDTO,
  CreatePriceAlertRequest,
  EarningsDTO,
} from '../types/api';

// ============================================================================
// Dashboard Data (User & Account Level)
// ============================================================================

/**
 * Get complete dashboard data for user (aggregated across all accounts)
 * Includes overview, performance, allocations, and recent transactions
 * @param userId - UUID of the user
 * @returns Complete dashboard data
 */
export const getUserDashboard = async (
  userId: string
): Promise<DashboardDTO> => {
  return apiFetch<DashboardDTO>(`/api/dashboard/user/${userId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get complete dashboard data for a specific account
 * @param accountId - UUID of the account
 * @returns Complete dashboard data for the account
 */
export const getAccountDashboard = async (
  accountId: string
): Promise<DashboardDTO> => {
  return apiFetch<DashboardDTO>(`/api/dashboard/account/${accountId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

// ============================================================================
// Portfolio Overview (Detailed)
// ============================================================================

/**
 * Get detailed portfolio overview for user
 * @param userId - UUID of the user
 * @returns Portfolio overview with holdings
 */
export const getUserPortfolioOverview = async (
  userId: string
): Promise<PortfolioOverviewDTO> => {
  return apiFetch<PortfolioOverviewDTO>(`/portfolio/overview/user/${userId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get detailed portfolio overview for account
 * @param accountId - UUID of the account
 * @returns Portfolio overview for the account
 */
export const getAccountPortfolioOverview = async (
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
// Portfolio Performance
// ============================================================================

/**
 * Get performance metrics for user (all accounts aggregated)
 * @param userId - UUID of the user
 * @returns Performance metrics
 */
export const getUserPerformance = async (
  userId: string
): Promise<PortfolioPerformanceDTO> => {
  return apiFetch<PortfolioPerformanceDTO>(
    `/portfolio/performance/user/${userId}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

/**
 * Get performance metrics for specific account
 * @param accountId - UUID of the account
 * @returns Performance metrics for the account
 */
export const getAccountPerformance = async (
  accountId: string
): Promise<PortfolioPerformanceDTO> => {
  return apiFetch<PortfolioPerformanceDTO>(
    `/portfolio/performance/account/${accountId}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

// ============================================================================
// Allocation Breakdown
// ============================================================================

/**
 * Get allocation breakdown for user (all accounts)
 * @param userId - UUID of the user
 * @returns Allocation breakdown by stock
 */
export const getUserAllocation = async (
  userId: string
): Promise<AllocationBreakdownDTO[]> => {
  return apiFetch<AllocationBreakdownDTO[]>(`/allocation/user/${userId}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Get allocation breakdown for specific account
 * @param accountId - UUID of the account
 * @returns Allocation breakdown for the account
 */
export const getAccountAllocation = async (
  accountId: string
): Promise<AllocationBreakdownDTO[]> => {
  return apiFetch<AllocationBreakdownDTO[]>(
    `/allocation/account/${accountId}`,
    {
      method: 'GET',
      requireAuth: true,
    }
  );
};

// ============================================================================
// Price Alerts
// ============================================================================

/**
 * Get all price alerts for the authenticated user
 * @param activeOnly - Optional filter to get only active alerts
 * @returns Array of price alerts
 */
export const getPriceAlerts = async (
  activeOnly?: boolean
): Promise<PriceAlertDTO[]> => {
  const queryParam = activeOnly !== undefined ? `?active=${activeOnly}` : '';
  return apiFetch<PriceAlertDTO[]>(`/api/price-alerts${queryParam}`, {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Create a new price alert
 * @param alert - Price alert creation data
 * @returns Created price alert
 */
export const createPriceAlert = async (
  alert: CreatePriceAlertRequest
): Promise<PriceAlertDTO> => {
  return apiFetch<PriceAlertDTO>('/api/price-alerts', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify(alert),
  });
};

/**
 * Delete a price alert
 * @param alertId - UUID of the alert
 * @returns void (204 No Content)
 */
export const deletePriceAlert = async (alertId: string): Promise<void> => {
  return apiFetch<void>(`/api/price-alerts/${alertId}`, {
    method: 'DELETE',
    requireAuth: true,
  });
};

// ============================================================================
// Earnings Calendar
// ============================================================================

/**
 * Get upcoming earnings for all stocks
 * Backend returns earnings for next 90 days
 * @returns Array of upcoming earnings
 */
export const getUpcomingEarnings = async (): Promise<EarningsDTO[]> => {
  return apiFetch<EarningsDTO[]>('/api/earnings/upcoming', {
    method: 'GET',
    requireAuth: false, // Public endpoint
  });
};

/**
 * Get earnings for a specific stock
 * @param stockId - UUID of the stock
 * @returns Array of earnings for the stock
 */
export const getStockEarnings = async (
  stockId: string
): Promise<EarningsDTO[]> => {
  return apiFetch<EarningsDTO[]>(`/api/earnings/by-stock/${stockId}`, {
    method: 'GET',
    requireAuth: false, // Public endpoint
  });
};

// ============================================================================
// Backward Compatibility Aliases
// ============================================================================

/**
 * Get dashboard summary (alias for getUserDashboard)
 * For backward compatibility with original service design
 * @param userId - UUID of the user
 * @returns Complete dashboard data
 */
export const getDashboardSummary = getUserDashboard;

/**
 * Get sector allocation (alias for getUserAllocation)
 * For backward compatibility
 * @param userId - UUID of the user
 * @returns Allocation breakdown
 */
export const getSectorAllocation = getUserAllocation;

/**
 * Get portfolio performance (alias for getUserPerformance)
 * For backward compatibility
 * @param userId - UUID of the user
 * @returns Performance metrics
 */
export const getPortfolioPerformance = getUserPerformance;

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Calculate total portfolio value from overview
 */
export const getTotalValue = (overview: PortfolioOverviewDTO): number => {
  return overview.totalPortfolioValue + overview.cashBalance;
};

/**
 * Calculate total gain (realized + unrealized)
 */
export const getTotalGain = (overview: PortfolioOverviewDTO): number => {
  return overview.totalRealizedGain + overview.totalUnrealizedGain;
};

/**
 * Calculate total gain percentage
 */
export const getTotalGainPercent = (overview: PortfolioOverviewDTO): number => {
  if (overview.totalCostBasis === 0) return 0;
  const totalGain = getTotalGain(overview);
  return (totalGain / overview.totalCostBasis) * 100;
};

/**
 * Get top holdings by value
 */
export const getTopHoldingsByValue = (
  overview: PortfolioOverviewDTO,
  limit: number = 5
) => {
  return [...overview.holdings]
    .sort((a, b) => b.currentValue - a.currentValue)
    .slice(0, limit);
};

/**
 * Get top gainers (by unrealized gain percent)
 */
export const getTopGainers = (
  overview: PortfolioOverviewDTO,
  limit: number = 5
) => {
  return [...overview.holdings]
    .sort((a, b) => b.unrealizedGainPercent - a.unrealizedGainPercent)
    .slice(0, limit);
};

/**
 * Get top losers (by unrealized gain percent)
 */
export const getTopLosers = (
  overview: PortfolioOverviewDTO,
  limit: number = 5
) => {
  return [...overview.holdings]
    .sort((a, b) => a.unrealizedGainPercent - b.unrealizedGainPercent)
    .slice(0, limit);
};

/**
 * Filter active price alerts
 */
export const getActivePriceAlerts = (alerts: PriceAlertDTO[]): PriceAlertDTO[] => {
  return alerts.filter((alert) => alert.isActive);
};

/**
 * Filter triggered price alerts
 */
export const getTriggeredPriceAlerts = (
  alerts: PriceAlertDTO[]
): PriceAlertDTO[] => {
  return alerts.filter((alert) => alert.triggeredAt !== undefined);
};

/**
 * Group price alerts by stock
 */
export const groupAlertsByStock = (
  alerts: PriceAlertDTO[]
): Record<string, PriceAlertDTO[]> => {
  return alerts.reduce((grouped, alert) => {
    const stockId = alert.stockId;
    if (!grouped[stockId]) {
      grouped[stockId] = [];
    }
    grouped[stockId].push(alert);
    return grouped;
  }, {} as Record<string, PriceAlertDTO[]>);
};

/**
 * Filter earnings by date range
 */
export const filterEarningsByDateRange = (
  earnings: EarningsDTO[],
  startDate: Date,
  endDate: Date
): EarningsDTO[] => {
  return earnings.filter((earning) => {
    const earningDate = new Date(earning.earningsDate);
    return earningDate >= startDate && earningDate <= endDate;
  });
};

/**
 * Get earnings for next N days
 */
export const getEarningsForNextDays = async (
  days: number = 7
): Promise<EarningsDTO[]> => {
  const allEarnings = await getUpcomingEarnings();
  const today = new Date();
  const futureDate = new Date();
  futureDate.setDate(today.getDate() + days);

  return filterEarningsByDateRange(allEarnings, today, futureDate);
};

/**
 * Sort earnings by date (ascending - soonest first)
 */
export const sortEarningsByDate = (
  earnings: EarningsDTO[]
): EarningsDTO[] => {
  return [...earnings].sort((a, b) => {
    const dateA = new Date(a.earningsDate).getTime();
    const dateB = new Date(b.earningsDate).getTime();
    return dateA - dateB;
  });
};

/**
 * Group earnings by date
 */
export const groupEarningsByDate = (
  earnings: EarningsDTO[]
): Record<string, EarningsDTO[]> => {
  return earnings.reduce((grouped, earning) => {
    const date = earning.earningsDate;
    if (!grouped[date]) {
      grouped[date] = [];
    }
    grouped[date].push(earning);
    return grouped;
  }, {} as Record<string, EarningsDTO[]>);
};
