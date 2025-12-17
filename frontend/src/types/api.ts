/**
 * API Response Type Definitions
 * Based on actual backend DTOs
 */

// ============================================================================
// Stock Types
// ============================================================================

export interface StockDTO {
  stockId: string;           // UUID
  stockCode: string;         // e.g., "AAPL"
  companyName: string;       // e.g., "Apple Inc."
  stockValue: number;        // Decimal value, e.g., 195.50
  industry?: string;         // e.g., "Technology"
}

export interface CreateStockRequest {
  stockCode: string;         // Stock ticker symbol (must be unique)
  companyName: string;       // Full company name
  stockValue?: number;       // Optional initial stock value (default: 0.00)
}

export interface UpdateStockRequest {
  stockCode: string;         // Stock ticker symbol
  companyName: string;       // Full company name
  stockValue?: number;       // Optional updated stock value
}

// ============================================================================
// FinnHub Types (Real-Time Stock Data)
// ============================================================================

export interface FinnhubQuoteDTO {
  c: number;   // current price
  h: number;   // high price
  l: number;   // low price
  o: number;   // open price
  pc: number;  // previous close price
  t: number;   // timestamp (Unix)

  // Convenience getters with full names
  currentPrice?: number;
  highPrice?: number;
  lowPrice?: number;
  openPrice?: number;
  previousClosePrice?: number;
  timestamp?: number;
}

export interface FinnhubCompanyProfileDTO {
  ticker: string;                 // e.g., "AAPL"
  name: string;                   // e.g., "Apple Inc." (maps from Finnhub's "name" field)
  companyName?: string;           // Alias for name
  finnhubIndustry: string;        // e.g., "Technology"
  industry?: string;              // Alias for finnhubIndustry
  marketCapitalization: number;   // e.g., 3000000 (in millions)
  marketCap?: number;             // Alias for marketCapitalization
  logo: string;                   // URL to logo
  country: string;                // e.g., "US"
  currency: string;               // e.g., "USD"
  phone: string;                  // e.g., "14089961010"
  weburl: string;                 // e.g., "https://www.apple.com"
  website?: string;               // Alias for weburl
  description?: string;           // Company description
}

export interface FinnhubMetricsDTO {
  metric: {
    // Price Performance
    '52WeekHigh': number;
    '52WeekLow': number;
    '52WeekHighDate': string;
    '52WeekLowDate': string;
    '52WeekPriceReturnDaily': number;

    // Fundamental Metrics
    peExclExtraTTM: number;           // P/E Ratio
    pbQuarterly: number;              // Price-to-Book
    psAnnual: number;                 // Price-to-Sales
    epsExclExtraItemsTTM: number;     // EPS
    dividendYieldIndicatedAnnual: number;
    bookValuePerShareQuarterly: number;
    revenuePerShareTTM: number;

    // Market Metrics
    marketCapitalization: number;
    beta: number;                     // Volatility measure
    '10DayAverageTradingVolume': number;
    currentRatioQuarterly: number;

    // Profitability Metrics
    roaeTTM: number;                  // Return on Average Equity
    roiTTM: number;                   // Return on Investment
    grossMarginTTM: number;
    netMarginTTM: number;
  };
  series?: any;
}

export interface FinnhubCandleDTO {
  c: number[];  // close prices
  h: number[];  // high prices
  l: number[];  // low prices
  o: number[];  // open prices
  t: number[];  // timestamps (Unix)
  v: number[];  // volumes
  s: string;    // status ("ok" or "no_data")
}

export interface FinnhubSearchResultDTO {
  description: string;   // Company name (e.g., "Apple Inc")
  displaySymbol: string; // Display ticker symbol (e.g., "AAPL")
  symbol: string;        // Stock symbol (e.g., "AAPL")
  type: string;          // Security type (e.g., "Common Stock")
}

export interface FinnhubSearchResponseDTO {
  result: FinnhubSearchResultDTO[];  // Array of search results
  count: number;                     // Number of results returned
}

// ============================================================================
// News Types
// ============================================================================

export interface NewsArticleDTO {
  sector: string;            // Raw Finnhub sector/industry (e.g., "Technology", "Financial Services")
  title: string;             // Article headline
  summary: string;           // Article description/summary
  url: string;               // Full URL to article
  publishedAt: string;       // ISO 8601 timestamp, e.g., "2025-12-06T10:30:00Z"
  imageUrl?: string;         // Optional image URL for the article
}

// ============================================================================
// Account Types (Backend uses Account instead of Portfolio)
// ============================================================================

