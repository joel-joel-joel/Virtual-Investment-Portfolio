-- V1__Initial_Schema.sql
-- Initial schema creation for Personal Investment Portfolio Tracker
-- This migration creates all core tables based on JPA entities

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==================== USERS TABLE ====================
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    roles VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- ==================== ACCOUNTS TABLE ====================
CREATE TABLE accounts (
    account_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    account_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);

-- ==================== STOCK TABLE ====================
CREATE TABLE stock (
    stock_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_code VARCHAR(20) NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    stock_value NUMERIC(19, 2) NOT NULL,
    dividend_per_share NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    account_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE SET NULL
);

CREATE INDEX idx_stock_code ON stock(stock_code);
CREATE INDEX idx_stock_company_name ON stock(company_name);
CREATE INDEX idx_stock_account_id ON stock(account_id);

-- ==================== TRANSACTIONS TABLE ====================
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    share_quantity NUMERIC(19, 2) NOT NULL,
    price_per_share NUMERIC(19, 2) NOT NULL,
    commission NUMERIC(19, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('BUY', 'SELL')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_transactions_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE RESTRICT
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_stock_id ON transactions(stock_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);

-- ==================== HOLDINGS TABLE ====================
CREATE TABLE holdings (
    holding_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    quantity NUMERIC(19, 8) NOT NULL,
    average_cost_basis NUMERIC(19, 4) NOT NULL,
    total_cost_basis NUMERIC(19, 2) NOT NULL,
    unrealized_gain NUMERIC(19, 2) DEFAULT 0.00,
    realized_gain NUMERIC(19, 2) DEFAULT 0.00,
    first_purchase_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_holdings_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_holdings_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE RESTRICT,
    CONSTRAINT uq_holdings_account_stock UNIQUE (account_id, stock_id)
);

CREATE INDEX idx_holdings_account_id ON holdings(account_id);
CREATE INDEX idx_holdings_stock_id ON holdings(stock_id);

-- ==================== DIVIDENDS TABLE ====================
CREATE TABLE dividends (
    dividend_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_id UUID NOT NULL,
    dividend_per_share NUMERIC(19, 2) NOT NULL,
    pay_date TIMESTAMP NOT NULL,
    announcement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dividends_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_dividends_stock_id ON dividends(stock_id);
CREATE INDEX idx_dividends_pay_date ON dividends(pay_date);

-- ==================== DIVIDEND PAYMENTS TABLE ====================
CREATE TABLE dividend_payments (
    payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    dividend_id UUID NOT NULL,
    share_quantity NUMERIC(19, 2) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),
    CONSTRAINT fk_dividend_payments_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_dividend_payments_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE,
    CONSTRAINT fk_dividend_payments_dividend FOREIGN KEY (dividend_id) REFERENCES dividends(dividend_id) ON DELETE CASCADE
);

CREATE INDEX idx_dividend_payments_account_id ON dividend_payments(account_id);
CREATE INDEX idx_dividend_payments_stock_id ON dividend_payments(stock_id);
CREATE INDEX idx_dividend_payments_dividend_id ON dividend_payments(dividend_id);
CREATE INDEX idx_dividend_payments_payment_date ON dividend_payments(payment_date);
CREATE INDEX idx_dividend_payments_status ON dividend_payments(status);

-- ==================== PRICE HISTORY TABLE ====================
CREATE TABLE price_history (
    price_history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_id UUID NOT NULL,
    close_date TIMESTAMP NOT NULL,
    close_price NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_price_history_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_price_history_stock_id ON price_history(stock_id);
CREATE INDEX idx_price_history_close_date ON price_history(close_date);
CREATE UNIQUE INDEX uq_price_history_stock_date ON price_history(stock_id, close_date);

-- ==================== PORTFOLIO SNAPSHOTS TABLE ====================
CREATE TABLE portfolio_snapshots (
    snapshot_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    snapshot_date DATE NOT NULL,
    total_value NUMERIC(19, 2) NOT NULL,
    cash_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    total_cost_basis NUMERIC(19, 2) NOT NULL,
    total_gain NUMERIC(19, 2),
    day_change NUMERIC(19, 2),
    day_change_percent NUMERIC(10, 4),
    realized_gain NUMERIC(19, 2) DEFAULT 0.00,
    unrealized_gain NUMERIC(19, 2) DEFAULT 0.00,
    total_dividends NUMERIC(19, 2) DEFAULT 0.00,
    roi_percentage NUMERIC(10, 4) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolio_snapshots_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

CREATE INDEX idx_portfolio_snapshots_account_id ON portfolio_snapshots(account_id);
CREATE INDEX idx_portfolio_snapshots_snapshot_date ON portfolio_snapshots(snapshot_date);
CREATE INDEX idx_portfolio_snapshots_created_at ON portfolio_snapshots(created_at);

-- ==================== WATCHLIST TABLE ====================
CREATE TABLE watchlist (
    watchlist_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_watchlist_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE,
    CONSTRAINT uq_watchlist_user_stock UNIQUE (user_id, stock_id)
);

CREATE INDEX idx_watchlist_user_id ON watchlist(user_id);
CREATE INDEX idx_watchlist_stock_id ON watchlist(stock_id);

-- ==================== PRICE ALERTS TABLE ====================
CREATE TABLE price_alerts (
    alert_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    target_price NUMERIC(19, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    triggered_at TIMESTAMP,
    CONSTRAINT fk_price_alerts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_price_alerts_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_price_alerts_user_id ON price_alerts(user_id);
CREATE INDEX idx_price_alerts_stock_id ON price_alerts(stock_id);
CREATE INDEX idx_price_alerts_is_active ON price_alerts(is_active);

-- ==================== EARNINGS TABLE ====================
CREATE TABLE earnings (
    earning_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stock_id UUID NOT NULL,
    earnings_date DATE NOT NULL,
    estimated_eps NUMERIC(19, 4),
    actual_eps NUMERIC(19, 4),
    report_time VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_earnings_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE CASCADE
);

CREATE INDEX idx_earnings_stock_id ON earnings(stock_id);
CREATE INDEX idx_earnings_earnings_date ON earnings(earnings_date);

-- ==================== ACTIVITIES TABLE ====================
CREATE TABLE activities (
    activity_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    stock_code VARCHAR(20),
    company_name VARCHAR(255),
    description TEXT,
    amount NUMERIC(19, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_activities_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_activities_user_id ON activities(user_id);
CREATE INDEX idx_activities_created_at ON activities(created_at);
CREATE INDEX idx_activities_type ON activities(type);
