/**
 * Authentication Service
 * Handles user authentication and JWT token management
 *
 * NOTE: This is a placeholder implementation.
 * Your backend does not currently expose auth endpoints.
 * See "Missing Endpoints" section for required backend implementations.
 */

import { apiFetch, setAuthToken, removeAuthToken } from './api';
import type { LoginRequest, RegisterRequest, AuthResponse, UserDTO } from '../types/api';

// ============================================================================
// Type Definitions
// ============================================================================

export interface UserProfile extends UserDTO {
  // Extended from UserDTO which includes: userId, username, email, fullName, createdAt
}

// ============================================================================
// Authentication Functions (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Login user and store JWT token
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/auth/login
 * Body: { email: string, password: string }
 * Response: { token: string, userId: string, email: string }
 *
 * @param credentials - User login credentials
 * @returns Authentication response with JWT token
 */
export const login = async (
  credentials: LoginRequest
): Promise<AuthResponse> => {
  try {
    const response = await apiFetch<any>('/api/auth/login', {
      method: 'POST',
      requireAuth: false,
      body: JSON.stringify(credentials),
    });

    // Extract token from response (LoginResponseDTO has 'token' field)
    if (!response.token) {
      throw new Error('No token received from server');
    }

    // Store the JWT token
    await setAuthToken(response.token);

    // Return response in AuthResponse format
    return {
      token: response.token,
      userId: response.userId || '',
      email: response.email || credentials.email,
    };
  } catch (error) {
    // Pass through the actual error message from backend
    if (error instanceof Error) {
      throw error;
    }
    throw new Error('Login failed: Unable to connect to server');
  }
};

/**
 * Register new user
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/auth/register
 * Body: { email: string, password: string, firstName?: string, lastName?: string }
 * Response: { token: string, userId: string, email: string }
 *
 * @param userData - User registration data
 * @returns Authentication response with JWT token
 */
export const register = async (
    userData: RegisterRequest
): Promise<AuthResponse> => {
    try {
        const response = await apiFetch<any>('/api/auth/register', {
            method: 'POST',
            requireAuth: false,
            body: JSON.stringify(userData),
        });

        // Extract token from response
        if (!response.token) {
            throw new Error('No token received from server');
        }

        // Store the JWT token
        await setAuthToken(response.token);

        // Return response in AuthResponse format
        return {
            token: response.token,
            userId: response.userId || '',
            email: response.email || userData.email,
        };
    } catch (error) {
        // Re-throw the actual error message from the backend
        if (error instanceof Error) {
            throw error;
        }
        throw new Error('Registration failed: Unable to connect to server');
    }
};


/**
 * Logout user and clear JWT token
 *
 * OPTIONAL BACKEND ENDPOINT:
 * POST /api/auth/logout
 * (Token invalidation on server side - recommended for security)
 */
export const logout = async (): Promise<void> => {
  try {
    // Optionally call backend to invalidate token
    // await apiFetch<void>('/api/auth/logout', {
    //   method: 'POST',
    //   requireAuth: true,
    // });
  } catch (error) {
    console.error('Logout API call failed:', error);
    // Continue with local logout even if API fails
  } finally {
    // Always remove token locally
    await removeAuthToken();
  }
};

/**
 * Get current user profile
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/auth/me
 * Headers: Authorization: Bearer <token>
 * Response: { userId: string, email: string, firstName?: string, lastName?: string }
 *
 * @returns Current user profile
 */
export const getCurrentUser = async (): Promise<UserProfile> => {
  return apiFetch<UserProfile>('/api/auth/me', {
    method: 'GET',
    requireAuth: true,
  });
};

/**
 * Refresh JWT token
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/auth/refresh
 * Headers: Authorization: Bearer <old_token>
 * Response: { token: string }
 *
 * @returns New authentication response with refreshed token
 */
export const refreshToken = async (): Promise<AuthResponse> => {
  try {
    const response = await apiFetch<AuthResponse>('/api/auth/refresh', {
      method: 'POST',
      requireAuth: true,
    });

    // Store the new JWT token
    await setAuthToken(response.token);

    return response;
  } catch (error) {
    throw new Error('Token refresh failed');
  }
};

/**
 * Verify if current JWT token is valid
 *
 * BACKEND ENDPOINT MISSING:
 * GET /api/auth/verify
 * Headers: Authorization: Bearer <token>
 * Response: { valid: boolean }
 *
 * @returns True if token is valid, false otherwise
 */
export const verifyToken = async (): Promise<boolean> => {
  try {
    const response = await apiFetch<{ valid: boolean }>('/api/auth/verify', {
      method: 'GET',
      requireAuth: true,
    });

    return response.valid;
  } catch (error) {
    return false;
  }
};

// ============================================================================
// Password Management (NOT IMPLEMENTED IN BACKEND YET)
// ============================================================================

/**
 * Request password reset email
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/auth/forgot-password
 * Body: { email: string }
 * Response: { message: string }
 */
export const forgotPassword = async (email: string): Promise<void> => {
  await apiFetch<{ message: string }>('/api/auth/forgot-password', {
    method: 'POST',
    requireAuth: false,
    body: JSON.stringify({ email }),
  });
};

/**
 * Reset password with token from email
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/auth/reset-password
 * Body: { token: string, newPassword: string }
 * Response: { message: string }
 */
export const resetPassword = async (
  token: string,
  newPassword: string
): Promise<void> => {
  await apiFetch<{ message: string }>('/api/auth/reset-password', {
    method: 'POST',
    requireAuth: false,
    body: JSON.stringify({ token, newPassword }),
  });
};

/**
 * Change password for authenticated user
 *
 * BACKEND ENDPOINT MISSING:
 * POST /api/auth/change-password
 * Headers: Authorization: Bearer <token>
 * Body: { currentPassword: string, newPassword: string }
 * Response: { message: string }
 */
export const changePassword = async (
  currentPassword: string,
  newPassword: string
): Promise<void> => {
  await apiFetch<{ message: string }>('/api/auth/change-password', {
    method: 'POST',
    requireAuth: true,
    body: JSON.stringify({ currentPassword, newPassword }),
  });
};
