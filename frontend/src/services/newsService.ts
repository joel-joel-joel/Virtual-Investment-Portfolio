/**
 * News Service
 * Handles all news-related API calls
 * All endpoints are public (no authentication required)
 */

import { apiFetch, buildQueryString } from './api';
import { NewsArticleDTO } from '../types/api';

// ============================================================================
// News Endpoints (No Authentication Required)
// ============================================================================

/**
 * Get all latest news articles across all sectors
 * @param limit - Maximum number of articles to return (default: 50)
 * @returns Array of news articles
 */
export const getAllNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  const queryString = buildQueryString({ limit });

  return apiFetch<NewsArticleDTO[]>(`/api/news${queryString}`, {
    method: 'GET',
    requireAuth: false,
  });
};

/**
 * Get news articles filtered by a specific sector
 * @param sector - Sector name from Finnhub (e.g., "Technology", "Financial Services")
 * @param limit - Maximum number of articles to return (default: 50)
 * @returns Array of news articles for the specified sector
 */
export const getNewsBySector = async (
  sector: string,
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  const queryString = buildQueryString({ limit });

  return apiFetch<NewsArticleDTO[]>(
    `/api/news/sector/${sector}${queryString}`,
    {
      method: 'GET',
      requireAuth: false,
    }
  );
};

/**
 * Get news articles filtered by multiple sectors
 * @param sectors - Array of sector names from Finnhub
 * @param limit - Maximum number of articles to return (default: 50)
 * @returns Array of news articles from all selected sectors
 */
export const getNewsByMultipleSectors = async (
  sectors: string[],
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  const queryString = buildQueryString({
    sectors: sectors.join(','),
    limit,
  });

  return apiFetch<NewsArticleDTO[]>(`/api/news/sectors${queryString}`, {
    method: 'GET',
    requireAuth: false,
  });
};

// ============================================================================
// Error Handling Wrappers
// ============================================================================

/**
 * Get news by sector with fallback handling
 * Returns empty array if the sector is invalid or API fails
 * @param sector - Sector name from Finnhub
 * @param limit - Maximum number of articles
 * @returns News articles or empty array on error
 */
export const getNewsBySectorSafe = async (
  sector: string,
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  try {
    return await getNewsBySector(sector, limit);
  } catch (error) {
    console.error(`Failed to fetch news for sector ${sector}:`, error);
    return [];
  }
};

/**
 * Get all news with fallback handling
 * Returns empty array if API fails
 * @param limit - Maximum number of articles
 * @returns News articles or empty array on error
 */
export const getAllNewsSafe = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  try {
    return await getAllNews(limit);
  } catch (error) {
    console.error('Failed to fetch all news:', error);
    return [];
  }
};

// ============================================================================
// Utility Functions
// ============================================================================

/**
 * Group news articles by sector
 * @param articles - Array of news articles
 * @returns Object mapping sectors to their articles
 */
export const groupNewsBySector = (
  articles: NewsArticleDTO[]
): Record<string, NewsArticleDTO[]> => {
  const grouped: Record<string, NewsArticleDTO[]> = {};

  articles.forEach((article) => {
    if (!grouped[article.sector]) {
      grouped[article.sector] = [];
    }
    grouped[article.sector].push(article);
  });

  return grouped;
};

/**
 * Filter articles with empty sector
 * @param articles - Array of news articles
 * @returns Articles with non-empty sectors
 */
export const filterArticlesWithSector = (
  articles: NewsArticleDTO[]
): NewsArticleDTO[] => {
  return articles.filter((article) => article.sector && article.sector.trim());
};

/**
 * Get unique sectors from news articles
 * @param articles - Array of news articles
 * @returns Array of unique sectors (sorted)
 */
export const getUniqueSectors = (
  articles: NewsArticleDTO[]
): string[] => {
  const sectors = new Set(
    articles
      .map((article) => article.sector)
      .filter((s) => s && s.trim())
  );
  return Array.from(sectors).sort();
};

/**
 * Sort news articles by published date (newest first)
 * @param articles - Array of news articles
 * @returns Sorted articles
 */
export const sortNewsByDate = (
  articles: NewsArticleDTO[]
): NewsArticleDTO[] => {
  return [...articles].sort((a, b) => {
    const dateA = new Date(a.publishedAt).getTime();
    const dateB = new Date(b.publishedAt).getTime();
    return dateB - dateA; // Newest first
  });
};
