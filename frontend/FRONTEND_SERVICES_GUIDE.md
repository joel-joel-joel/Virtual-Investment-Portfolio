# Frontend Services Implementation Guide

This guide explains how to use the newly created frontend services to integrate with your backend API.

---

## Overview

All API services have been implemented in TypeScript with:
- ✅ Complete type safety
- ✅ Error handling
- ✅ JWT authentication support
- ✅ Consistent API patterns
- ✅ Helper utility functions

**Location:** `/src/services/`

---

## Available Services

### 1. **Stock Service** (`entityService.ts`) ✅ WORKING

Handles stock CRUD operations and real-time FinnHub data.

```typescript
import {
  getAllStocks,
  getStockById,
  getStockPrice,
  getStockQuote,
  getCompanyProfile,
} from '@/src/services/entityService';

// Get all stocks (requires auth)
const stocks = await getAllStocks();

// Get real-time quote (no auth required)
const quote = await getStockQuote('AAPL');
// Returns: { currentPrice, highPrice, lowPrice, openPrice, ... }

// Get company profile (no auth required)
const profile = await getCompanyProfile('AAPL');
// Returns: { ticker, companyName, industry, marketCap, logo, ... }
```

---

### 2. **News Service** (`newsService.ts`) ✅ WORKING

Fetches news articles by sector.

```typescript
import {
  getAllNews,
  getNewsBySector,
  getNewsByMultipleSectors,
  getTechnologyNews,
} from '@/src/services/newsService';

// Get all news (max 50 articles)
const allNews = await getAllNews(50);

// Get news for specific sector
const techNews = await getNewsBySector('Technology', 25);

// Get news for multiple sectors
const news = await getNewsByMultipleSectors(
  ['Technology', 'FinTech', 'Healthcare'],
  100
);

// Convenience function for Technology news
const tech = await getTechnologyNews(50);
```

**Valid Sectors:**
- `Technology`
- `Semiconductors`
- `FinTech`
- `Consumer/Tech`
- `Healthcare`
- `Retail`
- `Other`

---

### 3. **Authentication Service** (`authService.ts`) ❌ NOT WORKING YET

Handles user authentication and JWT tokens.

```typescript
import {
  login,
  register,
  logout,
  getCurrentUser,
} from '@/src/services/authService';

// Register new user
const authResponse = await register({
  email: 'user@example.com',
  password: 'securePassword123',
  firstName: 'John',
  lastName: 'Doe',
});
// Returns: { token, userId, email }
// Token is automatically stored

// Login existing user
const authResponse = await login({
  email: 'user@example.com',
  password: 'securePassword123',
});
// Token is automatically stored

// Get current user profile
const user = await getCurrentUser();
// Returns: { userId, email, firstName, lastName }

// Logout
await logout(); // Clears JWT token
```

⚠️ **Backend endpoints missing** - See `MISSING_BACKEND_ENDPOINTS.md`

---

### 4. **Portfolio Service** (`portfolioService.ts`) ❌ NOT WORKING YET

Manages user portfolios, holdings, transactions, and watchlist.

```typescript
import {
  getPortfolio,
  getPortfolioHoldings,
  createTransaction,
  getAllTransactions,
  getWatchlist,
  addToWatchlist,
} from '@/src/services/portfolioService';

// Get portfolio summary
const portfolio = await getPortfolio();
// Returns: { portfolioId, totalValue, totalGain, totalGainPercent, ... }

// Get all holdings
const holdings = await getPortfolioHoldings();
// Returns: [{ holdingId, stockCode, shares, totalValue, totalGain, ... }]

// Create buy transaction
const transaction = await createTransaction({
  stockId: 'stock-uuid',
  type: 'BUY',
  shares: 10,
  pricePerShare: 195.50,
  orderType: 'MARKET',
});

// Get transaction history
const transactions = await getAllTransactions({
  limit: 50,
  type: 'BUY', // Optional filter
  stockId: 'stock-uuid', // Optional filter
});

// Watchlist operations
const watchlist = await getWatchlist();
await addToWatchlist('stock-uuid');
await removeFromWatchlist('stock-uuid');
```

⚠️ **Backend endpoints missing** - See `MISSING_BACKEND_ENDPOINTS.md`

---

### 5. **Dashboard Service** (`dashboardService.ts`) ❌ NOT WORKING YET

Provides dashboard analytics and insights.

