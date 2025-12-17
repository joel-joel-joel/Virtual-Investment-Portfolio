import React, { useState, useRef, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
    Animated,
    TextInput,
    ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { getThemeColors } from '@/src/constants/colors';
import { HeaderSection } from "@/src/components/home/HeaderSection";
import TransactionHistory from '@/src/components/transaction/TransactionHistory';
import { Svg, Polyline, Circle, Defs, LinearGradient, Stop } from 'react-native-svg';
import { getPriceHistoryForStock, filterPriceHistoryByTimeRange, type PriceHistoryDTO, addToWatchlist, removeFromWatchlist, isInWatchlist } from '@/src/services';
import { getOrCreateStockBySymbol } from '@/src/services/entityService';
import type { FinnhubCompanyProfileDTO, FinnhubQuoteDTO, FinnhubMetricsDTO, FinnhubCandleDTO } from '@/src/types/api';
import { getSectorColor } from '@/src/services/sectorColorService';

const screenWidth = Dimensions.get('window').width - 48;

const chartHeight = 200;
const chartPadding = 20;

// Mock price data for different timeframes (fallback)
const chartDataSets: Record<string, number[]> = {
    '1D': [145, 147, 146, 148, 150, 149, 151, 150, 152, 151, 150],
    '1W': [140, 142, 145, 143, 147, 149, 150],
    '1M': [130, 135, 138, 142, 145, 148, 150],
    '3M': [120, 125, 130, 135, 140, 145, 150],
    '1Y': [100, 110, 115, 120, 130, 140, 150],
};


// Popular stocks for comparison
const availableStocks = [
    { symbol: 'AAPL', name: 'Apple Inc.', sector: 'Technology' },
    { symbol: 'MSFT', name: 'Microsoft Corporation', sector: 'Technology' },
    { symbol: 'NVDA', name: 'NVIDIA Corporation', sector: 'Semiconductors' },
    { symbol: 'GOOGL', name: 'Alphabet Inc.', sector: 'Technology' },
    { symbol: 'TSLA', name: 'Tesla Inc.', sector: 'Consumer/Tech' },
    { symbol: 'AMD', name: 'Advanced Micro Devices', sector: 'Semiconductors' },
    { symbol: 'META', name: 'Meta Platforms', sector: 'Technology' },
];

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
    profile?: FinnhubCompanyProfileDTO;
}

