/**
 * Earnings Service
 * Handles earnings-related API calls
 */

import { apiFetch } from './api';

export interface EarningsBackendDTO {
    earningsId: string;
    stockId: string;
    stockCode: string;
    companyName: string;
    earningsDate: string;      // ISO date
    estimatedEPS?: number;     // BigDecimal (optional)
    actualEPS?: number;        // BigDecimal (optional)
    reportTime?: string;       // e.g., "Before Market", "After Market", "16:00"
}

/**
 * Get upcoming earnings for the next 90 days
 * @returns Array of upcoming earnings
 */
export const getUpcomingEarnings = async (): Promise<EarningsBackendDTO[]> => {
    console.log('🎯 [earningService] getUpcomingEarnings called');
    try {
        console.log('📡 [earningService] Calling API: GET /api/earnings/upcoming');
        const result = await apiFetch<EarningsBackendDTO[]>('/api/earnings/upcoming', {
            method: 'GET',
            requireAuth: true,
        });
        console.log('✅ [earningService] API response received');
        console.log('📊 [earningService] Earnings count:', result?.length || 0);
        if (result && result.length > 0) {
            console.log('📋 [earningService] First earnings:', result[0]);
            console.log('📋 [earningService] Last earnings:', result[result.length - 1]);
        } else {
            console.warn('⚠️ [earningService] No earnings data received');
        }
        return result || [];
    } catch (error: any) {
        console.error('❌ [earningService] Error fetching upcoming earnings:', error);
        console.error('   Error message:', error?.message);
        console.error('   Error details:', error);
        throw error;
    }
};

/**
 * Get earnings for a specific stock
 * @param stockId - UUID of the stock
 * @returns Array of earnings for the stock
 */
export const getEarningsByStock = async (stockId: string): Promise<EarningsBackendDTO[]> => {
    console.log('🎯 [earningService] getEarningsByStock called with stockId:', stockId);
    try {
        console.log(`📡 [earningService] Calling API: GET /api/earnings/by-stock/${stockId}`);
        const result = await apiFetch<EarningsBackendDTO[]>(`/api/earnings/by-stock/${stockId}`, {
            method: 'GET',
            requireAuth: true,
        });
        console.log('✅ [earningService] API response received');
        console.log('📊 [earningService] Earnings count for stock:', result?.length || 0);
        if (result && result.length > 0) {
            console.log('📋 [earningService] Earnings data:', result);
        } else {
            console.warn('⚠️ [earningService] No earnings data received for this stock');
        }
        return result || [];
    } catch (error: any) {
        console.error(`❌ [earningService] Error fetching earnings for stock ${stockId}:`, error);
        console.error('   Error message:', error?.message);
        throw error;
    }
};