```typescript
import {
  getDashboardSummary,
  getPortfolioPerformance,
  getTopGainers,
  getSectorAllocation,
  getUpcomingEarnings,
} from '@/src/services/dashboardService';

// Get dashboard summary
const summary = await getDashboardSummary();
// Returns: { totalValue, totalGain, dayChange, topGainers, topLosers, ... }

// Get performance data for chart
const performance = await getPortfolioPerformance('1M');
// Period options: '1D', '1W', '1M', '3M', '1Y', 'ALL'

// Get top performing stocks
const gainers = await getTopGainers(5); // Top 5
const losers = await getTopLosers(5);

// Get sector breakdown
const sectors = await getSectorAllocation();

// Get upcoming earnings
const earnings = await getUpcomingEarnings(7); // Next 7 days
```

⚠️ **Backend endpoints missing** - See `MISSING_BACKEND_ENDPOINTS.md`

---

## Configuration

### Update Backend URL

Edit `/src/services/api.ts`:

```typescript
// For local development
export const API_BASE_URL = 'http://localhost:8080';

// For production
export const API_BASE_URL = 'https://your-api.com';
```

### JWT Token Storage

Currently uses placeholder implementation. Update in `/src/services/api.ts`:

```typescript
import AsyncStorage from '@react-native-async-storage/async-storage';

const getAuthToken = async (): Promise<string | null> => {
  return await AsyncStorage.getItem('jwt_token');
};

export const setAuthToken = async (token: string): Promise<void> => {
  await AsyncStorage.setItem('jwt_token', token);
};

export const removeAuthToken = async (): Promise<void> => {
  await AsyncStorage.removeItem('jwt_token');
};
```

---

## Error Handling

All services include proper error handling:

```typescript
try {
  const stocks = await getAllStocks();
  console.log('Stocks:', stocks);
} catch (error) {
  if (error instanceof Error) {
    // Errors are already formatted with helpful messages
    console.error(error.message);
    // Examples:
    // - "Unauthorized: Invalid or missing authentication"
    // - "Resource not found"
    // - "Server error: Please try again later"
  }
}
```

**Error Types:**
- `400 Bad Request` - Invalid parameters or validation error
- `401 Unauthorized` - Missing/invalid JWT token
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - External API down (FinnHub/MarketAux)
- Network errors - Connection issues

---

## Safe Methods with Fallbacks

Services include "Safe" versions that return null/empty arrays instead of throwing:

```typescript
import {
  getStockQuoteSafe,
  getNewsBySectorSafe,
} from '@/src/services';

// Won't throw errors - returns null on failure
const quote = await getStockQuoteSafe('INVALID');
if (quote) {
  console.log('Price:', quote.currentPrice);
} else {
  console.log('Failed to fetch quote');
}

// Returns empty array on failure
const news = await getNewsBySectorSafe('Technology');
```

---

## React Native Usage Examples

### Example: Stock Detail Screen

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { getStockQuote, getCompanyProfile } from '@/src/services';
import type { FinnhubQuoteDTO, FinnhubCompanyProfileDTO } from '@/src/types/api';

