import {
    getStockQuote,
    getCompanyProfile,
    getStockQuoteSafe,
    getCompanyProfileSafe,
} from './entityService';
import { getAccountHoldings } from './portfolioService';
import { getNewsBySectorSafe } from './newsService';
import { generateInsight } from '../utils/suggestionInsights';
import type {
    VolatilityData,
    NewsVolumeData,
    StockAnalysisData,
    ScoredStock,
    SuggestionStock,
    HoldingDTO,
    FinnhubQuoteDTO,
    FinnhubCompanyProfileDTO,
    SuggestionReason,
    NewsArticleDTO,
} from '../types/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

// ============================================================================
// Candidate Stock Pool (~25 stocks across multiple sectors)
// ============================================================================

const CANDIDATE_STOCKS = [
    // Tech/Semiconductors
    'NVDA',
    'AMD',
    'AVGO',
    'QCOM',
    'MU',
    'TSM',
    // FinTech/Crypto
    'COIN',
    'SQ',
    'PYPL',
    'HOOD',
    // Consumer/Tech
    'UBER',
    'LYFT',
    'DASH',
    'ABNB',
    // High Volatility
    'MSTR',
    'PLTR',
    'RBLX',
    'SNOW',
    // Diversification
    'NKE',
    'BA',
    'JPM',
    'V',
    'MA',
    'DIS',
    'NFLX',
];

// ============================================================================
// Cache Helpers
// ============================================================================

interface CacheEntry<T> {
    data: T;
    timestamp: number;
    ttlMs: number;
}

const setCacheEntry = async <T>(
    key: string,
    data: T,
    ttlMs: number
): Promise<void> => {
    const entry: CacheEntry<T> = {
        data,
        timestamp: Date.now(),
        ttlMs,
    };
    try {
        await AsyncStorage.setItem(key, JSON.stringify(entry));
    } catch (error) {
        console.warn(`Failed to cache ${key}:`, error);
    }
};

const getCacheEntry = async <T>(key: string): Promise<T | null> => {
    try {
        const json = await AsyncStorage.getItem(key);
        if (!json) return null;

        const entry: CacheEntry<T> = JSON.parse(json);
        const age = Date.now() - entry.timestamp;

        if (age > entry.ttlMs) {
            await AsyncStorage.removeItem(key);
            return null;
        }

        return entry.data;
    } catch (error) {
        console.warn(`Failed to read cache ${key}:`, error);
        return null;
    }
};

const clearCache = async (prefix: string): Promise<void> => {
    try {
        const keys = await AsyncStorage.getAllKeys();
        const keysToDelete = keys.filter((k) => k.startsWith(prefix));
        await AsyncStorage.multiRemove(keysToDelete);
    } catch (error) {
        console.warn(`Failed to clear cache with prefix ${prefix}:`, error);
    }
};

// ============================================================================
// Volatility Calculation (52-Week Range)
// ============================================================================

/**
 * Calculate 52-week volatility percentage
 * Formula: (52W High - 52W Low) / Current Price × 100
 *
 * Note: Finnhub metrics endpoint would provide 52W high/low, but we fall back
 * to using high/low price from quote as approximation
 */
export const calculateVolatility = async (
    symbol: string
): Promise<VolatilityData> => {
    const cacheKey = `volatility_cache_${symbol}`;
    const cached = await getCacheEntry<VolatilityData>(cacheKey);
    if (cached) return cached;

    try {
        const quote = await getStockQuote(symbol);

        // Use high/low from quote as 52W approximation
        // In production, you'd call a metrics endpoint for actual 52W data
        const weekHigh52 = quote.h || quote.c;
        const weekLow52 = quote.l || quote.c;
        const currentPrice = quote.c;

        const volatilityPercent =
            ((weekHigh52 - weekLow52) / currentPrice) * 100;

        // Categorize volatility level
        let volatilityLevel: 'Low' | 'Medium' | 'High' | 'Extreme';
        if (volatilityPercent < 15) volatilityLevel = 'Low';
        else if (volatilityPercent < 25) volatilityLevel = 'Medium';
        else if (volatilityPercent < 40) volatilityLevel = 'High';
        else volatilityLevel = 'Extreme';

        const result: VolatilityData = {
            symbol,
            volatilityPercent,
            volatilityLevel,
            weekHigh52,
            weekLow52,
            currentPrice,
        };

        // Cache for 15 minutes
        await setCacheEntry(cacheKey, result, 15 * 60 * 1000);
        return result;
    } catch (error) {
        console.error(`Failed to calculate volatility for ${symbol}:`, error);
        throw error;
    }
};

// ============================================================================
// News Volume Calculation
// ============================================================================

/**
 * Get news volume for a stock in the past 7 days
 * Queries Yahoo Finance-backed news API for sector news, filters by symbol
 */
