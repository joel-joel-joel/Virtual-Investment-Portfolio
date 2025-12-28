-- Fix NULL user preferences and add NOT NULL constraints
-- This migration ensures all users have valid preference values

-- Update any NULL values to intended defaults
UPDATE users
SET
    price_alerts = COALESCE(price_alerts, true),
    portfolio_updates = COALESCE(portfolio_updates, true),
    market_news = COALESCE(market_news, false),
    dividend_notifications = COALESCE(dividend_notifications, true),
    earning_season = COALESCE(earning_season, false)
WHERE
    price_alerts IS NULL
    OR portfolio_updates IS NULL
    OR market_news IS NULL
    OR dividend_notifications IS NULL
    OR earning_season IS NULL;

-- Add NOT NULL constraints to prevent future NULL values
ALTER TABLE users ALTER COLUMN price_alerts SET NOT NULL;
ALTER TABLE users ALTER COLUMN portfolio_updates SET NOT NULL;
ALTER TABLE users ALTER COLUMN market_news SET NOT NULL;
ALTER TABLE users ALTER COLUMN dividend_notifications SET NOT NULL;
ALTER TABLE users ALTER COLUMN earning_season SET NOT NULL;
