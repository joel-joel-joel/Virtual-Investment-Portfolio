/**
 * Configuration Service
 * Manages backend URL and other app configuration
 * Allows runtime override for flexibility during development
 */

import * as SecureStore from 'expo-secure-store';

const CONFIG_KEY = 'app_backend_url';
const DEFAULT_BACKEND_URL = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://Joels-MacBook-Pro.local:8080';

/**
 * Get the current backend URL
 * First checks SecureStore for user override, then falls back to env variable
 */
export const getBackendUrl = async (): Promise<string> => {
  try {
    // Check if user has manually set a custom URL
    const customUrl = await SecureStore.getItemAsync(CONFIG_KEY);
    if (customUrl) {
      return customUrl;
    }
  } catch (error) {
    console.warn('Failed to get custom backend URL from storage:', error);
  }

  // Fall back to environment variable
  return DEFAULT_BACKEND_URL;
};

/**
 * Set a custom backend URL (persists to SecureStore)
 * Used when user needs to override the default
 */
export const setBackendUrl = async (url: string): Promise<void> => {
  try {
    // Validate URL format
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      throw new Error('URL must start with http:// or https://');
    }

    await SecureStore.setItemAsync(CONFIG_KEY, url);
  } catch (error) {
    console.error('Failed to set backend URL:', error);
    throw error;
  }
};

/**
 * Reset backend URL to default (removes custom override)
 */
export const resetBackendUrl = async (): Promise<void> => {
  try {
    await SecureStore.deleteItemAsync(CONFIG_KEY);
  } catch (error) {
    console.error('Failed to reset backend URL:', error);
  }
};

/**
 * Get the default backend URL (for display purposes)
 */
export const getDefaultBackendUrl = (): string => {
  return DEFAULT_BACKEND_URL;
};

/**
 * Check if a custom URL has been set
 */
export const hasCustomBackendUrl = async (): Promise<boolean> => {
  try {
    const customUrl = await SecureStore.getItemAsync(CONFIG_KEY);
    return !!customUrl;
  } catch (error) {
    return false;
  }
};
