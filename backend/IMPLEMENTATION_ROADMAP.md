# Backend Implementation Roadmap

## Current Status
- ✅ Stock endpoints (6)
- ✅ FinnHub integration (2)
- ✅ News endpoints (3)
- ❌ Authentication endpoints (7)
- ❌ Portfolio management (3)
- ❌ Transaction management (5)
- ❌ Watchlist (4)
- ❌ Dashboard & Analytics (6)
- ❌ Earnings calendar (2)
- ❌ Price alerts (3)

**Total Missing: 40 endpoints**

---

## Phase 1: Core Infrastructure (CRITICAL)
1. Create missing DTOs
2. Create missing entities (Watchlist, PriceAlert, Activity)
3. Create repositories for new entities
4. Implement Auth endpoints (7)
5. Implement Portfolio endpoints (3)

**Timeline:** Implement immediately as foundation

---

## Phase 2: Transactions & Operations (CRITICAL)
1. Enhance Transaction endpoints (5 total)
2. Watchlist endpoints (4)
3. Implement transaction logic and validation

**Timeline:** Implement after Phase 1

---

## Phase 3: Analytics & Monitoring (HIGH)
1. Dashboard endpoints (6)
2. Earnings calendar (2)
3. Price alerts (3)

**Timeline:** Complete after Phase 1 & 2

---

## Database Tables Required

```
CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  username VARCHAR(255) UNIQUE NOT NULL,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  roles VARCHAR(50)
);

CREATE TABLE accounts (
  account_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  account_name VARCHAR(255),
  account_balance DECIMAL(19,2),
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE transactions (
  transaction_id UUID PRIMARY KEY,
  account_id UUID NOT NULL,
  stock_id UUID NOT NULL,
  type VARCHAR(10), -- BUY, SELL
  shares DECIMAL(19,2),
  price_per_share DECIMAL(19,4),
  total_amount DECIMAL(19,2),
  fees DECIMAL(19,2),
  net_amount DECIMAL(19,2),
  order_type VARCHAR(20),
  status VARCHAR(20),
  created_at TIMESTAMP,
  executed_at TIMESTAMP,
  FOREIGN KEY (account_id) REFERENCES accounts(account_id),
  FOREIGN KEY (stock_id) REFERENCES stock(stock_id)
);

CREATE TABLE watchlist (
  watchlist_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  stock_id UUID NOT NULL,
  added_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (stock_id) REFERENCES stock(stock_id)
);

CREATE TABLE price_alerts (
  alert_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  stock_id UUID NOT NULL,
  type VARCHAR(10), -- ABOVE, BELOW
  target_price DECIMAL(19,4),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP,
  triggered_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (stock_id) REFERENCES stock(stock_id)
);

CREATE TABLE activities (
  activity_id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  type VARCHAR(50),
  stock_code VARCHAR(10),
  company_name VARCHAR(255),
  description TEXT,
  amount DECIMAL(19,2),
  created_at TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE earnings (
  earning_id UUID PRIMARY KEY,
  stock_id UUID NOT NULL,
  earnings_date DATE,
  estimated_eps DECIMAL(19,4),
  actual_eps DECIMAL(19,4),
  report_time VARCHAR(20),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (stock_id) REFERENCES stock(stock_id)
);
```

---

## Service Layer Structure

Each feature will have:
- `Entity` - JPA entity
- `DTO` - Data transfer objects
- `Repository` - Spring Data repository
- `Service` - Business logic
- `Controller` - REST endpoints
- `Validation` - Input validation

---

## Error Handling Strategy

1. Custom exception classes
2. Global exception handler
3. Consistent error response format
4. Proper HTTP status codes

---

## Authentication Flow

1. User registers → JWT token issued
2. User logs in → JWT token issued
3. Token includes: userId, email, expiration
4. Token refresh endpoint for expired tokens
5. Token verification on protected routes

---

## Next Steps

1. Implement all missing entities
2. Create DTOs for all new endpoints
3. Create repositories
4. Implement services with business logic
5. Create controllers
6. Add validation and error handling
7. Test all endpoints
8. Update API documentation