export interface UserDTO {
    userId: string;
    username: string;
    email: string;
    fullName: string;
    createdAt: string; // ISO timestamp
}


export interface AccountDTO {
  accountId: string;         // UUID
  accountName: string;
  user: UserDTO;
  cashBalance: number;       // BigDecimal
}

export interface CreateAccountRequest {
  accountName: string;
  cashBalance?: number;
}

export interface UpdateAccountRequest {
  accountName: string;
  cashBalance?: number;
}

// ============================================================================
// Holding Types
// ============================================================================

export interface HoldingDTO {
  holdingId: string;              // UUID
  accountId: string;              // UUID
  stockId: string;                // UUID
  stockSymbol: string;            // e.g., "AAPL"
  quantity: number;               // BigDecimal
  averageCostBasis: number;       // BigDecimal
  totalCostBasis: number;         // BigDecimal
  realizedGain: number;           // BigDecimal
  firstPurchaseDate: string;      // ISO 8601 timestamp
  currentPrice: number;           // BigDecimal
  currentValue: number;           // BigDecimal
  unrealizedGain: number;         // BigDecimal
  unrealizedGainPercent: number;
  companyName: string;
  sector: string;                // e.g., "Technology", "Healthcare"
}

export interface CreateHoldingRequest {
  accountId: string;
  stockId: string;
  quantity: number;
  averageCostBasis: number;
}

export interface UpdateHoldingRequest {
  quantity: number;
  averageCostBasis: number;
}

// ============================================================================
// Transaction Types
// ============================================================================

export type TransactionType = 'BUY' | 'SELL' | 'BUY_LIMIT' | 'SELL_LIMIT';

export interface TransactionDTO {
  transactionId: string;     // UUID
  stockId: string;           // UUID
  accountId: string;         // UUID
  shareQuantity: number;     // BigDecimal
  pricePerShare: number;     // BigDecimal
  transactionType: TransactionType;
  transactionDate?: string;  // ISO 8601 timestamp (from backend)
  createdAt?: string;        // ISO 8601 timestamp (fallback)
}

export interface CreateTransactionRequest {
  stockId: string;           // UUID
  accountId: string;         // UUID
  shareQuantity: number;
  pricePerShare: number;
  transactionType: TransactionType;
}

// ============================================================================
// Order Types (Limit Orders)
// ============================================================================

export type OrderType = 'BUY_LIMIT' | 'SELL_LIMIT';

export type OrderStatus = 'PENDING' | 'EXECUTED' | 'CANCELLED' | 'FAILED';

export interface OrderDTO {
  orderId: string;               // UUID
  stockId: string;               // UUID
  stockSymbol: string;           // e.g., "AAPL"
  accountId: string;             // UUID
  orderType: OrderType;
  quantity: number;              // BigDecimal
  limitPrice: number;            // BigDecimal
  status: OrderStatus;
  createdAt: string;             // ISO 8601 timestamp
  executedAt?: string;           // ISO 8601 timestamp (optional)
  cancelledAt?: string;          // ISO 8601 timestamp (optional)
  failureReason?: string;        // Optional failure reason
}

export interface CreateOrderRequest {
  stockId: string;               // UUID
  accountId: string;             // UUID
  quantity: number;
  limitPrice: number;
  orderType: OrderType;
}

// ============================================================================
// Portfolio/Dashboard Types
// ============================================================================

export interface PortfolioOverviewDTO {
  userId: string;                 // UUID
  accountId: string;              // UUID (null for user-level aggregation)
  totalPortfolioValue: number;    // BigDecimal - Total value including cash and holdings
  holdingsValue: number;          // BigDecimal - Value of holdings only (without cash)
  totalCostBasis: number;         // BigDecimal
  totalUnrealizedGain: number;    // BigDecimal
  totalRealizedGain: number;      // BigDecimal
  totalDividends: number;         // BigDecimal
  cashBalance: number;            // BigDecimal - Wallet balance
  holdings: HoldingDTO[];
}

export interface PortfolioPerformanceDTO {
  userId: string;                 // UUID
  accountId: string | null;       // UUID (null for user-level)
  totalPortfolioValue: number;    // BigDecimal
  totalCostBasis: number;         // BigDecimal
  totalRealizedGain: number;      // BigDecimal
  totalUnrealizedGain: number;    // BigDecimal
  totalDividends: number;         // BigDecimal
  cashBalance: number;            // BigDecimal
  roiPercentage: number;          // BigDecimal
  dailyGain: number;              // BigDecimal
  monthlyGain: number;            // BigDecimal
}

