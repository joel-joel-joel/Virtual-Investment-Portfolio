/**
 * API Response Type Definitions
 * Based on backend API documentation
 */

// ============================================================================
// Stock Types
// ============================================================================

export interface StockDTO {
  stockId: string;           // UUID
  stockCode: string;         // e.g., "AAPL"
  companyName: string;       // e.g., "Apple Inc."
  stockValue: number;        // Decimal value, e.g., 195.50
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
  currentPrice: number;           // e.g., 195.75
  highPrice: number;              // Day high
  lowPrice: number;               // Day low
  openPrice: number;              // Day opening price
  previousClosePrice: number;     // Previous day close
  timestamp: number;              // Unix timestamp
}

export interface FinnhubCompanyProfileDTO {
  ticker: string;                 // e.g., "AAPL"
  companyName: string;            // e.g., "Apple Inc."
  industry: string;               // e.g., "technology"
  marketCap: number;              // e.g., 3000000000000
  logo: string;                   // URL to logo
  country: string;                // e.g., "US"
  currency: string;               // e.g., "USD"
  phone: string;                  // e.g., "+1-408-996-1010"
  website: string;                // e.g., "https://www.apple.com"
  description: string;            // Company description
}

// ============================================================================
// News Types
// ============================================================================

export type FrontendSector =
  | 'Technology'
  | 'Semiconductors'
  | 'FinTech'
  | 'Consumer/Tech'
  | 'Healthcare'
  | 'Retail'
  | 'Other';

export interface NewsArticleDTO {
  sector: FrontendSector;    // Mapped frontend sector
  title: string;             // Article headline
  summary: string;           // Article description/summary
  url: string;               // Full URL to article
  publishedAt: string;       // ISO 8601 timestamp, e.g., "2025-12-06T10:30:00Z"
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
// Request Parameter Types
// ============================================================================

export interface NewsQueryParams {
  limit?: number;            // Maximum number of articles (default: 50)
}

export interface NewsBySectorsParams {
  sectors: FrontendSector[]; // Array of sectors to fetch news for
  limit?: number;            // Maximum number of articles (default: 50)
}
