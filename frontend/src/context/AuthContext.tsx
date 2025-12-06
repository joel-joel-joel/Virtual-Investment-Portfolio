import React, { createContext, useState, useContext, useEffect } from 'react';
import * as SecureStore from 'expo-secure-store';
import type { UserDTO, AccountDTO } from '../types/api';
import { getCurrentUser } from '../services/authService';
import { getAllAccounts } from '../services/portfolioService';

const TOKEN_KEY = 'user_token';
const ACTIVE_ACCOUNT_KEY = 'active_account_id';

interface AuthContextType {
  user: UserDTO | null;
  accounts: AccountDTO[];
  activeAccount: AccountDTO | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: UserDTO | null) => void;
  setAccounts: (accounts: AccountDTO[]) => void;
  setActiveAccount: (account: AccountDTO | null) => void;
  login: (token: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccounts: () => Promise<void>;
  switchAccount: (accountId: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<UserDTO | null>(null);
  const [accounts, setAccounts] = useState<AccountDTO[]>([]);
  const [activeAccount, setActiveAccount] = useState<AccountDTO | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = user !== null;

  // Auto-login on app start - check SecureStore for existing token
  useEffect(() => {
    checkStoredToken();
  }, []);

  // Save active account ID when it changes
  useEffect(() => {
    if (activeAccount) {
      SecureStore.setItemAsync(ACTIVE_ACCOUNT_KEY, activeAccount.accountId).catch(
        (error) => console.error('Failed to save active account ID:', error)
      );
    }
  }, [activeAccount]);

  /**
   * Check if a token exists in SecureStore and auto-login
   */
  const checkStoredToken = async () => {
    try {
      setIsLoading(true);
      const storedToken = await SecureStore.getItemAsync(TOKEN_KEY);

      if (storedToken) {
        // Token exists, attempt to auto-login
        await loginWithToken(storedToken);
      }
    } catch (error) {
      console.error('Failed to check stored token:', error);
      // Clear potentially corrupted token
      await SecureStore.deleteItemAsync(TOKEN_KEY).catch(() => {});
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Login with token - used for both auto-login and manual login
   */
  const loginWithToken = async (token: string) => {
    try {
      // Token is already stored in SecureStore by the login function
      // or passed directly from backend login response

      // Get current user using the token
      const currentUser = await getCurrentUser();
      setUser(currentUser);

      // Get user's accounts
      const userAccounts = await getAllAccounts();
      setAccounts(userAccounts);

      // Set active account (from storage or first account)
      const savedAccountId = await SecureStore.getItemAsync(ACTIVE_ACCOUNT_KEY);
      if (savedAccountId) {
        const savedAccount = userAccounts.find(
          (acc) => acc.accountId === savedAccountId
        );
        setActiveAccount(savedAccount || userAccounts[0] || null);
      } else {
        setActiveAccount(userAccounts[0] || null);
      }
    } catch (error) {
      console.error('Failed to login with token:', error);
      // Clear invalid token
      await SecureStore.deleteItemAsync(TOKEN_KEY).catch(() => {});
      throw error;
    }
  };

  /**
   * Login - called after successful authentication
   * Stores token in SecureStore and loads user data
   */
  const login = async (token: string) => {
    try {
      // Store token in SecureStore
      await SecureStore.setItemAsync(TOKEN_KEY, token);

      // Login with the token (fetch user data)
      await loginWithToken(token);
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  /**
   * Logout - clears all auth data and removes token
   */
  const logout = async () => {
    try {
      setUser(null);
      setAccounts([]);
      setActiveAccount(null);

      // Remove token from SecureStore
      await SecureStore.deleteItemAsync(TOKEN_KEY);
      await SecureStore.deleteItemAsync(ACTIVE_ACCOUNT_KEY);
    } catch (error) {
      console.error('Logout failed:', error);
      // Force clear state even if SecureStore fails
      setUser(null);
      setAccounts([]);
      setActiveAccount(null);
    }
  };

  /**
   * Refresh accounts - reload user's accounts from backend
   */
  const refreshAccounts = async () => {
    try {
      const userAccounts = await getAllAccounts();
      setAccounts(userAccounts);

      // If no active account, set first account
      if (!activeAccount && userAccounts.length > 0) {
        setActiveAccount(userAccounts[0]);
      }

      // If active account was deleted, switch to first available
      if (
        activeAccount &&
        !userAccounts.find((acc) => acc.accountId === activeAccount.accountId)
      ) {
        setActiveAccount(userAccounts[0] || null);
      }
    } catch (error) {
      console.error('Failed to refresh accounts:', error);
    }
  };

  /**
   * Switch active account
   */
  const switchAccount = (accountId: string) => {
    const account = accounts.find((acc) => acc.accountId === accountId);
    if (account) {
      setActiveAccount(account);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accounts,
        activeAccount,
        isAuthenticated,
        isLoading,
        setUser,
        setAccounts,
        setActiveAccount,
        login,
        logout,
        refreshAccounts,
        switchAccount,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

/**
 * Helper function to get stored token
 * Useful for API calls
 */
export const getStoredToken = async (): Promise<string | null> => {
  try {
    return await SecureStore.getItemAsync(TOKEN_KEY);
  } catch (error) {
    console.error('Failed to get stored token:', error);
    return null;
  }
};
