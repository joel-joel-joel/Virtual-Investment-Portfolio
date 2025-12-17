-- V2__Add_Limit_Orders.sql
-- Add limit order support to Personal Investment Portfolio Tracker
-- Creates orders table and indexes for pending order queries

-- ==================== ORDERS TABLE ====================
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('BUY_LIMIT', 'SELL_LIMIT')),
    quantity NUMERIC(19, 8) NOT NULL,
    limit_price NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'EXECUTED', 'CANCELLED', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    failure_reason TEXT,
    CONSTRAINT fk_orders_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT fk_orders_stock FOREIGN KEY (stock_id) REFERENCES stock(stock_id) ON DELETE RESTRICT
);

-- Indexes for efficient queries
CREATE INDEX idx_orders_account_id ON orders(account_id);
CREATE INDEX idx_orders_stock_id ON orders(stock_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_status_created_at ON orders(status, created_at);
CREATE INDEX idx_orders_account_status ON orders(account_id, status);

-- Update transactions table CHECK constraint to allow BUY_LIMIT and SELL_LIMIT
ALTER TABLE transactions DROP CONSTRAINT transactions_transaction_type_check;
ALTER TABLE transactions ADD CONSTRAINT transactions_transaction_type_check CHECK (transaction_type IN ('BUY', 'SELL', 'BUY_LIMIT', 'SELL_LIMIT'));
