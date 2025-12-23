-- Add notification preference columns to users table
ALTER TABLE users
ADD COLUMN price_alerts BOOLEAN DEFAULT true,
ADD COLUMN portfolio_updates BOOLEAN DEFAULT true,
ADD COLUMN market_news BOOLEAN DEFAULT false,
ADD COLUMN dividend_notifications BOOLEAN DEFAULT true,
ADD COLUMN earning_season BOOLEAN DEFAULT false;

-- Add indexes for potential filtering/reporting
CREATE INDEX idx_users_price_alerts ON users(price_alerts) WHERE price_alerts = true;
CREATE INDEX idx_users_portfolio_updates ON users(portfolio_updates) WHERE portfolio_updates = true;
