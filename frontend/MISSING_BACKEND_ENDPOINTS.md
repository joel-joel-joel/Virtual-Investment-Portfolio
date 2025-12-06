# Missing Backend API Endpoints

This document lists all the API endpoints that are referenced in the frontend services but are **NOT YET IMPLEMENTED** in the backend.

The frontend service methods have been created with proper TypeScript types and error handling, but they will not work until the corresponding backend endpoints are implemented.

---

## Priority Levels

- 🔴 **Critical** - Required for core app functionality
- 🟡 **High** - Important for user experience
- 🟢 **Medium** - Nice to have features
- ⚪ **Low** - Optional enhancements

---

## 1. Authentication Endpoints 🔴 CRITICAL

### 1.1 User Registration
**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "token": "jwt_token_here",
  "userId": "uuid",
  "email": "user@example.com"
}
```

**Frontend Service:** `authService.register()`

---

### 1.2 User Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "jwt_token_here",
  "userId": "uuid",
  "email": "user@example.com",
  "expiresAt": "2025-12-07T10:30:00Z"
}
```

**Frontend Service:** `authService.login()`

---

### 1.3 Get Current User Profile
**Endpoint:** `GET /api/auth/me`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2025-01-01T00:00:00Z"
}
```

**Frontend Service:** `authService.getCurrentUser()`

---

### 1.4 Logout (Optional)
**Endpoint:** `POST /api/auth/logout`

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

**Purpose:** Server-side token invalidation for security

**Frontend Service:** `authService.logout()`

---

### 1.5 Token Refresh 🟡 HIGH
**Endpoint:** `POST /api/auth/refresh`

**Headers:** `Authorization: Bearer <old_token>`

**Response:**
```json
{
  "token": "new_jwt_token_here",
  "userId": "uuid",
  "email": "user@example.com"
}
```

**Frontend Service:** `authService.refreshToken()`

---

### 1.6 Verify Token 🟢 MEDIUM
**Endpoint:** `GET /api/auth/verify`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "valid": true
}
```

**Frontend Service:** `authService.verifyToken()`

---

### 1.7 Password Reset Flow 🟢 MEDIUM

#### Forgot Password
**Endpoint:** `POST /api/auth/forgot-password`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "message": "Password reset email sent"
}
```

#### Reset Password
**Endpoint:** `POST /api/auth/reset-password`

**Request Body:**
```json
{
  "token": "reset_token_from_email",
  "newPassword": "newSecurePassword123"
}
```

**Response:**
```json
{
  "message": "Password reset successful"
}
```

#### Change Password
**Endpoint:** `POST /api/auth/change-password`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "currentPassword": "oldPassword",
  "newPassword": "newPassword123"
}
```

**Response:**
```json
{
  "message": "Password changed successfully"
}
```

**Frontend Services:**
- `authService.forgotPassword()`
- `authService.resetPassword()`
- `authService.changePassword()`

---

## 2. Portfolio Management Endpoints 🔴 CRITICAL

### 2.1 Get User Portfolio
**Endpoint:** `GET /api/portfolio`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "portfolioId": "uuid",
  "userId": "uuid",
  "totalValue": 125000.50,
  "totalGain": 25000.50,
  "totalGainPercent": 25.0,
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-12-06T10:30:00Z"
}
```

**Frontend Service:** `portfolioService.getPortfolio()`

---

### 2.2 Get Portfolio Holdings
**Endpoint:** `GET /api/portfolio/holdings`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
[
  {
    "holdingId": "uuid",
    "portfolioId": "uuid",
    "stockId": "uuid",
    "stockCode": "AAPL",
    "companyName": "Apple Inc.",
    "shares": 100,
    "averageCost": 150.00,
    "currentPrice": 195.50,
    "totalValue": 19550.00,
    "totalCost": 15000.00,
    "totalGain": 4550.00,
    "totalGainPercent": 30.33,
    "updatedAt": "2025-12-06T10:30:00Z"
  }
]
```

**Frontend Service:** `portfolioService.getPortfolioHoldings()`

---

### 2.3 Get Specific Holding
**Endpoint:** `GET /api/portfolio/holdings/{stockId}`

**Headers:** `Authorization: Bearer <token>`

**Response:** Same as single holding object above

**Frontend Service:** `portfolioService.getHoldingByStockId()`

---

## 3. Transaction Management Endpoints 🔴 CRITICAL

### 3.1 Get All Transactions
**Endpoint:** `GET /api/transactions`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `limit` (optional, default: 50)
- `offset` (optional, default: 0)
- `stockId` (optional) - Filter by stock
- `type` (optional) - Filter by BUY or SELL
- `startDate` (optional) - ISO 8601 timestamp
- `endDate` (optional) - ISO 8601 timestamp

**Example:** `GET /api/transactions?limit=20&type=BUY&stockId=uuid`

