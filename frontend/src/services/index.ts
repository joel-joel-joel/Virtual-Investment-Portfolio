/**
 * Services Index
 * Central export point for all API services
 */

// Base API utilities
export { setAuthToken, removeAuthToken, apiFetch } from './api';

// Stock/Entity services (✅ Backend implemented)
export * from './entityService';

// News services (✅ Backend implemented)
export * from './newsService';

// Authentication services (✅ Backend implemented)
export {
  login,
  register,
  logout,
  getCurrentUser,
  refreshToken,
  verifyToken,
  forgotPassword,
  resetPassword,
  changePassword,
  type UserProfile
} from './authService';

// Portfolio services (❌ Backend NOT implemented yet)
export * from './portfolioService';

// Dashboard services (❌ Backend NOT implemented yet)
export * from './dashboardService';

// Price History services (✅ Backend implemented)
export {
  getPriceHistoryForStock,
  getLatestPriceForStock,
  getAllPriceHistory,
  createPriceHistory,
  deletePriceHistory,
  filterPriceHistoryByTimeRange
} from './priceHistoryService';

// Type definitions (avoiding duplicate exports from authService)
export type {
  StockDTO,
  CreateStockRequest,
  UpdateStockRequest,
  FinnhubQuoteDTO,
  FinnhubCompanyProfileDTO,
  NewsArticleDTO,
  UserDTO,
  AccountDTO,
  CreateAccountRequest,
  UpdateAccountRequest,
  HoldingDTO,
  CreateHoldingRequest,
  UpdateHoldingRequest,
  TransactionDTO,
  CreateTransactionRequest,
  TransactionType,
  PortfolioOverviewDTO,
  PortfolioPerformanceDTO,
  AllocationBreakdownDTO,
  DashboardDTO,
  WatchlistDTO,
  AddToWatchlistRequest,
  PriceAlertDTO,
  CreatePriceAlertRequest,
  AlertType,
  EarningsDTO,
  PriceHistoryDTO,
  PriceHistoryCreateRequest,
  APIErrorResponse,
  ApiResponse,
  PaginationMetadata,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  ChangePasswordRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest
} from '../types/api';
