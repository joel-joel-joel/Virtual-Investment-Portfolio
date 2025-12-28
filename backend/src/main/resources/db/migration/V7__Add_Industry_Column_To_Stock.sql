-- Add industry column to stock table
-- This field stores the industry/sector information for stocks
-- Populated from Finnhub API via StockServiceImpl.populateMissingIndustryData()

ALTER TABLE stock
ADD COLUMN industry VARCHAR(100);