export default function StockTickerScreen({ route }: { route?: any }) {
    const router = useRouter();
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // All hooks must be called before any early returns
    const [selectedTimeframe, setSelectedTimeframe] = useState<'1D' | '1W' | '1M' | '3M' | '1Y'>('1M');
    const [isWatchlisted, setIsWatchlisted] = useState(false);
    const [activeTab, setActiveTab] = useState<'overview' | 'news' | 'transactions' | 'compare'>('overview');
    const [compareStock, setCompareStock] = useState<CompareStockData | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [showSearchResults, setShowSearchResults] = useState(false);
    const [priceData, setPriceData] = useState(chartDataSets['1M']);
    const [priceHistory, setPriceHistory] = useState<PriceHistoryDTO[]>([]);
    const [loadingPriceHistory, setLoadingPriceHistory] = useState(false);
    const [useMockData, setUseMockData] = useState(true);
    const [searchResults, setSearchResults] = useState<CompareStockData[]>([]);
    const [loadingCompareData, setLoadingCompareData] = useState(false);
    const [realtimeQuote, setRealtimeQuote] = useState<FinnhubQuoteDTO | null>(null);
    const [loadingRealtimeData, setLoadingRealtimeData] = useState(false);
    const [stockMetrics, setStockMetrics] = useState<FinnhubMetricsDTO | null>(null);
    const [loadingMetrics, setLoadingMetrics] = useState(false);
    const animation = useRef(new Animated.Value(0)).current;

    // Get stock data from route params
    const stock = route?.params?.stock;

    // Calculate real-time price data
    const currentPrice = realtimeQuote?.c || stock?.price || 0;
    const previousClose = realtimeQuote?.pc || currentPrice;
    const priceChange = realtimeQuote ? (currentPrice - previousClose) : (stock?.change || 0);
    const priceChangePercent = realtimeQuote && previousClose !== 0 ? (priceChange / previousClose) * 100 : (stock?.changePercent || 0);
    const dayHigh = realtimeQuote?.h || stock?.dayHigh || 0;
    const dayLow = realtimeQuote?.l || stock?.dayLow || 0;

    // Check if stock is in watchlist on component mount
    useEffect(() => {
        const checkWatchlistStatus = async () => {
            if (stock?.symbol) {
                try {
                    const dbStock = await getOrCreateStockBySymbol(stock.symbol);
                    const inWatchlist = await isInWatchlist(dbStock.stockId);
                    setIsWatchlisted(inWatchlist);
                } catch (error) {
                    console.error('Failed to check watchlist status:', error);
                }
            }
        };
        checkWatchlistStatus();
    }, [stock?.symbol]);

    // Fetch real-time quote data
    useEffect(() => {
        const fetchRealtimeQuote = async () => {
            if (!stock?.symbol) return;

            setLoadingRealtimeData(true);
            try {
                const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';
                const quoteResponse = await fetch(`${apiUrl}/api/stocks/finnhub/quote/${stock.symbol}`);

                if (!quoteResponse.ok) {
                    console.warn(`Failed to fetch real-time quote for ${stock.symbol}`);
                    return;
                }

                const quote: FinnhubQuoteDTO = await quoteResponse.json();
                if (quote && quote.c && quote.pc) {
                    setRealtimeQuote(quote);
                }
            } catch (error) {
                console.error(`Error fetching real-time quote for ${stock.symbol}:`, error);
            } finally {
                setLoadingRealtimeData(false);
            }
        };

        fetchRealtimeQuote();

        // Refresh every 30 seconds
        const interval = setInterval(fetchRealtimeQuote, 30000);
        return () => clearInterval(interval);
    }, [stock?.symbol]);

    // Fetch stock metrics (P/E, EPS, Dividend, 52W High/Low, etc.)
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

    // Fetch candle data for chart based on timeframe
    useEffect(() => {
        const fetchCandleData = async () => {
            if (!stock?.symbol) return;

            setLoadingPriceHistory(true);
            try {
                const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

                // Map timeframe to resolution and date range
                const resolutionMap: Record<string, { resolution: string; days: number }> = {
                    '1D': { resolution: '5', days: 1 },
                    '1W': { resolution: 'D', days: 7 },
                    '1M': { resolution: 'D', days: 30 },
                    '3M': { resolution: 'D', days: 90 },
                    '1Y': { resolution: 'W', days: 365 },
                };

                const config = resolutionMap[selectedTimeframe];
                const to = Math.floor(Date.now() / 1000);
                const from = to - (config.days * 24 * 60 * 60);

                const url = `${apiUrl}/api/stocks/finnhub/candles/${stock.symbol}?resolution=${config.resolution}&from=${from}&to=${to}`;
                const response = await fetch(url);

                if (response.ok) {
                    const data: FinnhubCandleDTO = await response.json();
                    if (data.s === 'ok' && data.c && data.c.length > 0) {
                        setPriceData(data.c.map(price => Number(price)));
                        setUseMockData(false);
                    } else {
                        console.warn(`No candle data available for ${stock.symbol}`);
                        setPriceData(chartDataSets[selectedTimeframe]);
                        setUseMockData(true);
                    }
                } else {
                    console.warn(`Failed to fetch candle data for ${stock.symbol}`);
                    setPriceData(chartDataSets[selectedTimeframe]);
                    setUseMockData(true);
                }
            } catch (error) {
                console.error('Error fetching candle data:', error);
                setPriceData(chartDataSets[selectedTimeframe]);
                setUseMockData(true);
            } finally {
                setLoadingPriceHistory(false);
            }
        };

        fetchCandleData();
    }, [selectedTimeframe, stock?.symbol]);


    // Animation effect
    useEffect(() => {
        Animated.timing(animation, {
            toValue: 1,
            duration: 750,
            useNativeDriver: false,
        }).start();

        return () => animation.removeAllListeners();
    }, [priceData, animation]);

    // Fetch real-time data for comparison stock
    const fetchCompareStockData = async (symbol: string): Promise<CompareStockData | null> => {
        try {
            const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

            const profileResponse = await fetch(
                `${apiUrl}/api/stocks/finnhub/profile/${symbol}`
            );
            const quoteResponse = await fetch(
                `${apiUrl}/api/stocks/finnhub/quote/${symbol}`
            );

            if (!profileResponse.ok || !quoteResponse.ok) {
                return null;
            }

            const profile: FinnhubCompanyProfileDTO = await profileResponse.json();
            const quote: FinnhubQuoteDTO = await quoteResponse.json();

            if (!quote || !quote.c || !quote.pc) {
                return null;
            }

            const currentPrice = quote.c || 0;
            const previousClose = quote.pc || currentPrice;
            const change = currentPrice - previousClose;
            const changePercent = previousClose !== 0 ? (change / previousClose) * 100 : 0;

            const marketCap = profile?.marketCapitalization || 0;
            const marketCapFormatted = marketCap >= 1000000
                ? `${(marketCap / 1000000).toFixed(1)}T`
                : marketCap >= 1000
                    ? `${(marketCap / 1000).toFixed(1)}B`
                    : `${marketCap.toFixed(0)}M`;

            return {
                symbol: symbol,
                name: profile?.name || profile?.companyName || symbol,
                sector: profile?.finnhubIndustry || profile?.industry || 'Other',
                price: currentPrice,
                changePercent: changePercent,
                peRatio: 'N/A', // Finnhub doesn't provide P/E, would need different API
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

    // Handle search for comparison stocks
    const handleSearchStocks = async (query: string) => {
        setSearchQuery(query);

        if (!query.trim()) {
            setSearchResults([]);
            return;
        }

        setLoadingCompareData(true);
        try {
            const filtered = availableStocks.filter(s =>
                (s.symbol.toLowerCase().includes(query.toLowerCase()) ||
                    s.name.toLowerCase().includes(query.toLowerCase())) &&
                s.symbol !== stock?.symbol
            );

            // Fetch real-time data for filtered stocks
            const results: CompareStockData[] = [];
            for (const stockOption of filtered) {
                try {
                    const data = await fetchCompareStockData(stockOption.symbol);
                    if (data) {
                        results.push(data);
                    }
                } catch (error) {
                    // Fall back to basic data
                    results.push({
                        symbol: stockOption.symbol,
                        name: stockOption.name,
                        sector: stockOption.sector,
                    });
                }
            }

            setSearchResults(results);
        } catch (error) {
            console.error('Search error:', error);
            setSearchResults([]);
        } finally {
            setLoadingCompareData(false);
        }
    };

    // Handle watchlist toggle
    const handleWatchlistToggle = async () => {
        try {
            const dbStock = await getOrCreateStockBySymbol(stock.symbol);

            if (isWatchlisted) {
                await removeFromWatchlist(dbStock.stockId);
                setIsWatchlisted(false);
            } else {
                await addToWatchlist(dbStock.stockId);
                setIsWatchlisted(true);
            }
        } catch (error) {
            console.error('Failed to toggle watchlist:', error);
        }
    };

    if (!stock) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background }]}>
                <Text style={{ color: Colors.text }}>Stock data not found</Text>
            </View>
        );
    }

    const sectorColor = getSectorColor(stock.sector);
    const timeframes = ['1D', '1W', '1M', '3M', '1Y'] as const;
    const isPositive = priceChangePercent >= 0;

    const chartWidth = screenWidth - chartPadding * 2;
    const usableHeight = chartHeight - chartPadding * 2;
    const minPrice = Math.min(...priceData);
    const maxPrice = Math.max(...priceData);
    const priceRange = maxPrice - minPrice || 1;

    const fullPoints = priceData.map((price, index) => {
        const x = chartPadding + (index / (priceData.length - 1)) * chartWidth;
        const y = chartHeight - chartPadding - ((price - minPrice) / priceRange) * usableHeight;
        return { x, y };
    });

    const updateTimeframe = (timeframe: '1D' | '1W' | '1M' | '3M' | '1Y') => {
        setSelectedTimeframe(timeframe);
        animation.setValue(0);
    };

    const handleGoBack = () => {
        router.back();
    };

    const handleSelectCompareStock = async (stock: CompareStockData) => {
        // Fetch latest data if not already available
        if (!stock.quote) {
            setLoadingCompareData(true);
            const updatedData = await fetchCompareStockData(stock.symbol);
            setLoadingCompareData(false);
            if (updatedData) {
                setCompareStock(updatedData);
            }
        } else {
            setCompareStock(stock);
        }
        setShowSearchResults(false);
        setSearchQuery('');
    };

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            <ScrollView showsVerticalScrollIndicator={false}>
                {/* Header with Back Button */}
                <View style={[styles.topBar, { backgroundColor: Colors.background }]}>
                    <TouchableOpacity
                        onPress={handleGoBack}
                        style={[styles.backButton, { backgroundColor: Colors.card }]}
                    >
                        <MaterialCommunityIcons
                            name="chevron-left"
                            size={28}
                            color={Colors.text}
                        />
                    </TouchableOpacity>
                    <View style={styles.headerSpacer}>
                        <HeaderSection />
                    </View>
                </View>

                {/* Stock Header */}
                <View style={[styles.header, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <View style={styles.headerTop}>
                        <View>
                            <Text style={[styles.stockName, { color: Colors.text }]}>
                                {stock.name}
                            </Text>
                            <Text style={[styles.stockSymbol, { color: sectorColor.color }]}>
                                {stock.symbol}
                            </Text>
                        </View>
                        <TouchableOpacity
                            onPress={handleWatchlistToggle}
                            style={[styles.favoriteButton, { backgroundColor: isWatchlisted ? Colors.tint : Colors.tint + '15' }]}
                        >
                            <MaterialCommunityIcons
                                name={isWatchlisted ? 'heart' : 'heart-outline'}
                                size={20}
                                color={isWatchlisted ? 'white' : Colors.tint}
                            />
                        </TouchableOpacity>
                    </View>

                    <View style={styles.priceSection}>
                        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                            <Text style={[styles.price, { color: Colors.text }]}>
                                A${currentPrice.toFixed(2)}
                            </Text>
                            {loadingRealtimeData && (
                                <ActivityIndicator size="small" color={Colors.tint} />
                            )}
                        </View>
                        <View style={[styles.changeBadge, { backgroundColor: isPositive ? '#E7F5E7' : '#FCE4E4' }]}>
                            <MaterialCommunityIcons
                                name={isPositive ? 'trending-up' : 'trending-down'}
                                size={16}
                                color={isPositive ? '#2E7D32' : '#C62828'}
                            />
                            <Text style={[styles.changeText, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                                {isPositive ? '+' : ''}{priceChange.toFixed(2)} ({isPositive ? '+' : ''}{priceChangePercent.toFixed(2)}%)
                            </Text>
                        </View>
                    </View>

                    <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                        <Text style={[styles.sectorText, { color: sectorColor.color }]}>
                            {stock.sector}
                        </Text>
                    </View>
                </View>

                {/* Timeframe Selector */}
                <View style={styles.timeframeContainer}>
                    {timeframes.map(tf => (
                        <TouchableOpacity
                            key={tf}
                            onPress={() => updateTimeframe(tf)}
                            style={[
                                styles.timeframeButton,
                                selectedTimeframe === tf && { backgroundColor: Colors.tint }
                            ]}
                        >
                            <Text
                                style={[
                                    styles.timeframeText,
                                    selectedTimeframe === tf && { color: 'white', fontWeight: '700' }
                                ]}
                            >
                                {tf}
                            </Text>
                        </TouchableOpacity>
                    ))}
                </View>

                {/* Animated Chart */}
                <View style={[styles.chartContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    {loadingPriceHistory && (
                        <View style={styles.loadingOverlay}>
                            <ActivityIndicator size="large" color={Colors.tint} />
                            <Text style={[styles.loadingText, { color: Colors.text }]}>Loading price history...</Text>
                        </View>
                    )}
                    {!loadingPriceHistory && (
                        <View style={styles.dataSourceBadge}>
                            <MaterialCommunityIcons
                                name={useMockData ? "alert-circle-outline" : "check-circle-outline"}
                                size={14}
                                color={useMockData ? "#F59E0B" : "#10B981"}
                            />
                            <Text style={[styles.dataSourceText, { color: useMockData ? "#F59E0B" : "#10B981" }]}>
                                {useMockData ? "Mock Data" : "Live Data"}
                            </Text>
                        </View>
                    )}
                    <Svg width={screenWidth} height={chartHeight}>
                        <Defs>
                            <LinearGradient id="stockGradient" x1="0" y1="0" x2="0" y2="1">
                                <Stop offset="0" stopColor={sectorColor.color} stopOpacity="0.3" />
                                <Stop offset="1" stopColor={sectorColor.color} stopOpacity="0" />
                            </LinearGradient>
                        </Defs>

                        {/* Gradient area under the line */}
                        <Polyline
                            points={
                                fullPoints.map(p => `${p.x},${p.y}`).join(' ') +
                                ` ${chartWidth + chartPadding},${chartHeight - chartPadding} ${chartPadding},${chartHeight - chartPadding}`
                            }
                            fill="url(#stockGradient)"
                            stroke="none"
                        />

                        {/* Line on top */}
                        <Polyline
                            points={fullPoints.map(p => `${p.x},${p.y}`).join(' ')}
                            fill="none"
                            stroke={sectorColor.color}
                            strokeWidth={3}
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />

                        {/* Data points */}
                        {fullPoints.map((p, index) => (
                            <Circle
                                key={index}
                                cx={p.x}
                                cy={p.y}
                                r={4}
                                fill={sectorColor.color}
                                opacity={0.6}
                            />
                        ))}
                    </Svg>
                </View>

                {/* Tabs */}
                <View style={[styles.tabContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    {(['overview', 'news', 'transactions', 'compare'] as const).map(tab => (
                        <TouchableOpacity
                            key={tab}
                            onPress={() => setActiveTab(tab)}
                            style={[
                                styles.tab,
                                activeTab === tab && { borderBottomColor: Colors.tint, borderBottomWidth: 3 }
                            ]}
                        >
                            <Text
                                style={[
                                    styles.tabText,
                                    { color: activeTab === tab ? Colors.tint : Colors.text, opacity: activeTab === tab ? 1 : 0.6 }
                                ]}
                            >
                                {tab === 'transactions' ? 'History' : tab.charAt(0).toUpperCase() + tab.slice(1)}
                            </Text>
                        </TouchableOpacity>
                    ))}
                </View>

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
                                        value={`A$${dayHigh.toFixed(2)}`}
                                        colors={Colors}
                                    />
                                    <StatItem
                                        label="Day Low"
                                        value={`A$${dayLow.toFixed(2)}`}
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
                                    placeholder="Search stocks to compare..."
                                    placeholderTextColor={Colors.text + '99'}
                                    value={searchQuery}
                                    onChangeText={handleSearchStocks}
                                    onFocus={() => setShowSearchResults(true)}
                                />
                                {searchQuery ? (
                                    <TouchableOpacity onPress={() => setSearchQuery('')}>
                                        <MaterialCommunityIcons name="close-circle" size={20} color={Colors.text} style={{ opacity: 0.6 }} />
                                    </TouchableOpacity>
                                ) : null}
                            </View>

                            {/* Loading Indicator */}
                            {loadingCompareData && (
                                <View style={[styles.searchResults, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <ActivityIndicator size="large" color={Colors.tint} />
                                </View>
                            )}

                            {/* Search Results */}
                            {showSearchResults && searchResults.length > 0 && !loadingCompareData && (
                                <View style={[styles.searchResults, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <ScrollView style={styles.searchResultsList} nestedScrollEnabled>
                                        {searchResults.slice(0, 5).map((result) => {
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
                                                    A${currentPrice.toFixed(2)}
                                                </Text>
                                                <Text style={[styles.comparisonValue, {color: getSectorColor(compareStock.sector).color}]}>
                                                    A${(compareStock.price || 0).toFixed(2)}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Change % */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Change %</Text>
                                            <View style={styles.comparisonValues}>
                                                <View style={[styles.changeIndicator, { backgroundColor: priceChangePercent >= 0 ? '#E7F5E7' : '#FCE4E4' }]}>
                                                    <Text style={[styles.comparisonValue, { color: priceChangePercent >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                        {priceChangePercent >= 0 ? '+' : ''}{priceChangePercent.toFixed(2)}%
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
                        <Text style={styles.actionButtonText}>Buy</Text>
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
    topBar: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 24,
        paddingTop: 12,
        paddingBottom: 8,
    },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
    },
    headerSpacer: {
        flex: 1,
        marginLeft: -32,
        marginTop: 15,
    },
    header: {
        borderWidth: 1,
        borderRadius: 16,
        padding: 16,
        marginHorizontal: 24,
        marginTop: -20,
        marginBottom: 16,
        gap: 12,
    },
    headerTop: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
    },
    stockName: {
        fontSize: 18,
        fontWeight: '800',
        marginBottom: 4,
    },
    stockSymbol: {
        fontSize: 14,
        fontWeight: '700',
    },
    favoriteButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
    },
    priceSection: {
        gap: 8,
    },
    price: {
        fontSize: 28,
        fontWeight: '800',
    },
    changeBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        paddingHorizontal: 10,
        paddingVertical: 6,
        borderRadius: 8,
        alignSelf: 'flex-start',
    },
    changeText: {
        fontSize: 13,
        fontWeight: '700',
    },
    sectorBadge: {
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8,
        alignSelf: 'flex-start',
    },
    sectorText: {
        fontSize: 12,
        fontWeight: '700',
    },
    timeframeContainer: {
        flexDirection: 'row',
        paddingHorizontal: 24,
        marginBottom: 16,
        gap: 8,
    },
    timeframeButton: {
        flex: 1,
        paddingVertical: 8,
        borderRadius: 8,
        alignItems: 'center',
        backgroundColor: '#F0F0F0',
    },
    timeframeText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
    },
    chartContainer: {
        borderWidth: 1,
        borderRadius: 16,
        marginHorizontal: 24,
        marginBottom: 16,
        padding: 8,
        position: 'relative',
    },
    loadingOverlay: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderRadius: 16,
        zIndex: 10,
        gap: 8,
    },
    loadingText: {
        fontSize: 12,
        fontWeight: '600',
    },
    dataSourceBadge: {
        position: 'absolute',
        top: 16,
        right: 16,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 4,
        paddingHorizontal: 8,
        paddingVertical: 4,
        backgroundColor: 'white',
        borderRadius: 6,
        zIndex: 5,
    },
    dataSourceText: {
        fontSize: 10,
        fontWeight: '700',
    },
    tabContainer: {
        flexDirection: 'row',
        marginHorizontal: 24,
        marginBottom: 16,
        borderWidth: 1,
        borderRadius: 12,
        overflow: 'hidden',
    },
    tab: {
        flex: 1,
        paddingVertical: 12,
        alignItems: 'center',
    },
    tabText: {
        fontSize: 13,
        fontWeight: '700',
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
