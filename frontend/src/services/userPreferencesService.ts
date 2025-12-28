import { apiFetch } from './api';

export interface NotificationPreferences {
  priceAlerts: boolean;
  portfolioUpdates: boolean;
  marketNews: boolean;
  dividendNotifications: boolean;
  earningSeason: boolean;
}

export interface UserPreferencesUpdateRequest {
  // All fields optional for partial updates
  priceAlerts?: boolean;
  portfolioUpdates?: boolean;
  marketNews?: boolean;
  dividendNotifications?: boolean;
  earningSeason?: boolean;
}

/**
 * Get user notification preferences
 * GET /api/users/me/preferences
 */
export const getUserPreferences = async (): Promise<NotificationPreferences> => {
  return apiFetch<NotificationPreferences>('/api/users/me/preferences', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Update user notification preferences
 * PUT /api/users/me/preferences
 */
export const updateUserPreferences = async (
  preferences: UserPreferencesUpdateRequest
): Promise<NotificationPreferences> => {
  return apiFetch<NotificationPreferences>('/api/users/me/preferences', {
    method: 'PUT',
    requireAuth: true,
    body: JSON.stringify(preferences),
  });
};
