/**
 * Services Index
 * Central export point for all API services
 */

// Base API utilities
export { API_BASE_URL, setAuthToken, removeAuthToken, apiFetch } from './api';

// Stock/Entity services (✅ Backend implemented)
export * from './entityService';

// News services (✅ Backend implemented)
export * from './newsService';

// Authentication services (❌ Backend NOT implemented yet)
export * from './authService';

// Portfolio services (❌ Backend NOT implemented yet)
export * from './portfolioService';

// Dashboard services (❌ Backend NOT implemented yet)
export * from './dashboardService';

// Type definitions
export * from '../types/api';