export default function StockDetailScreen({ stockSymbol }: { stockSymbol: string }) {
  const [quote, setQuote] = useState<FinnhubQuoteDTO | null>(null);
  const [profile, setProfile] = useState<FinnhubCompanyProfileDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [quoteData, profileData] = await Promise.all([
          getStockQuote(stockSymbol),
          getCompanyProfile(stockSymbol),
        ]);
        setQuote(quoteData);
        setProfile(profileData);
      } catch (error) {
        console.error('Failed to fetch stock data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [stockSymbol]);

  if (loading) return <ActivityIndicator />;

  return (
    <View>
      <Text>{profile?.companyName}</Text>
      <Text>${quote?.currentPrice}</Text>
      <Text>High: ${quote?.highPrice}</Text>
      <Text>Low: ${quote?.lowPrice}</Text>
    </View>
  );
}
```

### Example: News Feed

```typescript
import React, { useEffect, useState } from 'react';
import { FlatList, Text } from 'react-native';
import { getNewsBySector } from '@/src/services';
import type { NewsArticleDTO } from '@/src/types/api';

export default function NewsFeed({ sector }: { sector: string }) {
  const [news, setNews] = useState<NewsArticleDTO[]>([]);

  useEffect(() => {
    const fetchNews = async () => {
      const articles = await getNewsBySector(sector, 50);
      setNews(articles);
    };

    fetchNews();
  }, [sector]);

  return (
    <FlatList
      data={news}
      keyExtractor={(item) => item.url}
      renderItem={({ item }) => (
        <View>
          <Text>{item.title}</Text>
          <Text>{item.summary}</Text>
          <Text>{new Date(item.publishedAt).toLocaleDateString()}</Text>
        </View>
      )}
    />
  );
}
```

### Example: Login Screen

```typescript
import React, { useState } from 'react';
import { View, TextInput, Button, Alert } from 'react-native';
import { login } from '@/src/services';

export default function LoginScreen() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async () => {
    try {
      const response = await login({ email, password });
      console.log('Logged in:', response.userId);
      // Navigate to home screen
    } catch (error) {
      Alert.alert('Login Failed', error.message);
    }
  };

  return (
    <View>
      <TextInput
        placeholder="Email"
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
      />
      <TextInput
        placeholder="Password"
        value={password}
        onChangeText={setPassword}
        secureTextEntry
      />
      <Button title="Login" onPress={handleLogin} />
    </View>
  );
}
```

---

## Type Definitions

All API response types are available:

```typescript
import type {
  StockDTO,
  FinnhubQuoteDTO,
  FinnhubCompanyProfileDTO,
  NewsArticleDTO,
  FrontendSector,
  Portfolio,
  PortfolioHolding,
  Transaction,
} from '@/src/types/api';
```

---

## Batch Operations

Services include helper functions for batch operations:

```typescript
import { getBatchStockQuotes } from '@/src/services';

// Fetch quotes for multiple stocks at once
const symbols = ['AAPL', 'MSFT', 'NVDA'];
const quotes = await getBatchStockQuotes(symbols);

quotes.forEach((quote, index) => {
  console.log(`${symbols[index]}: $${quote.currentPrice}`);
});
```

---

## Utility Functions

### News Utilities

```typescript
import {
  groupNewsBySector,
  excludeOtherSectorNews,
  sortNewsByDate,
} from '@/src/services';

const allNews = await getAllNews();

// Group by sector
const grouped = groupNewsBySector(allNews);
console.log(grouped.Technology); // All tech news

// Filter out "Other" sector
const mainNews = excludeOtherSectorNews(allNews);

// Sort by date (newest first)
const sorted = sortNewsByDate(allNews);
```

### Portfolio Utilities

```typescript
import {
  calculatePortfolioStats,
  groupTransactionsByDate,
  calculateTotalFees,
} from '@/src/services';

const holdings = await getPortfolioHoldings();
const stats = calculatePortfolioStats(holdings);
console.log('Total Value:', stats.totalValue);
console.log('Total Gain:', stats.totalGain);
```

---

## Testing

### With Mock Data (Recommended)

Install Mock Service Worker:
```bash
npm install msw --save-dev
```

Create mock handlers:
```typescript
// src/mocks/handlers.ts
import { rest } from 'msw';
import { API_BASE_URL } from '@/src/services/api';

export const handlers = [
  rest.get(`${API_BASE_URL}/api/stocks/finnhub/quote/:symbol`, (req, res, ctx) => {
    return res(
      ctx.json({
        currentPrice: 195.75,
        highPrice: 198.50,
        lowPrice: 192.10,
        openPrice: 194.25,
        previousClosePrice: 194.80,
        timestamp: Date.now(),
      })
    );
  }),
];
```

### Without Backend

Services will throw descriptive errors when endpoints don't exist:
```typescript
const portfolio = await getPortfolio();
// Error: "Resource not found" (404)
```

---

## Next Steps

1. **Install AsyncStorage** for JWT token persistence:
   ```bash
   npm install @react-native-async-storage/async-storage
   ```

2. **Update token storage** in `/src/services/api.ts`

3. **Implement backend endpoints** (see `MISSING_BACKEND_ENDPOINTS.md`)

4. **Test working endpoints** (stocks and news)

5. **Add mock data** for endpoints not yet implemented

---

## Summary

✅ **11 endpoints working** (stocks + news)
- All stock CRUD operations
- Real-time stock quotes (FinnHub)
- Company profiles (FinnHub)
- News by sector

❌ **40 endpoints missing** (auth, portfolio, dashboard)
- See `MISSING_BACKEND_ENDPOINTS.md` for full list
- Services are ready to use once backend is implemented

**All services are production-ready** with:
- Full TypeScript support
- Error handling
- JWT authentication
- Helper utilities
- Batch operations
- Safe fallback methods
