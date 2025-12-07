-- V2__Fix_Column_Names.sql
-- Fix column naming to match Hibernate's default naming strategy (camelCase -> lowercase)

-- Earnings table: actual_eps -> actualeps, estimated_eps -> estimatedeps
ALTER TABLE earnings RENAME COLUMN actual_eps TO actualeps;
ALTER TABLE earnings RENAME COLUMN estimated_eps TO estimatedeps;
