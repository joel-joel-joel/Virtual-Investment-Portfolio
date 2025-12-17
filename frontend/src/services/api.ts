/**
 * Base API Configuration
 * Provides the foundation for all API service calls
 */
import * as SecureStore from 'expo-secure-store';
import { getBackendUrl } from './configService';

// Base URL will be loaded dynamically at runtime
let cachedBaseUrl: string | null = null;

const TOKEN_KEY = 'user_token';

/**
 * Get JWT token from SecureStore
 */
const getAuthToken = async (): Promise<string | null> => {
  try {
    return await SecureStore.getItemAsync(TOKEN_KEY);
  } catch (error) {
    console.error('Failed to get auth token:', error);
    return null;
  }
};

/**
 * Store JWT token in SecureStore
 */
export const setAuthToken = async (token: string): Promise<void> => {
  try {
    await SecureStore.setItemAsync(TOKEN_KEY, token);
  } catch (error) {
    console.error('Failed to set auth token:', error);
    throw error;
  }
};

/**
 * Remove JWT token from SecureStore
 */
export const removeAuthToken = async (): Promise<void> => {
  try {
    await SecureStore.deleteItemAsync(TOKEN_KEY);
  } catch (error) {
    console.error('Failed to remove auth token:', error);
  }
};

/**
 * Base fetch wrapper with error handling
 */
interface FetchOptions extends RequestInit {
  requireAuth?: boolean;
  queryParams?: Record<string, any>;
  signal?: AbortSignal;
}

export const apiFetch = async <T>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> => {
  const { requireAuth = false, headers = {}, queryParams = {}, signal, ...restOptions } = options;

  const defaultHeaders: HeadersInit = {
    'Content-Type': 'application/json',
  };

  // Add auth token if required
  if (requireAuth) {
    const token = await getAuthToken();
    if (token) {
      defaultHeaders['Authorization'] = `Bearer ${token}`;
    } else {
      throw new Error('Authentication required but no token found');
    }
  }

  // Get backend URL dynamically (supports runtime override)
  if (!cachedBaseUrl) {
    cachedBaseUrl = await getBackendUrl();
  }

  // Build URL with query parameters if provided
  let url = `${cachedBaseUrl}${endpoint}`;
  if (Object.keys(queryParams).length > 0) {
    const queryString = buildQueryString(queryParams);
    url += queryString;
    console.log('📍 apiFetch: Query parameters added');
    console.log('  Final URL:', url);
  }

  console.log('📍 apiFetch request:');
  console.log('  Endpoint:', endpoint);
  console.log('  Full URL:', url);
  console.log('  Method:', restOptions.method || 'GET');

  try {
    const response = await fetch(url, {
      ...restOptions,
      signal,
      headers: {
        ...defaultHeaders,
        ...headers,
      },
    });

    console.log('  Response status:', response.status);
    console.log('  Response ok:', response.ok);

    // Handle different response statuses
    if (!response.ok) {
      const errorData = await response.text();
      console.error('  ❌ Response error:');
      console.error('    Status:', response.status);
      console.error('    Error body:', errorData);

      switch (response.status) {
        case 400:
          throw new Error(`Bad Request: ${errorData}`);
        case 401:
          throw new Error('Unauthorized: Invalid or missing authentication');
        case 404:
          throw new Error('Resource not found');
        case 500:
          throw new Error(`Server error: ${errorData}`);
        case 503:
          throw new Error('Service unavailable: External API temporarily unavailable');
        default:
          throw new Error(`Request failed with status ${response.status}`);
      }
    }

    // Handle 204 No Content
    if (response.status === 204) {
      console.log('  ✅ 204 No Content response');
      return undefined as T;
    }

    // Parse JSON response
    const data = await response.json();
    console.log('  ✅ Response parsed successfully');
    return data as T;
  } catch (error) {
    console.error('  ❌ apiFetch catch block error:', error);
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('Network error: Unable to connect to server');
  }
};

/**
 * Build query string from parameters
 */
export const buildQueryString = (params: Record<string, any>): string => {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      searchParams.append(key, String(value));
    }
  });

  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : '';
};

/**
 * Clear cached base URL to force reload on next request
 * Call this if backend URL changes at runtime
 */
export const clearBaseUrlCache = (): void => {
  cachedBaseUrl = null;
};
