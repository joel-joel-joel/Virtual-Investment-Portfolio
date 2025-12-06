/**
 * News Service
 * Handles all news-related API calls
 * All endpoints are public (no authentication required)
 */

import { apiFetch, buildQueryString } from './api';
import {
  NewsArticleDTO,
  FrontendSector,
  NewsQueryParams,
  NewsBySectorsParams,
} from '../types/api';

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
 * @param sector - Frontend sector name (e.g., "Technology", "FinTech")
 * @param limit - Maximum number of articles to return (default: 50)
 * @returns Array of news articles for the specified sector
 */
export const getNewsBySector = async (
  sector: FrontendSector,
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
 * @param sectors - Array of frontend sector names
 * @param limit - Maximum number of articles to return (default: 50)
 * @returns Array of news articles from all selected sectors
 */
export const getNewsByMultipleSectors = async (
  sectors: FrontendSector[],
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
// Convenience Functions
// ============================================================================

/**
 * Get news for Technology sector
 * @param limit - Maximum number of articles (default: 50)
 * @returns Technology news articles
 */
export const getTechnologyNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  return getNewsBySector('Technology', limit);
};

/**
 * Get news for Semiconductors sector
 * @param limit - Maximum number of articles (default: 50)
 * @returns Semiconductors news articles
 */
export const getSemiconductorsNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  return getNewsBySector('Semiconductors', limit);
};

/**
 * Get news for FinTech sector
 * @param limit - Maximum number of articles (default: 50)
 * @returns FinTech news articles
 */
export const getFinTechNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  return getNewsBySector('FinTech', limit);
};

/**
 * Get news for Consumer/Tech sector
 * @param limit - Maximum number of articles (default: 50)
 * @returns Consumer/Tech news articles
 */
export const getConsumerTechNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  return getNewsBySector('Consumer/Tech', limit);
};

/**
 * Get news for Healthcare sector
 * @param limit - Maximum number of articles (default: 50)
 * @returns Healthcare news articles
 */
export const getHealthcareNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  return getNewsBySector('Healthcare', limit);
};

/**
 * Get news for Retail sector
 * @param limit - Maximum number of articles (default: 50)
 * @returns Retail news articles
 */
export const getRetailNews = async (
  limit: number = 50
): Promise<NewsArticleDTO[]> => {
  return getNewsBySector('Retail', limit);
};

// ============================================================================
// Error Handling Wrappers
// ============================================================================

/**
 * Get news by sector with fallback handling
 * Returns empty array if the sector is invalid or API fails
 * @param sector - Frontend sector name
 * @param limit - Maximum number of articles
 * @returns News articles or empty array on error
 */
export const getNewsBySectorSafe = async (
  sector: FrontendSector,
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
): Record<FrontendSector, NewsArticleDTO[]> => {
  const grouped: Record<string, NewsArticleDTO[]> = {
    Technology: [],
    Semiconductors: [],
    FinTech: [],
    'Consumer/Tech': [],
    Healthcare: [],
    Retail: [],
    Other: [],
  };

  articles.forEach((article) => {
    if (grouped[article.sector]) {
      grouped[article.sector].push(article);
    } else {
      grouped.Other.push(article);
    }
  });

  return grouped as Record<FrontendSector, NewsArticleDTO[]>;
};

/**
 * Filter out "Other" sector news
 * @param articles - Array of news articles
 * @returns Articles excluding "Other" sector
 */
export const excludeOtherSectorNews = (
  articles: NewsArticleDTO[]
): NewsArticleDTO[] => {
  return articles.filter((article) => article.sector !== 'Other');
};

/**
 * Get unique sectors from news articles
 * @param articles - Array of news articles
 * @returns Array of unique sectors
 */
export const getUniqueSectors = (
  articles: NewsArticleDTO[]
): FrontendSector[] => {
  const sectors = new Set(articles.map((article) => article.sector));
  return Array.from(sectors);
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