export interface AllocationBreakdownDTO {
  stockCode: string;
  percentage: number;             // BigDecimal
  currentValue: number;           // BigDecimal
}

export interface DashboardDTO {
  portfolioOverview: PortfolioOverviewDTO;
  portfolioPerformance: PortfolioPerformanceDTO;
  allocations: AllocationBreakdownDTO[];
  recentTransactions: TransactionDTO[];
}

// ============================================================================
// Watchlist Types
// ============================================================================

export interface WatchlistDTO {
    watchlistId: string;
    userId: string;
    stockId: string;
    stockCode: string;
    companyName: string;
    currentPrice: number | null;
    priceChange: number | null;  // ✅ Backend uses "priceChange"
    priceChangePercent: number | null;  // ✅ Backend uses "priceChangePercent"
    addedAt: string;
    sector?: string;  // ✅ Added sector field
}

export interface AddToWatchlistRequest {
    stockId: string;
}

export interface WatchlistCheckResponse {
    inWatchlist: boolean;
}

// Internal component types
export interface WatchlistStock {
    watchlistId: string;
    userId: string;
    stockId: string;
    symbol: string;
    name: string;
    price: number;
    change: number;
    changePercent: number;
    addedAt: string;
}

export type WatchlistSortField = 'symbol' | 'price' | 'changePercent';
export type SortOrder = 'asc' | 'desc';

export interface WatchlistSortOption {
    field: WatchlistSortField;
    order: SortOrder;
}

// ============================================================================
// Price Alert Types
// ============================================================================

export type AlertType = 'ABOVE' | 'BELOW';

export interface PriceAlertDTO {
  alertId: string;           // UUID
  userId: string;            // UUID
  stockId: string;           // UUID
  stockSymbol: string;
  alertType: AlertType;
  targetPrice: number;       // BigDecimal
  isActive: boolean;
  createdAt: string;         // ISO 8601 timestamp
  triggeredAt?: string;      // ISO 8601 timestamp (optional)
}

export interface CreatePriceAlertRequest {
  stockId: string;
  alertType: AlertType;
  targetPrice: number;
}

// ============================================================================
// Earnings Types
// ============================================================================

export interface EarningsDTO {
  earningsId: string;        // UUID
  stockId: string;           // UUID
  stockSymbol: string;
  earningsDate: string;      // ISO 8601 date
  estimatedEps?: number;     // BigDecimal (optional)
  actualEps?: number;        // BigDecimal (optional)
}

// ============================================================================
// Price History Types
// ============================================================================

export interface PriceHistoryDTO {
  priceHistoryId: string;    // UUID
  closeDate: string;         // ISO 8601 timestamp
  closePrice: number;        // BigDecimal
  stockId: string;           // UUID
}

export interface PriceHistoryCreateRequest {
  stockId: string;
  closeDate: string;
  closePrice: number;
}

// ============================================================================
// Portfolio Snapshot Types
// ============================================================================

export interface PortfolioSnapshotDTO {
  snapshotId: string;            // UUID
  accountId: string;             // UUID
  snapshotDate: string;          // ISO 8601 date (e.g., "2025-12-12")
  totalValue: number;            // BigDecimal - Total portfolio value (cash + holdings)
  cashBalance: number;           // BigDecimal
  totalCostBasis: number;        // BigDecimal
  totalGain: number;             // BigDecimal
  realizedGain: number;          // BigDecimal
  unrealizedGain: number;        // BigDecimal
  totalDividends: number;        // BigDecimal
  roiPercentage: number;         // BigDecimal
  dayChange: number;             // BigDecimal
  dayChangePercent: number;      // BigDecimal
  createdAt: string;             // ISO 8601 timestamp
  updatedAt: string;             // ISO 8601 timestamp
}

export interface CreatePortfolioSnapshotRequest {
  accountId: string;
  snapshotDate: string;
  totalValue: number;
  cashBalance: number;
  totalCostBasis: number;
  totalGain: number;
  dayChange: number;
}

// ============================================================================
// Error Types
// ============================================================================

export interface APIErrorResponse {
  error: string;             // Error message description
  status: number;            // HTTP status code
  timestamp: string;         // ISO 8601 timestamp
}

// ============================================================================
// Generic API Response Wrapper
// ============================================================================

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  statusCode: number;
  errors?: string[];
  pagination?: PaginationMetadata;
}

export interface PaginationMetadata {
  page: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

// ============================================================================
// Authentication Types
// ============================================================================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  fullName: string;
}

export interface AuthResponse {
  token: string;             // JWT token
  userId: string;            // UUID
  email: string;
  expiresAt?: string;        // ISO 8601 timestamp
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}
