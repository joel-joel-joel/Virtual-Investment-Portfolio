import React, { useState, useRef, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    TextInput,
    ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { getThemeColors } from '@/src/constants/colors';
import TransactionHistory from '@/src/components/transaction/TransactionHistory';
import { getStockQuote, getCompanyProfile } from '@/src/services/entityService';
import { searchCompaniesByName } from '@/src/services/portfolioService';
import type { FinnhubQuoteDTO, FinnhubMetricsDTO } from '@/src/types/api';
import { getSectorColor } from '@/src/services/sectorColorService';
import { useDebounce } from '@/src/hooks/useDebounce';
import { StockHeaderChart } from './StockHeaderChart';

interface CompareStockData {
    symbol: string;
    name: string;
    sector: string;
    price?: number;
    changePercent?: number;
    peRatio?: string;
    marketCap?: string;
    eps?: string;
    dividend?: string;
    quote?: FinnhubQuoteDTO;
    profile?: any;
}

interface Stock {
    symbol: string;
    name: string;
    price: number;
    change: number;
    changePercent: number;
    sector: string;
    marketCap: string;
    peRatio: string;
    dividend: string;
    dayHigh: number;
    dayLow: number;
    yearHigh: number;
    yearLow: number;
    description: string;
    employees: string;
    founded: string;
    website: string;
    nextEarningsDate: string;
    nextDividendDate: string;
    earningsPerShare: string;
}

/**
 * Enhanced fuzzy search for company names and symbols
 */
const fuzzySearchCompare = (query: string, symbol: string, companyName: string): boolean => {
    const normalizedQuery = query.toLowerCase().trim();
    const normalizedSymbol = symbol.toLowerCase();
    const normalizedCompanyName = companyName.toLowerCase();

    // Exact match or starts with (for ticker codes like AAPL or AAP)
    if (normalizedSymbol.startsWith(normalizedQuery)) {
        return true;
    }

    // Exact word match (e.g., "Bank of America" contains "Bank")
    if (normalizedCompanyName.includes(normalizedQuery)) {
        return true;
    }

    // Multi-word matching (e.g., "bank of america" matches "Bank of America Corp")
    const queryWords = normalizedQuery.split(/\s+/).filter(w => w.length > 0);
    const companyWords = normalizedCompanyName.split(/\s+/).filter(w => w.length > 0);

    const matchedWords = queryWords.filter(qWord =>
        companyWords.some(cWord =>
            cWord.includes(qWord) || qWord.includes(cWord)
        )
    );

    if (matchedWords.length >= Math.ceil(queryWords.length / 2)) {
        return true;
    }

    // Character-by-character fuzzy match
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

    if (performFuzzyMatch(normalizedCompanyName)) {
        return true;
    }

    if (performFuzzyMatch(normalizedSymbol)) {
        return true;
    }

    return false;
};

export default function StockTickerScreen({ route }: { route?: any }) {
    const router = useRouter();
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // State management
    const [selectedTimeframe, setSelectedTimeframe] = useState<'1D' | '1W' | '1M' | '3M' | '1Y'>('1M');
    const [activeTab, setActiveTab] = useState<'overview' | 'news' | 'transactions' | 'compare'>('overview');
    const [compareStock, setCompareStock] = useState<CompareStockData | null>(null);
    const [compareSearchQuery, setCompareSearchQuery] = useState('');
    const debouncedCompareQuery = useDebounce(compareSearchQuery, 300);
    const [searchResults, setSearchResults] = useState<CompareStockData[]>([]);
    const [loadingCompareData, setLoadingCompareData] = useState(false);
    const [showSearchResults, setShowSearchResults] = useState(false);
    const [stockMetrics, setStockMetrics] = useState<FinnhubMetricsDTO | null>(null);
    const [loadingMetrics, setLoadingMetrics] = useState(false);
    const [compareAllStocks, setCompareAllStocks] = useState<CompareStockData[]>([]);
    const [loadingCompareAllStocks, setLoadingCompareAllStocks] = useState(false);

    // Search management refs
    const compareSearchIdRef = useRef(0);
    const compareAbortControllerRef = useRef<AbortController | null>(null);

    // Get stock data from route params
    const stock = route?.params?.stock as Stock;

    if (!stock) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background }]}>
                <Text style={{ color: Colors.text }}>Stock data not found</Text>
            </View>
        );
    }

    // Fetch stock metrics
    useEffect(() => {
        const fetchMetrics = async () => {
            if (!stock?.symbol) return;

            setLoadingMetrics(true);
            try {
                const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';
                const response = await fetch(`${apiUrl}/api/stocks/finnhub/metrics/${stock.symbol}`);

                if (response.ok) {
                    const data: FinnhubMetricsDTO = await response.json();
                    setStockMetrics(data);
                } else {
                    console.warn(`Failed to fetch metrics for ${stock.symbol}`);
                }
            } catch (error) {
                console.error('Error fetching metrics:', error);
            } finally {
                setLoadingMetrics(false);
            }
        };

        fetchMetrics();
    }, [stock?.symbol]);

    /**
     * Fetch real-time data for comparison stock from Finnhub
     */
    const fetchCompareStockData = async (symbol: string): Promise<CompareStockData | null> => {
        try {
            const profile = await getCompanyProfile(symbol);
            const quote = await getStockQuote(symbol);

            if (!quote || !quote.c || !quote.pc) {
                return null;
            }

            const currentPrice = quote.c || 0;
            const previousClose = quote.pc || currentPrice;
            const change = currentPrice - previousClose;
            const changePercent = previousClose !== 0 ? (change / previousClose) * 100 : 0;

            const marketCap = profile?.marketCapitalization || 0;
            const marketCapFormatted = marketCap >= 1000000
                ? `$${(marketCap / 1000000).toFixed(1)}T`
                : marketCap >= 1000
                    ? `$${(marketCap / 1000).toFixed(1)}B`
                    : `$${marketCap.toFixed(0)}M`;

            return {
                symbol: symbol,
                name: profile?.name || profile?.companyName || symbol,
                sector: profile?.finnhubIndustry || profile?.industry || 'Other',
                price: currentPrice,
                changePercent: changePercent,
                peRatio: 'N/A',
                marketCap: marketCapFormatted,
                eps: 'N/A',
                dividend: '0',
                quote: quote,
                profile: profile,
            };
        } catch (error) {
            console.error(`Error fetching compare stock data for ${symbol}:`, error);
            return null;
        }
    };

    /**
     * Load popular stocks for comparison on mount
     */
    const loadComparePopularStocks = async () => {
        setLoadingCompareAllStocks(true);
        try {
            const POPULAR_SYMBOLS = ['MSFT', 'NVDA', 'GOOGL', 'AMZN', 'AMD', 'META'];
            const stocks: CompareStockData[] = [];

            for (const symbol of POPULAR_SYMBOLS) {
                // Skip if it's the current stock
                if (symbol === stock?.symbol) continue;

                try {
                    const data = await fetchCompareStockData(symbol);
                    if (data) {
                        stocks.push(data);
                    }
                } catch (error) {
                    console.warn(`Failed to fetch ${symbol}:`, error);
                }
            }

            setCompareAllStocks(stocks);
        } catch (error) {
            console.error('Failed to load popular stocks:', error);
        } finally {
            setLoadingCompareAllStocks(false);
        }
    };

    /**
     * Perform the actual search with all strategies
     */
    const performCompareSearch = async (
        query: string,
        searchId: number,
        signal: AbortSignal
    ) => {
        console.log(`🔍 performCompareSearch called (searchId: ${searchId}, query: "${query}")`);

        if (!query.trim()) {
            console.log(`  ❌ Empty query, clearing results`);
            setSearchResults([]);
            setLoadingCompareData(false);
            return;
        }

        setLoadingCompareData(true);

        try {
            // Strategy 1: Try fetching as ticker symbol
            console.log(`  [${searchId}] Strategy 1: Trying as ticker symbol "${query.toUpperCase()}"`);
            try {
                const stockData = await fetchCompareStockData(query.toUpperCase());

                if (signal.aborted) {
                    console.log(`  [${searchId}] ❌ Aborted during ticker fetch`);
                    return;
                }

                if (stockData && stockData.symbol !== stock?.symbol) {
                    if (compareSearchIdRef.current === searchId) {
                        console.log(`  [${searchId}] ✅ Found as ticker, updating results`);
                        setSearchResults([stockData]);
                    }
                    return;
                }
            } catch (error) {
                console.warn(`  [${searchId}] Failed to fetch as ticker:`, error);
            }

            // Strategy 2: Fuzzy search on popular stocks
            console.log(`  [${searchId}] Strategy 2: Fuzzy search on popular stocks`);
            const fuzzyResults = compareAllStocks.filter(s =>
                fuzzySearchCompare(query, s.symbol, s.name) && s.symbol !== stock?.symbol
            );

            if (signal.aborted) {
                console.log(`  [${searchId}] ❌ Aborted during fuzzy search`);
                return;
            }

            if (fuzzyResults.length > 0) {
                if (compareSearchIdRef.current === searchId) {
                    console.log(`  [${searchId}] ✅ Found ${fuzzyResults.length} fuzzy matches`);
                    setSearchResults(fuzzyResults.slice(0, 5));
                }
                return;
            }

            // Strategy 3: Search Finnhub by company name
            console.log(`  [${searchId}] Strategy 3: Searching Finnhub by company name`);
            try {
                const finnhubResults = await searchCompaniesByName(query);

                if (signal.aborted) {
                    console.log(`  [${searchId}] ❌ Aborted during Finnhub search`);
                    return;
                }

                if (!Array.isArray(finnhubResults) || finnhubResults.length === 0) {
                    console.log(`  [${searchId}] ❌ No Finnhub results`);
                    if (compareSearchIdRef.current === searchId) {
                        setSearchResults([]);
                    }
                    return;
                }

                // Filter valid symbols and fetch data
                const validSymbols = finnhubResults
                    .map(r => r.symbol)
                    .filter(symbol => {
                        const isValid = /^[A-Z]{1,5}$/.test(symbol) && symbol !== stock?.symbol;
                        return isValid;
                    })
                    .slice(0, 5);

                console.log(`  [${searchId}] Found ${validSymbols.length} valid symbols`);

                if (validSymbols.length === 0) {
                    if (compareSearchIdRef.current === searchId) {
                        setSearchResults([]);
                    }
                    return;
                }

                // Fetch data for valid symbols
                const results: CompareStockData[] = [];

                for (const symbol of validSymbols) {
                    if (signal.aborted) {
                        console.log(`  [${searchId}] ❌ Aborted during data fetch`);
                        return;
                    }

                    try {
                        const data = await fetchCompareStockData(symbol);
                        if (data) {
                            results.push(data);
                        }
                    } catch (error) {
                        console.warn(`  [${searchId}] Failed to fetch ${symbol}:`, error);
                    }
                }

                if (compareSearchIdRef.current === searchId) {
                    console.log(`  [${searchId}] ✅ Found ${results.length} results from Finnhub`);
                    setSearchResults(results);
                }
            } catch (error) {
                console.error(`  [${searchId}] Finnhub search failed:`, error);
                if (compareSearchIdRef.current === searchId) {
                    setSearchResults([]);
                }
            }
        } catch (error) {
            if (signal.aborted) {
                console.log(`  [${searchId}] ❌ Search aborted`);
                return;
            }
            console.error(`  [${searchId}] Search error:`, error);
            if (compareSearchIdRef.current === searchId) {
                setSearchResults([]);
            }
        } finally {
            if (compareSearchIdRef.current === searchId) {
                setLoadingCompareData(false);
            }
        }
    };

    // Load popular stocks when tab changes to compare
    useEffect(() => {
        if (activeTab === 'compare' && compareAllStocks.length === 0) {
            loadComparePopularStocks();
        }
    }, [activeTab]);

    // Handle debounced search query changes
    useEffect(() => {
        console.log(`🔄 Debounced compare query changed: "${debouncedCompareQuery}"`);

        // Cancel any in-flight search
        if (compareAbortControllerRef.current) {
            console.log('  ❌ Aborting previous search');
            compareAbortControllerRef.current.abort();
        }

        // Clear results if query is empty
        if (!debouncedCompareQuery.trim()) {
            console.log('  ✅ Empty query, clearing results');
            setSearchResults([]);
            setLoadingCompareData(false);
            return;
        }

        // Increment search ID to track this search
        compareSearchIdRef.current += 1;
        const currentSearchId = compareSearchIdRef.current;

        // Create new AbortController for this search
        const abortController = new AbortController();
        compareAbortControllerRef.current = abortController;

        console.log(`  🚀 Starting search ${currentSearchId}`);

        // Execute the search
        performCompareSearch(debouncedCompareQuery, currentSearchId, abortController.signal);

        // Cleanup
        return () => {
            console.log(`  🧹 Cleanup for search ${currentSearchId}`);
            abortController.abort();
        };
    }, [debouncedCompareQuery, compareAllStocks, stock?.symbol]);

    // Cleanup on unmount
    useEffect(() => {
        return () => {
            if (compareAbortControllerRef.current) {
                compareAbortControllerRef.current.abort();
            }
        };
    }, []);

    const handleSelectCompareStock = async (selectedStock: CompareStockData) => {
        if (!selectedStock.quote) {
            setLoadingCompareData(true);
            const updatedData = await fetchCompareStockData(selectedStock.symbol);
            setLoadingCompareData(false);
            if (updatedData) {
                setCompareStock(updatedData);
            }
        } else {
            setCompareStock(selectedStock);
        }
        setShowSearchResults(false);
        setCompareSearchQuery('');
    };

    const sectorColor = getSectorColor(stock.sector);

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            <ScrollView showsVerticalScrollIndicator={false}>
                {/* Header and Chart Component */}
                <StockHeaderChart
                    stock={stock}
                    selectedTimeframe={selectedTimeframe}
                    onTimeframeChange={setSelectedTimeframe}
                    onTabChange={setActiveTab}
                />

                {/* Tab Content */}
                <View style={styles.tabContent}>
                    {activeTab === 'overview' && (
                        <View style={styles.statsSection}>
                            <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                Key Statistics
                            </Text>
                            {loadingMetrics ? (
                                <View style={{ paddingVertical: 40, alignItems: 'center' }}>
                                    <ActivityIndicator size="large" color={Colors.tint} />
                                    <Text style={[styles.placeholderText, { color: Colors.text, marginTop: 12 }]}>
                                        Loading metrics...
                                    </Text>
                                </View>
                            ) : (
                                <View style={[styles.statsGrid, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <StatItem
                                        label="P/E Ratio"
                                        value={stockMetrics?.metric?.peExclExtraTTM?.toFixed(2) || 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="EPS"
                                        value={stockMetrics?.metric?.epsExclExtraItemsTTM
                                            ? `A$${stockMetrics.metric.epsExclExtraItemsTTM.toFixed(2)}`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Dividend Yield"
                                        value={stockMetrics?.metric?.dividendYieldIndicatedAnnual
                                            ? `${stockMetrics.metric.dividendYieldIndicatedAnnual.toFixed(2)}%`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Beta"
                                        value={stockMetrics?.metric?.beta?.toFixed(2) || 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Day High"
                                        value={`A$${(stock.dayHigh || 0).toFixed(2)}`}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Day Low"
                                        value={`A$${(stock.dayLow || 0).toFixed(2)}`}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="52W High"
                                        value={stockMetrics?.metric?.['52WeekHigh']
                                            ? `A$${stockMetrics.metric['52WeekHigh'].toFixed(2)}`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="52W Low"
                                        value={stockMetrics?.metric?.['52WeekLow']
                                            ? `A$${stockMetrics.metric['52WeekLow'].toFixed(2)}`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="P/B Ratio"
                                        value={stockMetrics?.metric?.pbQuarterly?.toFixed(2) || 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="ROE"
                                        value={stockMetrics?.metric?.roaeTTM
                                            ? `${stockMetrics.metric.roaeTTM.toFixed(2)}%`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Gross Margin"
                                        value={stockMetrics?.metric?.grossMarginTTM
                                            ? `${stockMetrics.metric.grossMarginTTM.toFixed(2)}%`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Net Margin"
                                        value={stockMetrics?.metric?.netMarginTTM
                                            ? `${stockMetrics.metric.netMarginTTM.toFixed(2)}%`
                                            : 'N/A'}
                                        colors={Colors}
                                    />
                                </View>
                            )}
                        </View>
                    )}

                    {activeTab === 'news' && (
                        <View style={styles.statsSection}>
                            <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                Latest News
                            </Text>
                            <Text style={[styles.placeholderText, { color: Colors.text, opacity: 0.6 }]}>
                                News content will appear here
                            </Text>
                        </View>
                    )}

                    {activeTab === 'transactions' && (
                        <View style={styles.statsSection}>
                            <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                Recent Transactions for {stock.symbol}
                            </Text>
                            <TransactionHistory stockSymbol={stock.symbol} showHeader={false} maxTransactions={5} />
                        </View>
                    )}

                    {activeTab === 'compare' && (
                        <View style={styles.statsSection}>
                            <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                Compare {stock.symbol} with
                            </Text>

                            {/* Search Input */}
                            <View style={[styles.searchContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                <MaterialCommunityIcons name="magnify" size={20} color={Colors.text} style={{ opacity: 0.6 }} />
                                <TextInput
                                    style={[styles.searchInput, { color: Colors.text }]}
                                    placeholder="Search by symbol or company name (e.g. MSFT or Microsoft)..."
                                    placeholderTextColor={Colors.text + '99'}
                                    value={compareSearchQuery}
                                    onChangeText={setCompareSearchQuery}
                                    onFocus={() => setShowSearchResults(true)}
                                />
                                {compareSearchQuery ? (
                                    <TouchableOpacity onPress={() => {
                                        setCompareSearchQuery('');
                                        setSearchResults([]);
                                        setShowSearchResults(false);
                                        setLoadingCompareData(false);
                                    }}>
                                        <MaterialCommunityIcons name="close-circle" size={20} color={Colors.text} style={{ opacity: 0.6 }} />
                                    </TouchableOpacity>
                                ) : null}
                            </View>

                            {/* Loading Indicator */}
                            {loadingCompareData && (
                                <View style={[styles.searchResults, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <View style={{ padding: 20, alignItems: 'center' }}>
                                        <ActivityIndicator size="large" color={Colors.tint} />
                                        <Text style={[styles.placeholderText, { color: Colors.text, marginTop: 12 }]}>
                                            Searching stocks...
                                        </Text>
                                    </View>
                                </View>
                            )}

                            {/* Search Results */}
                            {showSearchResults && searchResults.length > 0 && !loadingCompareData && (
                                <View style={[styles.searchResults, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <ScrollView style={styles.searchResultsList} nestedScrollEnabled>
                                        {searchResults.map((result) => {
                                            const resultSectorColor = getSectorColor(result.sector);
                                            return (
                                                <TouchableOpacity
                                                    key={result.symbol}
                                                    onPress={() => handleSelectCompareStock(result)}
                                                    style={[styles.searchResultItem, { borderBottomColor: Colors.border }]}
                                                >
                                                    <View style={styles.searchResultLeft}>
                                                        <Text style={[styles.searchResultSymbol, { color: resultSectorColor.color }]}>
                                                            {result.symbol}
                                                        </Text>
                                                        <Text style={[styles.searchResultName, { color: Colors.text, opacity: 0.7 }]}>
                                                            {result.name}
                                                        </Text>
                                                    </View>
                                                    <View style={styles.searchResultRight}>
                                                        <Text style={[styles.searchResultPrice, { color: Colors.text }]}>
                                                            A${(result.price || 0).toFixed(2)}
                                                        </Text>
                                                        {result.changePercent !== undefined && (
                                                            <Text style={[styles.searchResultChange, { color: (result.changePercent || 0) >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                                {(result.changePercent || 0) >= 0 ? '+' : ''}{(result.changePercent || 0).toFixed(2)}%
                                                            </Text>
                                                        )}
                                                    </View>
                                                </TouchableOpacity>
                                            );
                                        })}
                                    </ScrollView>
                                </View>
                            )}

                            {/* Show popular stocks when no search */}
                            {!compareSearchQuery && !loadingCompareAllStocks && compareAllStocks.length > 0 && (
                                <View style={[styles.searchResults, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <Text style={[styles.searchResultsTitle, { color: Colors.text, padding: 12, fontSize: 12, fontWeight: '600', opacity: 0.7 }]}>
                                        Popular Stocks
                                    </Text>
                                    <ScrollView style={styles.searchResultsList} nestedScrollEnabled>
                                        {compareAllStocks.map((result) => {
                                            const resultSectorColor = getSectorColor(result.sector);
                                            return (
                                                <TouchableOpacity
                                                    key={result.symbol}
                                                    onPress={() => handleSelectCompareStock(result)}
                                                    style={[styles.searchResultItem, { borderBottomColor: Colors.border }]}
                                                >
                                                    <View style={styles.searchResultLeft}>
                                                        <Text style={[styles.searchResultSymbol, { color: resultSectorColor.color }]}>
                                                            {result.symbol}
                                                        </Text>
                                                        <Text style={[styles.searchResultName, { color: Colors.text, opacity: 0.7 }]}>
                                                            {result.name}
                                                        </Text>
                                                    </View>
                                                    <View style={styles.searchResultRight}>
                                                        <Text style={[styles.searchResultPrice, { color: Colors.text }]}>
                                                            A${(result.price || 0).toFixed(2)}
                                                        </Text>
                                                        {result.changePercent !== undefined && (
                                                            <Text style={[styles.searchResultChange, { color: (result.changePercent || 0) >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                                {(result.changePercent || 0) >= 0 ? '+' : ''}{(result.changePercent || 0).toFixed(2)}%
                                                            </Text>
                                                        )}
                                                    </View>
                                                </TouchableOpacity>
                                            );
                                        })}
                                    </ScrollView>
                                </View>
                            )}

                            {/* Comparison View */}
                            {compareStock ? (
                                <View style={styles.comparisonContainer}>
                                    {/* Header */}
                                    <View style={styles.comparisonHeader}>
                                        <Text style={[styles.comparisonTitle, { color: Colors.text }]}>
                                            {stock.symbol} vs {compareStock.symbol}
                                        </Text>
                                        <TouchableOpacity onPress={() => setCompareStock(null)}>
                                            <MaterialCommunityIcons name="close" size={20} color={Colors.text} />
                                        </TouchableOpacity>
                                    </View>

                                    {/* Comparison Stats */}
                                    <View style={[styles.comparisonStats, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                        {/* Price */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Price</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: sectorColor.color }]}>
                                                    A${(stock.price || 0).toFixed(2)}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: getSectorColor(compareStock.sector).color }]}>
                                                    A${(compareStock.price || 0).toFixed(2)}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Change % */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Change %</Text>
                                            <View style={styles.comparisonValues}>
                                                <View style={[styles.changeIndicator, { backgroundColor: (stock.changePercent || 0) >= 0 ? '#E7F5E7' : '#FCE4E4' }]}>
                                                    <Text style={[styles.comparisonValue, { color: (stock.changePercent || 0) >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                        {(stock.changePercent || 0) >= 0 ? '+' : ''}{(stock.changePercent || 0).toFixed(2)}%
                                                    </Text>
                                                </View>
                                                <View style={[styles.changeIndicator, { backgroundColor: (compareStock.changePercent || 0) >= 0 ? '#E7F5E7' : '#FCE4E4' }]}>
                                                    <Text style={[styles.comparisonValue, { color: (compareStock.changePercent || 0) >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                        {(compareStock.changePercent || 0) >= 0 ? '+' : ''}{(compareStock.changePercent || 0).toFixed(2)}%
                                                    </Text>
                                                </View>
                                            </View>
                                        </View>

                                        {/* P/E Ratio */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>P/E Ratio</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.peRatio || 'N/A'}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {compareStock.peRatio || 'N/A'}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Market Cap */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Market Cap</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.marketCap || 'N/A'}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {compareStock.marketCap || 'N/A'}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Dividend */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Dividend</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.dividend || '0'}%
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {compareStock.dividend || '0'}%
                                                </Text>
                                            </View>
                                        </View>
                                    </View>
                                </View>
                            ) : (
                                <View style={[styles.emptyCompare, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <MaterialCommunityIcons name="chart-line-variant" size={48} color={Colors.text} style={{ opacity: 0.3 }} />
                                    <Text style={[styles.emptyCompareText, { color: Colors.text, opacity: 0.6 }]}>
                                        Search and select a stock to compare with {stock.symbol}
                                    </Text>
                                </View>
                            )}
                        </View>
                    )}
                </View>

                {/* Action Buttons */}
                <View style={styles.actionButtonsContainer}>
                    <TouchableOpacity
                        onPress={() => {
                            router.push({
                                pathname: '/transaction/buy',
                                params: {
                                    stock: JSON.stringify(stock),
                                },
                            });
                        }}
                        style={[styles.actionButton, { backgroundColor: Colors.tint }]}
                    >
                        <MaterialCommunityIcons name="plus" size={18} color="white" />
                        <Text style={styles.actionButtonText}>Invest</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                        onPress={() => {
                            router.push({
                                pathname: '/transaction/sell',
                                params: {
                                    stock: JSON.stringify(stock),
                                    owned: '100',
                                },
                            });
                        }}
                        style={[styles.actionButton, { backgroundColor: '#FCE4E4' }]}
                    >
                        <MaterialCommunityIcons name="minus" size={18} color="#C62828" />
                        <Text style={[styles.actionButtonText, { color: '#C62828' }]}>Sell</Text>
                    </TouchableOpacity>
                </View>

                <View style={{ height: 24 }} />
            </ScrollView>
        </View>
    );
}

const StatItem = ({ label, value, colors }: { label: string; value: string; colors: any }) => (
    <View style={styles.statItem}>
        <Text style={[styles.statLabel, { color: colors.text, opacity: 0.6 }]}>
            {label}
        </Text>
        <Text style={[styles.statValue, { color: colors.text }]}>
            {value}
        </Text>
    </View>
);

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    tabContent: {
        paddingHorizontal: 24,
    },
    statsSection: {
        gap: 12,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '800',
        marginBottom: 4,
    },
    statsGrid: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 16,
    },
    statItem: {
        width: '47%',
        gap: 4,
    },
    statLabel: {
        fontSize: 12,
        fontWeight: '600',
    },
    statValue: {
        fontSize: 16,
        fontWeight: '800',
    },
    placeholderText: {
        fontSize: 13,
        fontWeight: '500',
        textAlign: 'center',
        paddingVertical: 40,
    },
    searchContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        borderWidth: 1,
        borderRadius: 10,
        paddingHorizontal: 12,
        gap: 8,
        marginBottom: 12,
    },
    searchInput: {
        flex: 1,
        fontSize: 14,
        fontWeight: '500',
        paddingVertical: 10,
    },
    searchResults: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
        marginBottom: 16,
        maxHeight: 300,
    },
    searchResultsList: {
        gap: 8,
    },
    searchResultsTitle: {
        borderBottomWidth: 1,
        marginTop: -10,
    },
    searchResultItem: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 12,
        borderBottomWidth: 1,
    },
    searchResultLeft: {
        flex: 1,
        gap: 4,
    },
    searchResultSymbol: {
        fontSize: 14,
        fontWeight: '700',
    },
    searchResultName: {
        fontSize: 12,
        fontWeight: '500',
    },
    searchResultRight: {
        alignItems: 'flex-end',
        gap: 4,
    },
    searchResultPrice: {
        fontSize: 14,
        fontWeight: '700',
    },
    searchResultChange: {
        fontSize: 11,
        fontWeight: '600',
    },
    comparisonContainer: {
        gap: 12,
    },
    comparisonHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginTop: 12,
    },
    comparisonTitle: {
        fontSize: 15,
        fontWeight: '700',
    },
    comparisonStats: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        gap: 12,
    },
    comparisonRow: {
        gap: 8,
    },
    comparisonLabel: {
        fontSize: 12,
        fontWeight: '600',
    },
    comparisonValues: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    comparisonValue: {
        fontSize: 14,
        fontWeight: '700',
    },
    changeIndicator: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
    },
    emptyCompare: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 60,
        alignItems: 'center',
        gap: 12,
        marginTop: 12,
    },
    emptyCompareText: {
        fontSize: 13,
        fontWeight: '500',
        textAlign: 'center',
        paddingHorizontal: 24,
    },
    actionButtonsContainer: {
        flexDirection: 'row',
        paddingHorizontal: 24,
        gap: 12,
        marginTop: 24,
    },
    actionButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 16,
        borderRadius: 12,
    },
    actionButtonText: {
        fontSize: 15,
        fontWeight: '800',
        color: 'white',
    },
});