**Response:**
```json
[
  {
    "transactionId": "uuid",
    "userId": "uuid",
    "portfolioId": "uuid",
    "stockId": "uuid",
    "stockCode": "AAPL",
    "companyName": "Apple Inc.",
    "type": "BUY",
    "shares": 10,
    "pricePerShare": 195.50,
    "totalAmount": 1955.00,
    "fees": 1.96,
    "netAmount": 1956.96,
    "orderType": "MARKET",
    "status": "COMPLETED",
    "createdAt": "2025-12-06T09:30:00Z",
    "executedAt": "2025-12-06T09:30:05Z"
  }
]
```

**Frontend Service:** `portfolioService.getAllTransactions()`

---

### 3.2 Get Transactions by Stock
**Endpoint:** `GET /api/transactions/stock/{stockId}`

**Headers:** `Authorization: Bearer <token>`

**Response:** Array of transactions (same format as above)

**Frontend Service:** `portfolioService.getTransactionsByStock()`

---

### 3.3 Create Transaction
**Endpoint:** `POST /api/transactions`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "stockId": "uuid",
  "type": "BUY",
  "shares": 10,
  "pricePerShare": 195.50,
  "orderType": "MARKET"
}
```

**Response:** Transaction object (same format as above)

**Frontend Service:** `portfolioService.createTransaction()`

---

### 3.4 Get Transaction by ID
**Endpoint:** `GET /api/transactions/{transactionId}`

**Headers:** `Authorization: Bearer <token>`

**Response:** Single transaction object

**Frontend Service:** `portfolioService.getTransactionById()`

---

### 3.5 Cancel Transaction 🟢 MEDIUM
**Endpoint:** `PUT /api/transactions/{transactionId}/cancel`

**Headers:** `Authorization: Bearer <token>`

**Response:** Updated transaction object with status "CANCELLED"

**Frontend Service:** `portfolioService.cancelTransaction()`

---

## 4. Watchlist Endpoints 🟡 HIGH

### 4.1 Get Watchlist
**Endpoint:** `GET /api/watchlist`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
[
  {
    "watchlistId": "uuid",
    "userId": "uuid",
    "stockId": "uuid",
    "stockCode": "MSFT",
    "companyName": "Microsoft Corporation",
    "currentPrice": 420.75,
    "priceChange": 5.25,
    "priceChangePercent": 1.26,
    "addedAt": "2025-12-01T10:00:00Z"
  }
]
```

**Frontend Service:** `portfolioService.getWatchlist()`

---

### 4.2 Add to Watchlist
**Endpoint:** `POST /api/watchlist`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "stockId": "uuid"
}
```

**Response:** WatchlistItem object (same format as above)

**Frontend Service:** `portfolioService.addToWatchlist()`

---

### 4.3 Remove from Watchlist
**Endpoint:** `DELETE /api/watchlist/{stockId}`

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

**Frontend Service:** `portfolioService.removeFromWatchlist()`

---

### 4.4 Check if Stock is in Watchlist 🟢 MEDIUM
**Endpoint:** `GET /api/watchlist/check/{stockId}`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "inWatchlist": true
}
```

**Frontend Service:** `portfolioService.isInWatchlist()`

---

## 5. Dashboard & Analytics Endpoints 🟡 HIGH

### 5.1 Get Dashboard Summary
**Endpoint:** `GET /api/dashboard/summary`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
{
  "totalValue": 125000.50,
  "totalGain": 25000.50,
  "totalGainPercent": 25.0,
  "dayChange": 1250.00,
  "dayChangePercent": 1.01,
  "topGainers": [],
  "topLosers": [],
  "sectorAllocation": [],
  "recentActivity": []
}
```

**Frontend Service:** `dashboardService.getDashboardSummary()`

---

### 5.2 Get Sector Allocation
**Endpoint:** `GET /api/dashboard/sectors`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
[
  {
    "sector": "Technology",
    "value": 50000.00,
    "percentage": 40.0,
    "change": 5000.00,
    "changePercent": 11.11
  }
]
```

**Frontend Service:** `dashboardService.getSectorAllocation()`

---

### 5.3 Get Recent Activity
**Endpoint:** `GET /api/dashboard/activity`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `limit` (optional, default: 10)

**Response:**
```json
[
  {
    "activityId": "uuid",
    "type": "BUY",
    "stockCode": "AAPL",
    "companyName": "Apple Inc.",
    "description": "Bought 10 shares at $195.50",
    "timestamp": "2025-12-06T09:30:00Z",
    "amount": 1955.00
  }
]
```

**Frontend Service:** `dashboardService.getRecentActivity()`

---

### 5.4 Get Portfolio Performance
**Endpoint:** `GET /api/dashboard/performance`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `period` (optional) - Values: 1D, 1W, 1M, 3M, 1Y, ALL

**Example:** `GET /api/dashboard/performance?period=1M`

**Response:**
```json
[
  {
    "date": "2025-11-06",
    "portfolioValue": 120000.00,
    "gain": 20000.00,
    "gainPercent": 20.0
  },
  {
    "date": "2025-11-07",
    "portfolioValue": 121500.00,
    "gain": 21500.00,
    "gainPercent": 21.5
  }
]
```

