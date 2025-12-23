import { API_BASE_URL } from './api';
import { getStoredToken } from '../context/AuthContext';

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

export const getUserPreferences = async (): Promise<NotificationPreferences> => {
  const token = await getStoredToken();
  const response = await fetch(`${API_BASE_URL}/users/me/preferences`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error('Failed to fetch user preferences');
  }

  return await response.json();
};

export const updateUserPreferences = async (
  preferences: UserPreferencesUpdateRequest
): Promise<NotificationPreferences> => {
  const token = await getStoredToken();
  const response = await fetch(`${API_BASE_URL}/users/me/preferences`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(preferences),
  });

  if (!response.ok) {
    throw new Error('Failed to update user preferences');
  }

  return await response.json();
};