export const getNewsVolume = async (
    symbol: string,
    sector: string
): Promise<NewsVolumeData> => {
    const cacheKey = `news_volume_cache_${sector}`;
    const cached = await getCacheEntry<NewsVolumeData>(cacheKey);
    if (cached && cached.symbol === symbol) return cached;

    try {
        const news = await getNewsBySectorSafe(sector);

        // Filter articles mentioning the stock symbol (case-insensitive)
        const relevantArticles = news.filter(
            (article) =>
                article.title.toUpperCase().includes(symbol.toUpperCase()) ||
                article.summary?.toUpperCase().includes(symbol.toUpperCase())
        );

        const articleCount = relevantArticles.length;

        // Categorize news volume
        let volumeLevel: 'Low' | 'Moderate' | 'High' | 'Very High' | 'Extreme';
        if (articleCount < 5) volumeLevel = 'Low';
        else if (articleCount < 15) volumeLevel = 'Moderate';
        else if (articleCount < 30) volumeLevel = 'High';
        else if (articleCount < 50) volumeLevel = 'Very High';
        else volumeLevel = 'Extreme';

        const result: NewsVolumeData = {
            symbol,
            sector,
            articleCount,
            volumeLevel,
            timeRange: 'past 7 days',
        };

        // Cache for 10 minutes
        await setCacheEntry(cacheKey, result, 10 * 60 * 1000);
        return result;
    } catch (error) {
        console.error(`Failed to get news volume for ${symbol}:`, error);
        throw error;
    }
};

// ============================================================================
// Scoring Algorithm
// ============================================================================

/**
 * Score and rank candidate stocks based on 4 weighted factors
 * Weights: Volatility 30%, News 30%, Diversification 20%, Correlation 20%
 */
export const scoreAndRankStocks = async (
    candidates: string[],
    holdings: HoldingDTO[]
): Promise<ScoredStock[]> => {
    const scored: ScoredStock[] = [];
    const heldSymbols = new Set(holdings.filter(h => h != null).map((h) => h.stockSymbol.toUpperCase()));

    // Add 60ms delay between requests to respect Finnhub rate limits (60 calls/min)
    const delayBetweenRequests = 60; // ms

    for (let i = 0; i < candidates.length; i++) {
        const symbol = candidates[i];

        // Skip if already owned
        if (heldSymbols.has(symbol.toUpperCase())) {
            continue;
        }

        try {
            // Rate limit friendly requests
            if (i > 0) {
                await new Promise((resolve) =>
                    setTimeout(resolve, delayBetweenRequests)
                );
            }

            // Fetch quote and profile
            const quote = await getStockQuoteSafe(symbol);
            if (!quote) continue;

            const profile = await getCompanyProfileSafe(symbol);
            if (!profile) continue;

            // Calculate metrics
            const volatilityData = await calculateVolatility(symbol);
            const newsVolumeData = await getNewsVolume(symbol, profile.finnhubIndustry);

            // Score each factor (0-100)
            const volatilityScore = Math.min(
                100,
                (volatilityData.volatilityPercent / 50) * 100
            );
            const newsScore = Math.min(
                100,
                (newsVolumeData.articleCount / 50) * 100
            );

            // Diversification: How different is this sector from current holdings?
            const holdingSectors = new Set(holdings.filter(h => h != null).map((h) => h.sector));
            const diversificationScore = holdingSectors.has(profile.finnhubIndustry)
                ? 30
                : 100;


            // Correlation: Do holdings have the same sector?
            const correlatedHoldings = holdings.filter(
                (h) => h != null && h.sector === profile.industry
            );
            const correlationScore =
                correlatedHoldings.length === 0
                    ? 50
                    : Math.max(0, 100 - correlatedHoldings.length * 20);

            // Weighted total score
            const totalScore =
                volatilityScore * 0.3 +
                newsScore * 0.3 +
                diversificationScore * 0.2 +
                correlationScore * 0.2;

            // Determine primary reason (highest scoring factor)
            const scores = [
                { factor: 'High Volatility' as SuggestionReason, score: volatilityScore },
                { factor: 'Trending News' as SuggestionReason, score: newsScore },
                { factor: 'Sector Diversification' as SuggestionReason, score: diversificationScore },
                { factor: 'Correlated Holdings' as SuggestionReason, score: correlationScore },
            ];
            const reason = scores.sort((a, b) => b.score - a.score)[0]
                .factor;

            // Generate insight
            const insight = generateInsight(
                {
                    symbol,
                    companyName: profile.name,
                    sector: profile.finnhubIndustry,
                    currentPrice: quote.c,
                    changePercent: calculateChangePercent(quote.c, quote.pc),
                    volatility: volatilityData,
                    newsVolume: newsVolumeData,
                },
                holdings,
                reason
            );

            scored.push({
                symbol,
                companyName: profile.name,
                sector: profile.finnhubIndustry,
                currentPrice: quote.c,
                changePercent: calculateChangePercent(quote.c, quote.pc),
                volatility: volatilityData,
                newsVolume: newsVolumeData,
                score: totalScore,
                reason,
                insight,
            });
        } catch (error) {
            console.warn(`Failed to score ${symbol}:`, error);
            // Continue with next candidate
        }
    }

    // Sort by score (descending) and return
    return scored.sort((a, b) => b.score - a.score);
};