**Frontend Service:** `dashboardService.getPortfolioPerformance()`

---

### 5.5 Get Top Gainers
**Endpoint:** `GET /api/dashboard/top-gainers`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `limit` (optional, default: 5)

**Response:** Array of PortfolioHolding objects (sorted by gain %)

**Frontend Service:** `dashboardService.getTopGainers()`

---

### 5.6 Get Top Losers
**Endpoint:** `GET /api/dashboard/top-losers`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `limit` (optional, default: 5)

**Response:** Array of PortfolioHolding objects (sorted by loss %)

**Frontend Service:** `dashboardService.getTopLosers()`

---

## 6. Earnings Calendar Endpoints 🟢 MEDIUM

### 6.1 Get Upcoming Earnings (Portfolio)
**Endpoint:** `GET /api/dashboard/earnings`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `days` (optional, default: 7) - Number of days ahead

**Response:**
```json
[
  {
    "stockCode": "AAPL",
    "companyName": "Apple Inc.",
    "earningsDate": "2025-12-15",
    "estimatedEPS": 2.45,
    "actualEPS": null,
    "reportTime": "AMC"
  }
]
```

**Frontend Service:** `dashboardService.getUpcomingEarnings()`

---

### 6.2 Get All Upcoming Earnings (Public)
**Endpoint:** `GET /api/dashboard/earnings/all`

**Headers:** None (public endpoint)

**Query Parameters:**
- `days` (optional, default: 7)
- `sector` (optional) - Filter by sector

**Response:** Same as above

**Frontend Service:** `dashboardService.getAllUpcomingEarnings()`

---

## 7. Price Alerts Endpoints 🟢 MEDIUM

### 7.1 Get Price Alerts
**Endpoint:** `GET /api/alerts`

**Headers:** `Authorization: Bearer <token>`

**Response:**
```json
[
  {
    "alertId": "uuid",
    "stockId": "uuid",
    "stockCode": "AAPL",
    "type": "ABOVE",
    "targetPrice": 200.00,
    "currentPrice": 195.50,
    "isActive": true,
    "createdAt": "2025-12-01T10:00:00Z",
    "triggeredAt": null
  }
]
```

**Frontend Service:** `dashboardService.getPriceAlerts()`

---

### 7.2 Create Price Alert
**Endpoint:** `POST /api/alerts`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "stockId": "uuid",
  "type": "ABOVE",
  "targetPrice": 200.00
}
```

**Response:** PriceAlert object (same format as above)

**Frontend Service:** `dashboardService.createPriceAlert()`

---

### 7.3 Delete Price Alert
**Endpoint:** `DELETE /api/alerts/{alertId}`

**Headers:** `Authorization: Bearer <token>`

**Response:** `204 No Content`

**Frontend Service:** `dashboardService.deletePriceAlert()`

---

## Implementation Priority Recommendation

### Phase 1 - MVP (Minimum Viable Product) 🔴
1. Authentication (Register, Login, Get User)
2. Portfolio Management (Get Portfolio, Get Holdings)
3. Transaction Management (Create, Get All, Get by Stock)

### Phase 2 - Core Features 🟡
4. Watchlist (Get, Add, Remove)
5. Dashboard Summary
6. Portfolio Performance

### Phase 3 - Enhanced Features 🟢
7. Price Alerts
8. Earnings Calendar
9. Token Refresh & Verification
10. Password Reset Flow
11. Transaction Cancellation

---

## Database Schema Implications

To implement these endpoints, your backend will need the following database tables:

1. **users** - User accounts
2. **portfolios** - User portfolios (1:1 with users)
3. **holdings** - Stock positions in portfolios
4. **transactions** - Buy/sell transaction history
5. **watchlist** - User watchlist items
6. **price_alerts** - User-configured price alerts
7. **activities** - Recent activity log

Most of these can be implemented using your existing Stock table plus the new user-related tables.

---

## Testing the Services

All frontend services are ready to use. To test them:

1. **Without Backend**: Services will throw errors (expected)
2. **With Mock Data**: Use tools like MSW (Mock Service Worker) to intercept API calls
3. **With Backend**: Once endpoints are implemented, update `API_BASE_URL` in `src/services/api.ts`

Example usage:
```typescript
import { getAllStocks } from '@/src/services/entityService';
import { getAllNews } from '@/src/services/newsService';

// These work now (backend exists)
const stocks = await getAllStocks();
const news = await getAllNews();

// These will fail (backend missing)
import { getPortfolio } from '@/src/services/portfolioService';
const portfolio = await getPortfolio(); // ❌ 404 error
```

---

## Summary

**Total Missing Endpoints: 40**

- 🔴 Critical: 11 endpoints
- 🟡 High Priority: 10 endpoints
- 🟢 Medium/Low Priority: 19 endpoints

**Already Implemented (Working):**
- ✅ 6 Stock CRUD endpoints
- ✅ 2 FinnHub real-time data endpoints
- ✅ 3 News endpoints

**Total Backend Coverage: 11/51 endpoints (21.6%)**
