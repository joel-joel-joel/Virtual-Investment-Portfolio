import { getSectorColor } from '@/src/services/sectorColorService';
import React, { useState, useMemo, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TextInput,
    TouchableOpacity,
    useColorScheme,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '@/src/constants/colors';
import { useRouter } from 'expo-router';
import { addToWatchlist, removeFromWatchlist, getWatchlist, searchCompaniesByName } from '@/src/services/portfolioService';
import { getOrCreateStockBySymbol } from '@/src/services/entityService';
import { FinnhubCompanyProfileDTO, FinnhubQuoteDTO, FinnhubSearchResponseDTO } from '@/src/types/api';
import { useFocusEffect } from '@react-navigation/native';

// Popular stocks to display on initial load
const POPULAR_STOCK_SYMBOLS = ['AAPL', 'MSFT', 'NVDA', 'GOOGL', 'TSLA', 'AMZN', 'AMD', 'META'];

interface Stock {
    id: string;
    symbol: string;
    name: string;
    price: number;
    change: number;
    changePercent: number;
    sector: string;
    marketCap: string;
    profile?: FinnhubCompanyProfileDTO;
    quote?: FinnhubQuoteDTO;
}

interface SearchFilters {
    sector: string | null;
    marketCap: string | null;
}

const sectors = ['Technology', 'Semiconductors', 'FinTech', 'Consumer/Tech', 'Healthcare', 'Retail'];

// ✅ NEW: Enhanced multi-field fuzzy search
const fuzzySearch = (query: string, symbol: string, companyName: string): boolean => {
    const normalizedQuery = query.toLowerCase().trim();
    const normalizedSymbol = symbol.toLowerCase();
    const normalizedCompanyName = companyName.toLowerCase();

    // ===== TICKER SYMBOL MATCHING =====
    // Exact match or starts with (for ticker codes like AAPL or AAP)
    if (normalizedSymbol.startsWith(normalizedQuery)) {
        return true;
    }

    // ===== COMPANY NAME MATCHING =====
    // Exact word match (e.g., "Bank of America" contains "Bank")
    if (normalizedCompanyName.includes(normalizedQuery)) {
        return true;
    }

    // Multi-word matching (e.g., "bank of america" matches "Bank of America Corp")
    const queryWords = normalizedQuery.split(/\s+/).filter(w => w.length > 0);
    const companyWords = normalizedCompanyName.split(/\s+/).filter(w => w.length > 0);

    // Check how many query words match company words (partial or full word match)
    const matchedWords = queryWords.filter(qWord =>
        companyWords.some(cWord =>
            cWord.includes(qWord) || qWord.includes(cWord)
        )
    );

    // If at least 50% of query words match, it's a hit
    if (matchedWords.length >= Math.ceil(queryWords.length / 2)) {
        return true;
    }

    // ===== FUZZY CHARACTER MATCHING =====
    // Character-by-character fuzzy match for both fields
    const performFuzzyMatch = (text: string): boolean => {
        let queryIndex = 0;
        for (let i = 0; i < text.length; i++) {
            if (text[i] === normalizedQuery[queryIndex]) {
                queryIndex++;
                if (queryIndex === normalizedQuery.length) {
                    return true;
                }
            }
        }
        return false;
    };

    // Try fuzzy match on company name first (more important), then symbol
    if (performFuzzyMatch(normalizedCompanyName)) {
        return true;
    }

    if (performFuzzyMatch(normalizedSymbol)) {
        return true;
    }

    return false;
};

const SearchResultCard = ({
                              stock,
                              colors,
                              sectorColor,
                              isWatchlisted,
                              onToggleWatchlist,
                              isTogglingWatchlist,
                          }: {
    stock: Stock;
    colors: any;
    sectorColor: any;
    isWatchlisted: boolean;
    onToggleWatchlist: (symbol: string) => void;
    isTogglingWatchlist: boolean;
}) => {
    const router = useRouter();
    const isPositive = stock.changePercent >= 0;

    const handleNavigateToStock = () => {
        const stockData = {
            symbol: stock.symbol,
            name: stock.name,
            price: stock.price || 0,
            change: stock.change || 0,
            changePercent: stock.changePercent || 0,
            sector: stock.sector,
            marketCap: stock.marketCap,
            peRatio: '0',
            dividend: '0',
            dayHigh: stock.quote?.highPrice || stock.quote?.h || 0,
            dayLow: stock.quote?.lowPrice || stock.quote?.l || 0,
            yearHigh: 0,
            yearLow: 0,
            description: stock.profile?.description || '',
            employees: '',
            founded: '',
            website: stock.profile?.website || stock.profile?.weburl || '',
            nextEarningsDate: '',
            nextDividendDate: '',
            earningsPerShare: '',
        };

        router.push({
            pathname: '/stock/[ticker]',
            params: {
                ticker: stock.symbol,
                stock: JSON.stringify(stockData),
            },
        });
    };

    const handleBuyStock = () => {
        const stockData = {
            symbol: stock.symbol,
            name: stock.name,
            price: stock.price || 0,
            change: stock.change || 0,
            changePercent: stock.changePercent || 0,
            sector: stock.sector,
            marketCap: stock.marketCap,
            peRatio: '0',
            dividend: '0',
            dayHigh: stock.quote?.highPrice || stock.quote?.h || 0,
            dayLow: stock.quote?.lowPrice || stock.quote?.l || 0,
            yearHigh: 0,
            yearLow: 0,
            description: stock.profile?.description || '',
            employees: '',
            founded: '',
            website: stock.profile?.website || stock.profile?.weburl || '',
            nextEarningsDate: '',
            nextDividendDate: '',
            earningsPerShare: '',
        };

        router.push({
            pathname: '/transaction/buy',
            params: {
                stock: JSON.stringify(stockData),
            },
        });
    };

    return (
        <TouchableOpacity
            onPress={handleNavigateToStock}
            style={[
                styles.resultCard,
                {
                    backgroundColor: colors.card,
                    borderColor: colors.border,
                }
            ]}
            activeOpacity={0.7}
        >
            {/* Left Content */}
            <View style={styles.cardLeft}>
                <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                    <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                        {stock.sector.slice(0, 1)}
                    </Text>
                </View>

                <View style={styles.stockInfo}>
                    <Text style={[styles.stockSymbol, { color: sectorColor.color }]}>
                        {stock.symbol}
                    </Text>
                    <Text style={[styles.stockName, { color: colors.text, opacity: 0.7 }]} numberOfLines={1}>
                        {stock.name}
                    </Text>
                    <Text style={[styles.marketCap, { color: colors.text, opacity: 0.5 }]}>
                        {stock.marketCap} market cap
                    </Text>
                </View>
            </View>

            {/* Middle - Price */}
            <View style={styles.cardMiddle}>
                <Text style={[styles.price, { color: colors.text }]}>
                    A${stock.price.toFixed(2)}
                </Text>
                <View style={[styles.changeBadge, { backgroundColor: isPositive ? '#E7F5E7' : '#FCE4E4' }]}>
                    <MaterialCommunityIcons
                        name={isPositive ? 'trending-up' : 'trending-down'}
                        size={12}
                        color={isPositive ? '#2E7D32' : '#C62828'}
                    />
                    <Text style={[styles.changePercent, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                        {isPositive ? '+' : ''}{stock.changePercent.toFixed(2)}%
                    </Text>
                </View>
            </View>

            {/* Right - Actions */}
            <View style={styles.cardRight}>
                <TouchableOpacity
                    onPress={() => onToggleWatchlist(stock.symbol)}
                    style={[
                        styles.actionButton,
                        {
                            backgroundColor: isWatchlisted ? colors.tint : colors.tint + '15',
                            opacity: isTogglingWatchlist ? 0.5 : 1,
                        }
                    ]}
                    activeOpacity={0.6}
                    disabled={isTogglingWatchlist}
                >
                    {isTogglingWatchlist ? (
                        <ActivityIndicator size="small" color={isWatchlisted ? "white" : colors.tint} />
                    ) : (
                        <MaterialCommunityIcons
                            name={isWatchlisted ? "heart" : "heart-outline"}
                            size={18}
                            color={isWatchlisted ? "white" : colors.tint}
                        />
                    )}
                </TouchableOpacity>
                <TouchableOpacity
                    onPress={handleBuyStock}
                    style={[styles.actionButton, { backgroundColor: colors.tint }]}
                    activeOpacity={0.6}
                >
                    <MaterialCommunityIcons
                        name="plus"
                        size={18}
                        color="white"
                    />
                </TouchableOpacity>
            </View>
        </TouchableOpacity>
    );
};

export default function SearchScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const [searchQuery, setSearchQuery] = useState('');
    const [filters, setFilters] = useState<SearchFilters>({
        sector: null,
        marketCap: null,
    });
    const [showFilters, setShowFilters] = useState(false);
    const [recentSearchesList, setRecentSearchesList] = useState<string[]>([]);
    const [watchlistedStocks, setWatchlistedStocks] = useState<Set<string>>(new Set());
    const [allStocks, setAllStocks] = useState<Stock[]>([]);
    const [loadingStocks, setLoadingStocks] = useState(true);
    const [searchResults, setSearchResults] = useState<Stock[]>([]);
    const [loadingSearch, setLoadingSearch] = useState(false);
    const [togglingWatchlistSymbol, setTogglingWatchlistSymbol] = useState<string | null>(null);

    // ✅ NEW: Load recent searches from storage on mount
    useEffect(() => {
        loadRecentSearches();
    }, []);

    const loadRecentSearches = async () => {
        try {
            const result = await window.storage?.get('recent_searches');
            if (result?.value) {
                const searches = JSON.parse(result.value);
                setRecentSearchesList(searches);
            }
        } catch (error) {
            console.error('Failed to load recent searches:', error);
        }
    };

    // ✅ NEW: Save recent searches to storage
    const saveRecentSearches = async (searches: string[]) => {
        try {
            await window.storage?.set('recent_searches', JSON.stringify(searches));
        } catch (error) {
            console.error('Failed to save recent searches:', error);
        }
    };

    // ✅ NEW: Add search to recent searches
    const addToRecentSearches = async (query: string) => {
        const trimmedQuery = query.toUpperCase().trim();
        if (!trimmedQuery) return;

        let updated = [trimmedQuery, ...recentSearchesList.filter(s => s !== trimmedQuery)].slice(0, 10);
        setRecentSearchesList(updated);
        await saveRecentSearches(updated);
    };

    // Load watchlist status
    const loadWatchlistStatus = useCallback(async () => {
        try {
            const watchlistData = await getWatchlist();
            const watchlistedSymbols = new Set(watchlistData.map(item => item.stockCode));
            setWatchlistedStocks(watchlistedSymbols);
        } catch (error) {
            console.error('Failed to load watchlist status:', error);
        }
    }, []);

    // Fetch popular stocks on component mount
    useEffect(() => {
        loadPopularStocks();
        loadWatchlistStatus();
    }, []);

    // Reload watchlist status when screen comes into focus
    useFocusEffect(
        useCallback(() => {
            loadWatchlistStatus();
        }, [loadWatchlistStatus])
    );

    const loadPopularStocks = async () => {
        setLoadingStocks(true);
        try {
            const stocks: Stock[] = [];

            for (const symbol of POPULAR_STOCK_SYMBOLS) {
                try {
                    const stock = await fetchStockData(symbol);
                    if (stock) {
                        stocks.push(stock);
                    }
                } catch (error) {
                    console.error(`Failed to fetch ${symbol}:`, error);
                }
            }

            setAllStocks(stocks);
        } catch (error) {
            console.error('Failed to load popular stocks:', error);
        } finally {
            setLoadingStocks(false);
        }
    };

    const fetchStockData = async (symbol: string): Promise<Stock | null> => {
        try {
            const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

            const profileResponse = await fetch(
                `${apiUrl}/api/stocks/finnhub/profile/${symbol}`
            );
            const quoteResponse = await fetch(
                `${apiUrl}/api/stocks/finnhub/quote/${symbol}`
            );

            if (!profileResponse.ok || !quoteResponse.ok) {
                console.warn(`Failed to fetch data for ${symbol}`);
                return null;
            }

            const profile: FinnhubCompanyProfileDTO = await profileResponse.json();
            const quote: FinnhubQuoteDTO = await quoteResponse.json();

            if (!quote || !quote.c || !quote.pc) {
                console.warn(`Invalid quote data for ${symbol}`);
                return null;
            }

            const currentPrice = quote.c || 0;
            const previousClose = quote.pc || currentPrice;
            const change = currentPrice - previousClose;
            const changePercent = previousClose !== 0 ? (change / previousClose) * 100 : 0;

            return {
                id: symbol,
                symbol: symbol,
                name: profile?.name || profile?.companyName || symbol,
                price: currentPrice,
                change: change,
                changePercent: changePercent,
                sector: profile?.finnhubIndustry || profile?.industry || 'Other',
                marketCap: formatMarketCap(profile?.marketCapitalization || 0),
                profile: profile,
                quote: quote,
            };
        } catch (error) {
            console.error(`Error fetching stock data for ${symbol}:`, error);
            return null;
        }
    };

    const formatMarketCap = (marketCap: number): string => {
        if (marketCap >= 1000000) {
            return `${(marketCap / 1000000).toFixed(1)}T`;
        } else if (marketCap >= 1000) {
            return `${(marketCap / 1000).toFixed(1)}B`;
        }
        return `${marketCap.toFixed(0)}M`;
    };

    // ✅ NEW: Search Finnhub for company by name
    const searchFinnhubByCompanyName = async (companyName: string): Promise<Stock | null> => {
        try {
            // Call backend endpoint to search Finnhub by company name
            const searchResponse: FinnhubSearchResponseDTO = await searchCompaniesByName(companyName);

            // Check if we have results
            if (!searchResponse || !searchResponse.result || searchResponse.result.length === 0) {
                console.warn(`No search results found for company: ${companyName}`);
                return null;
            }

            // Take the first result (best match)
            const bestMatch = searchResponse.result[0];

            // Fetch full stock data for this ticker
            if (bestMatch.symbol) {
                return await fetchStockData(bestMatch.symbol);
            }

            return null;
        } catch (error) {
            console.error('Finnhub company search failed:', error);
            return null;
        }
    };

    // ✅ NEW: Fuzzy search implementation with Finnhub API fallback
    const handleSearch = async (query: string) => {
        setSearchQuery(query);

        if (!query.trim()) {
            setSearchResults([]);
            return;
        }

        setLoadingSearch(true);
        try {
            // Try fetching the query as a stock ticker
            const stock = await fetchStockData(query.toUpperCase());

            if (stock) {
                setSearchResults([stock]);
                await addToRecentSearches(query);
                setLoadingSearch(false);
                return;
            }

            // If not a valid ticker, do fuzzy search on popular stocks
            const fuzzyResults = allStocks.filter(s =>
                fuzzySearch(query, s.symbol, s.name)
            );

            if (fuzzyResults.length > 0) {
                setSearchResults(fuzzyResults);
                await addToRecentSearches(query);
                setLoadingSearch(false);
                return;
            }

            // ✅ NEW: Try searching Finnhub by company name directly
            const finnhubResult = await searchFinnhubByCompanyName(query);
            if (finnhubResult) {
                setSearchResults([finnhubResult]);
                await addToRecentSearches(query);
                setLoadingSearch(false);
                return;
            }

            // Try ticker pattern extraction as fallback
            const companyPattern = query.toUpperCase().trim();
            const tickerPatterns = [
                companyPattern.replace(/\s+/g, ''),
                Array.from(companyPattern).filter(c => c !== ' ').join(''),
                companyPattern.split(/\s+/).map(w => w[0]).join(''),
            ];

            for (const pattern of tickerPatterns) {
                if (pattern.length > 0 && pattern.length <= 4) {
                    const result = await fetchStockData(pattern);
                    if (result) {
                        setSearchResults([result]);
                        await addToRecentSearches(query);
                        setLoadingSearch(false);
                        return;
                    }
                }
            }

            // No results found
            setSearchResults([]);
        } catch (error) {
            console.error('Search failed:', error);
            setSearchResults([]);
        } finally {
            setLoadingSearch(false);
        }
    };

    // Filter and search stocks
    const filteredStocks = useMemo(() => {
        let results = searchResults.length > 0 ? searchResults : allStocks;

        // Filter by sector
        if (filters.sector) {
            results = results.filter(stock => stock.sector === filters.sector);
        }

        return results;
    }, [searchResults, filters, allStocks]);

    const handleClearSearch = () => {
        setSearchQuery('');
        setSearchResults([]);
        setFilters({ sector: null, marketCap: null });
    };

    // ✅ NEW: Clear recent searches
    const handleClearRecentSearches = async () => {
        Alert.alert(
            'Clear Recent Searches',
            'Are you sure you want to clear all recent searches?',
            [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Clear',
                    onPress: async () => {
                        setRecentSearchesList([]);
                        await saveRecentSearches([]);
                    },
                    style: 'destructive',
                },
            ]
        );
    };

    // ✅ NEW: Handle recent search tap
    const handleRecentSearch = async (query: string) => {
        await handleSearch(query);
    };

    const toggleWatchlist = async (symbol: string) => {
        if (togglingWatchlistSymbol) return;

        try {
            setTogglingWatchlistSymbol(symbol);

            const dbStock = await getOrCreateStockBySymbol(symbol);
            const isCurrentlyWatchlisted = watchlistedStocks.has(symbol);

            if (isCurrentlyWatchlisted) {
                await removeFromWatchlist(dbStock.stockId);
                setWatchlistedStocks(prev => {
                    const newSet = new Set(prev);
                    newSet.delete(symbol);
                    return newSet;
                });
            } else {
                await addToWatchlist(dbStock.stockId);
                setWatchlistedStocks(prev => {
                    const newSet = new Set(prev);
                    newSet.add(symbol);
                    return newSet;
                });
            }
        } catch (error: any) {
            console.error('Failed to toggle watchlist:', error);

            let errorMessage = 'Failed to update watchlist. Please try again.';

            if (error?.message) {
                if (error.message.includes('already in watchlist')) {
                    setWatchlistedStocks(prev => {
                        const newSet = new Set(prev);
                        newSet.add(symbol);
                        return newSet;
                    });
                    return;
                } else if (error.message.includes('Unauthorized')) {
                    errorMessage = 'Your session has expired. Please log in again.';
                } else {
                    errorMessage = error.message;
                }
            }

            Alert.alert('Error', errorMessage);
        } finally {
            setTogglingWatchlistSymbol(null);
        }
    };

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: Colors.text }]}>
                    Search Stocks
                </Text>
                <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                    Find and invest in stocks
                </Text>
            </View>

            {/* Search Bar */}
            <View style={styles.searchContainer}>
                <View
                    style={[
                        styles.searchInputWrapper,
                        {
                            backgroundColor: Colors.card,
                            borderColor: Colors.border,
                        }
                    ]}
                >
                    <MaterialCommunityIcons
                        name="magnify"
                        size={20}
                        color={Colors.text}
                        style={{ opacity: 0.6 }}
                    />
                    <TextInput
                        style={[styles.searchInput, { color: Colors.text }]}
                        placeholder="Search by symbol or name (e.g. AAPL or Apple)..."
                        placeholderTextColor={Colors.text + '99'}
                        value={searchQuery}
                        onChangeText={handleSearch}
                    />
                    {searchQuery ? (
                        <TouchableOpacity onPress={() => handleSearch('')}>
                            <MaterialCommunityIcons
                                name="close-circle"
                                size={20}
                                color={Colors.text}
                                style={{ opacity: 0.6 }}
                            />
                        </TouchableOpacity>
                    ) : null}
                </View>

                <TouchableOpacity
                    onPress={() => setShowFilters(!showFilters)}
                    style={[
                        styles.filterButton,
                        {
                            backgroundColor: Colors.card,
                            borderColor: Colors.border,
                            borderWidth: showFilters ? 2 : 1,
                        },
                        showFilters && { borderColor: Colors.tint }
                    ]}
                >
                    <MaterialCommunityIcons
                        name="tune"
                        size={20}
                        color={showFilters ? Colors.tint : Colors.text}
                    />
                </TouchableOpacity>
            </View>

            {/* Filters */}
            {showFilters && (
                <View style={[styles.filterPanel, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <View>
                        <Text style={[styles.filterTitle, { color: Colors.text }]}>
                            Sector
                        </Text>
                        <ScrollView
                            horizontal
                            showsHorizontalScrollIndicator={false}
                            style={styles.filterOptions}
                            contentContainerStyle={styles.filterOptionsContent}
                        >
                            {sectors.map(sector => (
                                <TouchableOpacity
                                    key={sector}
                                    onPress={() =>
                                        setFilters(prev => ({
                                            ...prev,
                                            sector: prev.sector === sector ? null : sector,
                                        }))
                                    }
                                    style={[
                                        styles.filterOption,
                                        filters.sector === sector && {
                                            backgroundColor: Colors.tint,
                                        },
                                        filters.sector !== sector && {
                                            backgroundColor: Colors.background,
                                            borderWidth: 1,
                                            borderColor: Colors.border,
                                        }
                                    ]}
                                >
                                    <Text
                                        style={[
                                            styles.filterOptionText,
                                            filters.sector === sector && { color: 'white', fontWeight: '700' },
                                            filters.sector !== sector && { color: Colors.text, opacity: 0.7 }
                                        ]}
                                    >
                                        {sector}
                                    </Text>
                                </TouchableOpacity>
                            ))}
                        </ScrollView>
                    </View>
                </View>
            )}

            {/* Results or Empty State */}
            {loadingStocks && !searchQuery ? (
                <View style={styles.loadingContainer}>
                    <ActivityIndicator size="large" color={Colors.tint} />
                    <Text style={[styles.loadingText, { color: Colors.text }]}>
                        Loading popular stocks...
                    </Text>
                </View>
            ) : !searchQuery && filteredStocks.length === allStocks.length ? (
                // Recent Searches & Popular Stocks
                <ScrollView
                    style={styles.content}
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={styles.contentContainer}
                >
                    {/* Recent Searches Section */}
                    {recentSearchesList.length > 0 && (
                        <View style={styles.section}>
                            <View style={styles.sectionHeader}>
                                <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                    Recent Searches
                                </Text>
                                <TouchableOpacity onPress={handleClearRecentSearches}>
                                    <Text style={[styles.clearButton, { color: Colors.tint }]}>
                                        Clear
                                    </Text>
                                </TouchableOpacity>
                            </View>
                            <View style={styles.recentSearches}>
                                {recentSearchesList.map(query => (
                                    <TouchableOpacity
                                        key={query}
                                        onPress={() => handleRecentSearch(query)}
                                        style={[
                                            styles.recentSearchTag,
                                            { backgroundColor: Colors.card, borderColor: Colors.border }
                                        ]}
                                    >
                                        <MaterialCommunityIcons
                                            name="clock-outline"
                                            size={14}
                                            color={Colors.tint}
                                        />
                                        <Text style={[styles.recentSearchText, { color: Colors.text }]}>
                                            {query}
                                        </Text>
                                    </TouchableOpacity>
                                ))}
                            </View>
                        </View>
                    )}

                    {/* Popular Stocks */}
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                            Popular Stocks
                        </Text>
                        {allStocks.slice(0, 5).map(stock => {
                            const sectorColor = getSectorColor(stock.sector);
                            return (
                                <SearchResultCard
                                    key={stock.id}
                                    stock={stock}
                                    colors={Colors}
                                    sectorColor={sectorColor}
                                    isWatchlisted={watchlistedStocks.has(stock.symbol)}
                                    onToggleWatchlist={toggleWatchlist}
                                    isTogglingWatchlist={togglingWatchlistSymbol === stock.symbol}
                                />
                            );
                        })}
                    </View>
                </ScrollView>
            ) : loadingSearch ? (
                <View style={styles.loadingContainer}>
                    <ActivityIndicator size="large" color={Colors.tint} />
                </View>
            ) : filteredStocks.length > 0 ? (
                // Search Results
                <ScrollView
                    style={styles.content}
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={styles.contentContainer}
                >
                    <View style={styles.resultCountContainer}>
                        <Text style={[styles.resultCount, { color: Colors.text, opacity: 0.7 }]}>
                            Found {filteredStocks.length} result{filteredStocks.length !== 1 ? 's' : ''}
                        </Text>
                    </View>
                    {filteredStocks.map(stock => {
                        const sectorColor = getSectorColor(stock.sector);
                        return (
                            <SearchResultCard
                                key={stock.id}
                                stock={stock}
                                colors={Colors}
                                sectorColor={sectorColor}
                                isWatchlisted={watchlistedStocks.has(stock.symbol)}
                                onToggleWatchlist={toggleWatchlist}
                                isTogglingWatchlist={togglingWatchlistSymbol === stock.symbol}
                            />
                        );
                    })}
                </ScrollView>
            ) : (
                // No Results
                <View style={styles.emptyState}>
                    <MaterialCommunityIcons
                        name="magnify-close"
                        size={56}
                        color={Colors.text}
                        style={{ opacity: 0.3, marginBottom: 16 }}
                    />
                    <Text style={[styles.emptyStateTitle, { color: Colors.text }]}>
                        No stocks found
                    </Text>
                    <Text style={[styles.emptyStateSubtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Try searching with a different ticker symbol or company name
                    </Text>
                    <TouchableOpacity
                        onPress={handleClearSearch}
                        style={[styles.clearButton, { marginTop: 16 }]}
                    >
                        <Text style={{ color: Colors.tint, fontWeight: '700' }}>Clear search</Text>
                    </TouchableOpacity>
                </View>
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginTop: -40,
    },
    header: {
        paddingHorizontal: 12,
        paddingVertical: 16,
    },
    title: {
        fontSize: 28,
        fontWeight: '800',
        fontStyle: 'italic',
        marginBottom: 4,
    },
    subtitle: {
        fontSize: 14,
    },
    searchContainer: {
        marginBottom: 16,
        flexDirection: 'row',
        gap: 10,
    },
    searchInputWrapper: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 12,
        borderRadius: 10,
        borderWidth: 1,
        gap: 8,
    },
    searchInput: {
        flex: 1,
        fontSize: 14,
        fontWeight: '500',
        paddingVertical: 10,
    },
    filterButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
    },
    filterPanel: {
        marginBottom: 16,
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
    },
    filterTitle: {
        fontSize: 12,
        fontWeight: '700',
        marginBottom: 10,
        opacity: 0.7,
    },
    filterOptions: {
        marginHorizontal: -12,
    },
    filterOptionsContent: {
        gap: 8,
    },
    filterOption: {
        paddingVertical: 10,
        paddingHorizontal: 12,
        borderRadius: 8,
    },
    filterOptionText: {
        fontSize: 11,
        fontWeight: '600',
    },
    content: {
        flex: 1,
    },
    contentContainer: {
        paddingBottom: 24,
        gap: 20,
    },
    section: {
        gap: 12,
    },
    sectionHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '700',
    },
    clearButton: {
        fontSize: 12,
        fontWeight: '600',
    },
    recentSearches: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 8,
    },
    recentSearchTag: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 20,
        borderWidth: 1,
    },
    recentSearchText: {
        fontSize: 12,
        fontWeight: '600',
    },
    resultCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        marginBottom: 8,
    },
    cardLeft: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 10,
    },
    sectorBadge: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
    },
    sectorBadgeText: {
        fontSize: 14,
        fontWeight: '700',
    },
    stockInfo: {
        flex: 1,
    },
    stockSymbol: {
        fontSize: 13,
        fontWeight: '700',
        marginBottom: 2,
    },
    stockName: {
        fontSize: 11,
        fontWeight: '500',
        marginBottom: 2,
    },
    marketCap: {
        fontSize: 10,
        fontWeight: '500',
    },
    cardMiddle: {
        alignItems: 'flex-end',
        gap: 4,
    },
    price: {
        fontSize: 13,
        fontWeight: '700',
    },
    changeBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 3,
        paddingHorizontal: 6,
        paddingVertical: 3,
        borderRadius: 6,
    },
    changePercent: {
        fontSize: 11,
        fontWeight: '700',
    },
    cardRight: {
        flexDirection: 'row',
        gap: 8,
    },
    actionButton: {
        width: 36,
        height: 36,
        borderRadius: 8,
        alignItems: 'center',
        justifyContent: 'center',
    },
    resultCountContainer: {
        marginBottom: 8,
    },
    resultCount: {
        fontSize: 12,
        fontWeight: '600',
    },
    emptyState: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingHorizontal: 24,
    },
    emptyStateTitle: {
        fontSize: 18,
        fontWeight: '700',
        marginBottom: 8,
    },
    emptyStateSubtitle: {
        fontSize: 13,
        textAlign: 'center',
    },
    loadingContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        gap: 12,
    },
    loadingText: {
        fontSize: 14,
        fontWeight: '500',
    },
})