// ============================================================================
// Main Entry Point
// ============================================================================

/**
 * Generate personalized stock suggestions
 * @param accountId - UUID of the user's account
 * @param limit - Number of suggestions to return (default: 5)
 * @param forceRefresh - Bypass cache and fetch fresh data
 * @returns Array of suggested stocks
 */
export const generateStockSuggestions = async (
    accountId: string,
    limit: number = 5,
    forceRefresh: boolean = false
): Promise<SuggestionStock[]> => {
    const cacheKey = `stock_suggestions_cache_${accountId}`;

    // Check cache first
    if (!forceRefresh) {
        const cached = await getCacheEntry<SuggestionStock[]>(cacheKey);
        if (cached) {
            console.log('📦 Using cached suggestions');
            return cached;
        }
    }

    try {
        console.log(`🔍 Generating suggestions for account ${accountId}`);

        // Fetch user's holdings
        const holdings = await getAccountHoldings(accountId);
        console.log(`📊 User has ${holdings.length} holdings`);

        // Score candidates
        const scored = await scoreAndRankStocks(CANDIDATE_STOCKS, holdings);
        console.log(`✅ Scored ${scored.length} candidates`);

        // Handle edge case: all stocks owned or no scores
        if (scored.length === 0) {
            const result: SuggestionStock[] = [
                {
                    symbol: 'DIVERSIFY',
                    price: 'N/A',
                    change: 'N/A',
                    reason: 'Sector Diversification',
                    icon: 'chart-box-outline',
                    volatility: 'N/A',
                    newsVolume: 'N/A',
                    sector: 'Education',
                    insight: 'Your portfolio is fully diversified! Consider researching new sectors.',
                },
            ];
            await setCacheEntry(cacheKey, result, 5 * 60 * 1000);
            return result;
        }

        // Transform to UI format and apply limit
        const suggestions: SuggestionStock[] = scored.slice(0, limit).map((s) => ({
            symbol: s.symbol,
            price: `$${s.currentPrice.toFixed(2)}`,
            change: `${s.changePercent >= 0 ? '+' : ''}${s.changePercent.toFixed(1)}%`,
            reason: s.reason,
            icon: getReasonIcon(s.reason),
            volatility: `${s.volatility.volatilityLevel} (${s.volatility.volatilityPercent.toFixed(0)}%)`,
            newsVolume: s.newsVolume.volumeLevel,
            sector: s.sector,
            insight: s.insight,
        }));

        // Cache for 5 minutes
        await setCacheEntry(cacheKey, suggestions, 5 * 60 * 1000);
        console.log(`✨ Generated ${suggestions.length} suggestions`);

        return suggestions;
    } catch (error) {
        console.error('Failed to generate suggestions:', error);
        throw error;
    }
};

// ============================================================================
// Helper Functions
// ============================================================================

/**
 * Calculate percentage change from previous close to current price
 */
const calculateChangePercent = (currentPrice: number, previousClosePrice: number): number => {
    if (previousClosePrice === 0) return 0;
    return ((currentPrice - previousClosePrice) / previousClosePrice) * 100;
};

/**
 * Map suggestion reason to Material Community Icon name
 */
const getReasonIcon = (reason: SuggestionReason): string => {
    const iconMap: Record<SuggestionReason, string> = {
        'High Volatility': 'lightning-bolt',
        'Trending News': 'newspaper',
        'Sector Diversification': 'sitemap',
        'Correlated Holdings': 'link-variant',
    };
    return iconMap[reason] || 'star';
};

/**
 * Invalidate suggestions cache (call when holdings change)
 */
export const invalidateSuggestionsCache = async (
    accountId: string
): Promise<void> => {
    const cacheKey = `stock_suggestions_cache_${accountId}`;
    try {
        await AsyncStorage.removeItem(cacheKey);
        console.log('🔄 Cleared suggestions cache');
    } catch (error) {
        console.warn('Failed to invalidate cache:', error);
    }
};

/**
 * Clear all suggestion-related caches (volatility, news, suggestions)
 */
export const clearAllSuggestionCaches = async (): Promise<void> => {
    try {
        await clearCache('volatility_cache_');
        await clearCache('news_volume_cache_');
        await clearCache('stock_suggestions_cache_');
        console.log('🧹 Cleared all suggestion caches');
    } catch (error) {
        console.warn('Failed to clear all caches:', error);
    }
};