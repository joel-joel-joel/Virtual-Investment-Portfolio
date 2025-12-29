import React, { useEffect, useRef, useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
    Animated,
    ActivityIndicator,
    PanResponder,
    GestureResponderEvent,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useTheme } from '@/src/context/ThemeContext';
import { HeaderSection } from "@/src/screens/tabs/home/HeaderSection";
import { Svg, Polyline, Circle, Defs, LinearGradient, Stop, Line, RadialGradient} from 'react-native-svg';
import { getStockQuote, getCompanyProfile, getOrCreateStockBySymbol } from '@/src/services/entityService';
import { addToWatchlist, removeFromWatchlist, isInWatchlist } from '@/src/services';
import type { FinnhubQuoteDTO, FinnhubMetricsDTO, FinnhubCandleDTO } from '@/src/types/api';
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
    '3Y': [80, 85, 90, 95, 100, 110, 120, 130, 140, 150],
    'ALL': [60, 65, 70, 75, 80, 85, 90, 95, 100, 110, 120, 130, 140, 150],
};

interface ChartPoint {
    date: string;
    value: number;
    timestamp: number;
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

interface StockHeaderChartProps {
    stock: Stock;
    selectedTimeframe: '1D' | '1W' | '1M' | '3M' | '1Y' | '3Y' | 'ALL';
    onTimeframeChange: (timeframe: '1D' | '1W' | '1M' | '3M' | '1Y' | '3Y' | 'ALL') => void;
    onTabChange: (tab: 'overview' | 'news' | 'transactions' | 'compare') => void;
}

export const StockHeaderChart = React.memo(({
                                                stock,
                                                selectedTimeframe,
                                                onTimeframeChange,
                                                onTabChange,
                                            }: StockHeaderChartProps) => {
    const navigation = useNavigation();
    const {Colors} = useTheme();

    const [isWatchlisted, setIsWatchlisted] = useState(false);
    const [priceData, setPriceData] = useState(chartDataSets['1M']);
    const [chartPoints, setChartPoints] = useState<ChartPoint[]>([]);
    const [loadingPriceHistory, setLoadingPriceHistory] = useState(false);
    const [useMockData, setUseMockData] = useState(true);
    const [realtimeQuote, setRealtimeQuote] = useState<FinnhubQuoteDTO | null>(null);
    const [loadingRealtimeData, setLoadingRealtimeData] = useState(false);
    const [selectedPointIndex, setSelectedPointIndex] = useState<number | null>(null);
    const [isDragging, setIsDragging] = useState(false);
    const animation = useRef(new Animated.Value(0)).current;
    const [animatedPoints, setAnimatedPoints] = useState<string>("");
    const [progressIndex, setProgressIndex] = useState(0);
    const chartContainerRef = useRef<View>(null);

    // Calculate real-time price data
    const currentPrice = realtimeQuote?.c || stock?.price || 0;
    const previousClose = realtimeQuote?.pc || currentPrice;
    const priceChange = realtimeQuote ? (currentPrice - previousClose) : (stock?.change || 0);
    const priceChangePercent = realtimeQuote && previousClose !== 0 ? (priceChange / previousClose) * 100 : (stock?.changePercent || 0);

    // Check if stock is in watchlist on component mount
    useEffect(() => {
        const checkWatchlistStatus = async () => {
            if (stock?.symbol) {
                try {
                    const dbStock = await getOrCreateStockBySymbol(stock.symbol);
                    const inWatchlist = await isInWatchlist(dbStock.stockId);
                    setIsWatchlisted(inWatchlist);
                } catch (error) {
                    // Handle silently
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
                const quote = await getStockQuote(stock.symbol);
                if (quote && quote.c && quote.pc) {
                    setRealtimeQuote(quote);
                }
            } catch (error) {
                // Handle silently
            } finally {
                setLoadingRealtimeData(false);
            }
        };

        fetchRealtimeQuote();

        // Refresh every 30 seconds
        const interval = setInterval(fetchRealtimeQuote, 30000);
        return () => clearInterval(interval);
    }, [stock?.symbol]);

    // Fetch candle data for chart based on timeframe
    useEffect(() => {
        const fetchCandleData = async () => {
            if (!stock?.symbol) return;

            setLoadingPriceHistory(true);
            setSelectedPointIndex(null);
            setIsDragging(false);
            try {
                const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

                const resolutionMap: Record<string, { resolution: string; days: number }> = {
                    '1D': { resolution: '5', days: 1 },
                    '1W': { resolution: 'D', days: 7 },
                    '1M': { resolution: 'D', days: 30 },
                    '3M': { resolution: 'D', days: 90 },
                    '1Y': { resolution: 'W', days: 365 },
                    '3Y': { resolution: 'W', days: 1095 },
                    'ALL': { resolution: 'M', days: 3650 },
                };

                const config = resolutionMap[selectedTimeframe];
                const to = Math.floor(Date.now() / 1000);
                const from = to - (config.days * 24 * 60 * 60);

                const url = `${apiUrl}/api/stocks/finnhub/candles/${stock.symbol}?resolution=${config.resolution}&from=${from}&to=${to}`;

                const response = await fetch(url);

                if (response.ok) {
                    const data: FinnhubCandleDTO = await response.json();

                    if (data.s === 'ok' && data.c && data.c.length > 0) {
                        const points: ChartPoint[] = data.c.map((price, index) => ({
                            value: Number(price),
                            date: new Date(data.t[index] * 1000).toISOString().split('T')[0],
                            timestamp: data.t[index],
                        }));

                        setChartPoints(points);
                        setPriceData(data.c.map(price => Number(price)));
                        setUseMockData(false);
                    } else {
                        // Use mock data - generate chartPoints from it
                        const mockPrices = chartDataSets[selectedTimeframe];
                        const now = new Date();
                        const generatedPoints: ChartPoint[] = mockPrices.map((price, index) => {
                            const date = new Date(now);
                            date.setDate(date.getDate() - (mockPrices.length - index - 1));
                            return {
                                value: price,
                                date: date.toISOString().split('T')[0],
                                timestamp: Math.floor(date.getTime() / 1000),
                            };
                        });
                        setPriceData(mockPrices);
                        setChartPoints(generatedPoints);
                        setUseMockData(true);
                    }
                } else {
                    // Use mock data on API error
                    const mockPrices = chartDataSets[selectedTimeframe];
                    const now = new Date();
                    const generatedPoints: ChartPoint[] = mockPrices.map((price, index) => {
                        const date = new Date(now);
                        date.setDate(date.getDate() - (mockPrices.length - index - 1));
                        return {
                            value: price,
                            date: date.toISOString().split('T')[0],
                            timestamp: Math.floor(date.getTime() / 1000),
                        };
                    });
                    setPriceData(mockPrices);
                    setChartPoints(generatedPoints);
                    setUseMockData(true);
                }
            } catch (error) {
                // Use mock data on exception
                const mockPrices = chartDataSets[selectedTimeframe];
                const now = new Date();
                const generatedPoints: ChartPoint[] = mockPrices.map((price, index) => {
                    const date = new Date(now);
                    date.setDate(date.getDate() - (mockPrices.length - index - 1));
                    return {
                        value: price,
                        date: date.toISOString().split('T')[0],
                        timestamp: Math.floor(date.getTime() / 1000),
                    };
                });
                setPriceData(mockPrices);
                setChartPoints(generatedPoints);
                setUseMockData(true);
            } finally {
                setLoadingPriceHistory(false);
            }
        };

        fetchCandleData();
    }, [selectedTimeframe, stock?.symbol]);

    // Animation effect
    useEffect(() => {
        if (priceData.length === 0) return;

        const listenerId = animation.addListener(({ value }) => {
            const pointsCount = Math.floor(value * fullPoints.length);
            const pointsString = fullPoints
                .slice(0, pointsCount)
                .map((p) => `${p.x},${p.y}`)
                .join(" ");
            setAnimatedPoints(pointsString);
            setProgressIndex(pointsCount);
        });

        animation.setValue(0);
        Animated.timing(animation, {
            toValue: 1,
            duration: 750,
            useNativeDriver: false,
        }).start();

        return () => {
            animation.removeListener(listenerId);
        };
    }, [priceData]);

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
            // Handle silently
        }
    };

    const handleGoBack = () => {
        navigation.goBack();
    };

    const sectorColor = getSectorColor(stock.sector);
    const timeframes = ['1D', '1W', '1M', '3M', '1Y', '3Y', 'ALL'] as const;
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

    // Handle chart touch/pan
    const handleChartPress = (event: GestureResponderEvent) => {
        const { locationX } = event.nativeEvent;
        const touchX = locationX;

        // Find closest point to touch
        const closestIndex = fullPoints.reduce((closest, point, index) => {
            const distance = Math.abs(point.x - touchX);
            const closestDistance = Math.abs(fullPoints[closest].x - touchX);
            return distance < closestDistance ? index : closest;
        }, 0);

        setSelectedPointIndex(closestIndex);
        setIsDragging(true);
    };

    const handleChartMove = (event: GestureResponderEvent) => {
        if (!isDragging) return;

        const { locationX } = event.nativeEvent;
        const touchX = locationX;

        // Clamp to chart boundaries
        const clampedX = Math.max(chartPadding, Math.min(touchX, chartWidth + chartPadding));

        // Find closest point to touch
        const closestIndex = fullPoints.reduce((closest, point, index) => {
            const distance = Math.abs(point.x - clampedX);
            const closestDistance = Math.abs(fullPoints[closest].x - clampedX);
            return distance < closestDistance ? index : closest;
        }, 0);

        setSelectedPointIndex(closestIndex);
    };

    const handleChartRelease = () => {
        setIsDragging(false);
    };

    const updateTimeframe = (timeframe: '1D' | '1W' | '1M' | '3M' | '1Y' | '3Y' | 'ALL') => {
        setSelectedPointIndex(null);
        onTimeframeChange(timeframe);
    };

    // Vertical line position for dragging indicator
    const selectedPoint = selectedPointIndex !== null ? fullPoints[selectedPointIndex] : null;

    return (
        <>
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
                            { backgroundColor: Colors.card },
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

            {/* Animated Chart Container */}
            <View
                ref={chartContainerRef}
                style={[styles.chartContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                onStartShouldSetResponder={() => true}
                onMoveShouldSetResponder={() => isDragging}
                onResponderGrant={handleChartPress}
                onResponderMove={handleChartMove}
                onResponderRelease={handleChartRelease}
            >
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

                {priceData.length > 0 && (
                    <View>
                        <Svg width={screenWidth} height={chartHeight}>
                            <Defs>
                                <LinearGradient id="stockGradient" x1="0" y1="0" x2="0" y2="1">
                                    <Stop offset="0" stopColor={sectorColor.color} stopOpacity="0.3" />
                                    <Stop offset="1" stopColor={sectorColor.color} stopOpacity="0" />
                                </LinearGradient>

                                {/* Radial gradient for selected point circle */}
                                <RadialGradient id="pointGradient" cx="50%" cy="50%" r="50%">
                                    <Stop offset="0%" stopColor={sectorColor.color} stopOpacity="1" />
                                    <Stop offset="70%" stopColor={sectorColor.color} stopOpacity="0.6" />
                                    <Stop offset="100%" stopColor={sectorColor.color} stopOpacity="0" />
                                </RadialGradient>
                            </Defs>

                            {/* Gradient area under the line */}
                            {animatedPoints ? (
                                <Polyline
                                    points={
                                        animatedPoints +
                                        ` ${chartWidth + chartPadding},${chartHeight - chartPadding} ${chartPadding},${chartHeight - chartPadding}`
                                    }
                                    fill="url(#stockGradient)"
                                    stroke="none"
                                />
                            ) : null}

                            {/* Line on top */}
                            <Polyline
                                points={animatedPoints}
                                fill="none"
                                stroke={sectorColor.color}
                                strokeWidth={3}
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />

                            {/* Vertical indicator line when dragging */}
                            {selectedPoint && (
                                <Line
                                    x1={selectedPoint.x}
                                    y1={chartPadding}
                                    x2={selectedPoint.x}
                                    y2={chartHeight - chartPadding}
                                    stroke={sectorColor.color}
                                    strokeWidth={2}
                                    strokeDasharray="4,4"
                                    opacity={isDragging ? 0.8 : 0.4}
                                />
                            )}

                            {/* Highlight point when dragging */}
                            {selectedPoint && (
                                <>
                                    {/* Outer gradient halo effect */}
                                    <Circle
                                        cx={selectedPoint.x}
                                        cy={selectedPoint.y}
                                        r={10}
                                        fill="url(#pointGradient)"
                                        opacity={0.8}
                                    />

                                    {/* Inner solid core */}
                                    <Circle
                                        cx={selectedPoint.x}
                                        cy={selectedPoint.y}
                                        r={6}
                                        fill={sectorColor.color}
                                        opacity={1}
                                    />
                                </>
                            )}
                        </Svg>
                    </View>
                )}
            </View>

            {/* Tooltip showing selected point data */}
            {selectedPointIndex !== null && chartPoints[selectedPointIndex] && (
                <View style={[styles.tooltipContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <Text style={[styles.tooltipLabel, { color: Colors.text }]}>
                        {new Date(chartPoints[selectedPointIndex].date).toLocaleDateString("en-AU")}
                    </Text>
                    <Text style={[styles.tooltipValue, { color: sectorColor.color }]}>
                        A${chartPoints[selectedPointIndex].value.toLocaleString("en-AU", {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                    })}
                    </Text>
                </View>
            )}

            {/* Tabs */}
            <View style={[styles.tabContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                {(['overview', 'news', 'transactions', 'compare'] as const).map(tab => (
                    <TouchableOpacity
                        key={tab}
                        onPress={() => onTabChange(tab)}
                        style={styles.tab}
                    >
                        <Text
                            style={[
                                styles.tabText,
                                { color: Colors.text, opacity: 0.6 }
                            ]}
                        >
                            {tab === 'transactions' ? 'History' : tab.charAt(0).toUpperCase() + tab.slice(1)}
                        </Text>
                    </TouchableOpacity>
                ))}
            </View>
        </>
    );
});

const styles = StyleSheet.create({
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
        marginLeft: 110,
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
        marginBottom: 12,
        padding: 8,
        position: 'relative',
        minHeight: chartHeight + 16,
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
    tooltipContainer: {
        marginHorizontal: 24,
        marginBottom: 12,
        padding: 12,
        borderRadius: 8,
        borderWidth: 1,
        alignItems: "center",
        gap: 4,
    },
    tooltipLabel: {
        fontSize: 12,
        opacity: 0.7,
        fontWeight: "500",
    },
    tooltipValue: {
        fontSize: 18,
        fontWeight: "bold",
    },
});