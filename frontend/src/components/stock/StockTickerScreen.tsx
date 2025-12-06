import React, { useState, useRef, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
    Alert,
    Animated,
    TextInput,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { getThemeColors } from '@/src/constants/colors';
import { HeaderSection } from "@/src/components/home/HeaderSection";
import TransactionHistory from '@/src/components/transaction/TransactionHistory';
import { Svg, Polyline, Circle, Defs, LinearGradient, Stop } from 'react-native-svg';

const screenWidth = Dimensions.get('window').width - 48;

const chartHeight = 200;
const chartPadding = 20;

// Mock price data for different timeframes
const chartDataSets: Record<string, number[]> = {
    '1D': [145, 147, 146, 148, 150, 149, 151, 150, 152, 151, 150],
    '1W': [140, 142, 145, 143, 147, 149, 150],
    '1M': [130, 135, 138, 142, 145, 148, 150],
    '3M': [120, 125, 130, 135, 140, 145, 150],
    '1Y': [100, 110, 115, 120, 130, 140, 150],
};

const sectorColors: Record<string, { color: string; bgLight: string }> = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Retail': { color: '#EA580C', bgLight: '#FFEDD5' },
};

// Mock stocks for comparison
const availableStocks = [
    { symbol: 'AAPL', name: 'Apple Inc.', price: 195.50, changePercent: 1.30, sector: 'Technology', peRatio: '28.5', marketCap: '3.0T', eps: '$6.85', dividend: '0.5' },
    { symbol: 'MSFT', name: 'Microsoft Corporation', price: 380.50, changePercent: 1.40, sector: 'Technology', peRatio: '35.2', marketCap: '2.8T', eps: '$10.81', dividend: '0.8' },
    { symbol: 'NVDA', name: 'NVIDIA Corporation', price: 892.50, changePercent: 2.92, sector: 'Semiconductors', peRatio: '75.3', marketCap: '2.2T', eps: '$11.85', dividend: '0.04' },
    { symbol: 'GOOGL', name: 'Alphabet Inc.', price: 140.75, changePercent: 1.52, sector: 'Technology', peRatio: '25.8', marketCap: '1.8T', eps: '$5.45', dividend: '0' },
    { symbol: 'TSLA', name: 'Tesla Inc.', price: 245.30, changePercent: -1.41, sector: 'Consumer/Tech', peRatio: '65.2', marketCap: '780B', eps: '$3.76', dividend: '0' },
    { symbol: 'AMD', name: 'Advanced Micro Devices', price: 165.45, changePercent: 2.79, sector: 'Semiconductors', peRatio: '145.6', marketCap: '268B', eps: '$1.14', dividend: '0' },
    { symbol: 'META', name: 'Meta Platforms', price: 480.25, changePercent: 1.86, sector: 'Technology', peRatio: '29.4', marketCap: '1.2T', eps: '$16.33', dividend: '0.5' },
];

export default function StockTickerScreen({ route }: { route?: any }) {
    const router = useRouter();
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Get stock data from route params
    const stock = route?.params?.stock;

    if (!stock) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background }]}>
                <Text style={{ color: Colors.text }}>Stock data not found</Text>
            </View>
        );
    }

    const sectorColor = sectorColors[stock.sector] || sectorColors['Technology'];
    const [selectedTimeframe, setSelectedTimeframe] = useState('1M');
    const [isWatchlisted, setIsWatchlisted] = useState(false);
    const [activeTab, setActiveTab] = useState<'overview' | 'news' | 'transactions' | 'compare'>('overview');

    // Comparison state
    const [compareStock, setCompareStock] = useState<any>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [showSearchResults, setShowSearchResults] = useState(false);

    const timeframes = ['1D', '1W', '1M', '3M', '1Y'];
    const isPositive = stock.changePercent >= 0;

    // Filter stocks for search
    const searchResults = searchQuery
        ? availableStocks.filter(s =>
            s.symbol.toLowerCase().includes(searchQuery.toLowerCase()) ||
            s.name.toLowerCase().includes(searchQuery.toLowerCase())
        ).filter(s => s.symbol !== stock.symbol)
        : availableStocks.filter(s => s.symbol !== stock.symbol);

    // Chart animation state
    const [priceData, setPriceData] = useState(chartDataSets[selectedTimeframe]);
    const animation = useRef(new Animated.Value(0)).current;
    const [animatedPoints, setAnimatedPoints] = useState<string>('');
    const [progressIndex, setProgressIndex] = useState(0);

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

    const generatePointsString = (progress: number) => {
        const count = Math.floor(progress * fullPoints.length);
        if (count === 0) return '';
        return fullPoints
            .slice(0, count)
            .map((p) => `${p.x},${p.y}`)
            .join(' ');
    };

    const updateTimeframe = (timeframe: string) => {
        setSelectedTimeframe(timeframe);
        setPriceData(chartDataSets[timeframe]);
        animation.setValue(0);
    };

    useEffect(() => {
        const id = animation.addListener(({ value }) => {
            setAnimatedPoints(generatePointsString(value));
            setProgressIndex(Math.floor(value * fullPoints.length));
        });

        Animated.timing(animation, {
            toValue: 1,
            duration: 750,
            useNativeDriver: false,
        }).start();

        return () => animation.removeAllListeners();
    }, [priceData]);

    const handleGoBack = () => {
        router.back();
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
                            onPress={() => setIsWatchlisted(!isWatchlisted)}
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
                        <Text style={[styles.price, { color: Colors.text }]}>
                            A${stock.price.toFixed(2)}
                        </Text>
                        <View style={[styles.changeBadge, { backgroundColor: isPositive ? '#E7F5E7' : '#FCE4E4' }]}>
                            <MaterialCommunityIcons
                                name={isPositive ? 'trending-up' : 'trending-down'}
                                size={16}
                                color={isPositive ? '#2E7D32' : '#C62828'}
                            />
                            <Text style={[styles.changeText, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                                {isPositive ? '+' : ''}{stock.change.toFixed(2)} ({isPositive ? '+' : ''}{stock.changePercent.toFixed(2)}%)
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
                    <Svg width={screenWidth} height={chartHeight}>
                        <Defs>
                            <LinearGradient id="stockGradient" x1="0" y1="0" x2="0" y2="1">
                                <Stop offset="0" stopColor={sectorColor.color} stopOpacity="0.3" />
                                <Stop offset="1" stopColor={sectorColor.color} stopOpacity="0" />
                            </LinearGradient>
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

                        {/* Data points */}
                        {fullPoints.map((p, index) => {
                            if (index >= progressIndex) return null;
                            return (
                                <Circle
                                    key={index}
                                    cx={p.x}
                                    cy={p.y}
                                    r={4}
                                    fill={sectorColor.color}
                                    opacity={0.6}
                                />
                            );
                        })}
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
                            <View style={[styles.statsGrid, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                <StatItem label="P/E Ratio" value={stock.peRatio} colors={Colors} />
                                <StatItem label="Market Cap" value={stock.marketCap} colors={Colors} />
                                <StatItem label="Dividend Yield" value={stock.dividend + '%'} colors={Colors} />
                                <StatItem label="EPS" value={stock.earningsPerShare || 'N/A'} colors={Colors} />
                                <StatItem label="Day High" value={`A$${stock.dayHigh.toFixed(2)}`} colors={Colors} />
                                <StatItem label="Day Low" value={`A$${stock.dayLow.toFixed(2)}`} colors={Colors} />
                                <StatItem label="52W High" value={`A$${stock.yearHigh.toFixed(2)}`} colors={Colors} />
                                <StatItem label="52W Low" value={`A$${stock.yearLow.toFixed(2)}`} colors={Colors} />
                            </View>
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
                                Transaction History for {stock.symbol}
                            </Text>
                            <TransactionHistory stockSymbol={stock.symbol} showHeader={false} />
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
                                    onChangeText={setSearchQuery}
                                    onFocus={() => setShowSearchResults(true)}
                                />
                                {searchQuery ? (
                                    <TouchableOpacity onPress={() => setSearchQuery('')}>
                                        <MaterialCommunityIcons name="close-circle" size={20} color={Colors.text} style={{ opacity: 0.6 }} />
                                    </TouchableOpacity>
                                ) : null}
                            </View>

                            {/* Search Results */}
                            {showSearchResults && searchResults.length > 0 && (
                                <View style={[styles.searchResults, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                    <ScrollView style={styles.searchResultsList} nestedScrollEnabled>
                                        {searchResults.slice(0, 5).map((result) => {
                                            const resultSectorColor = sectorColors[result.sector as keyof typeof sectorColors] || sectorColors['Technology'];
                                            return (
                                                <TouchableOpacity
                                                    key={result.symbol}
                                                    onPress={() => {
                                                        setCompareStock(result);
                                                        setShowSearchResults(false);
                                                        setSearchQuery('');
                                                    }}
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
                                                    <Text style={[styles.searchResultPrice, { color: Colors.text }]}>
                                                        A${result.price.toFixed(2)}
                                                    </Text>
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
                                                    A${stock.price.toFixed(2)}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: sectorColors[compareStock.sector as keyof typeof sectorColors]?.color || Colors.text }]}>
                                                    A${compareStock.price.toFixed(2)}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Change % */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Change %</Text>
                                            <View style={styles.comparisonValues}>
                                                <View style={[styles.changeIndicator, { backgroundColor: stock.changePercent >= 0 ? '#E7F5E7' : '#FCE4E4' }]}>
                                                    <Text style={[styles.comparisonValue, { color: stock.changePercent >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                        {stock.changePercent >= 0 ? '+' : ''}{stock.changePercent.toFixed(2)}%
                                                    </Text>
                                                </View>
                                                <View style={[styles.changeIndicator, { backgroundColor: compareStock.changePercent >= 0 ? '#E7F5E7' : '#FCE4E4' }]}>
                                                    <Text style={[styles.comparisonValue, { color: compareStock.changePercent >= 0 ? '#2E7D32' : '#C62828' }]}>
                                                        {compareStock.changePercent >= 0 ? '+' : ''}{compareStock.changePercent.toFixed(2)}%
                                                    </Text>
                                                </View>
                                            </View>
                                        </View>

                                        {/* P/E Ratio */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>P/E Ratio</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.peRatio || compareStock.peRatio}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text, fontWeight: '800' }]}>
                                                    {compareStock.peRatio}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Market Cap */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Market Cap</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.marketCap || compareStock.marketCap}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text, fontWeight: '800' }]}>
                                                    {compareStock.marketCap}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* EPS */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>EPS</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.earningsPerShare || compareStock.eps}
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text, fontWeight: '800' }]}>
                                                    {compareStock.eps}
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Dividend */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Dividend %</Text>
                                            <View style={styles.comparisonValues}>
                                                <Text style={[styles.comparisonValue, { color: Colors.text }]}>
                                                    {stock.dividend || compareStock.dividend}%
                                                </Text>
                                                <Text style={[styles.comparisonValue, { color: Colors.text, fontWeight: '800' }]}>
                                                    {compareStock.dividend}%
                                                </Text>
                                            </View>
                                        </View>

                                        {/* Sector */}
                                        <View style={styles.comparisonRow}>
                                            <Text style={[styles.comparisonLabel, { color: Colors.text, opacity: 0.7 }]}>Sector</Text>
                                            <View style={styles.comparisonValues}>
                                                <View style={[styles.sectorTag, { backgroundColor: sectorColor.bgLight }]}>
                                                    <Text style={[styles.sectorTagText, { color: sectorColor.color }]}>
                                                        {stock.sector}
                                                    </Text>
                                                </View>
                                                <View style={[styles.sectorTag, { backgroundColor: sectorColors[compareStock.sector as keyof typeof sectorColors]?.bgLight }]}>
                                                    <Text style={[styles.sectorTagText, { color: sectorColors[compareStock.sector as keyof typeof sectorColors]?.color }]}>
                                                        {compareStock.sector}
                                                    </Text>
                                                </View>
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
                                    owned: '100', // Mock owned shares - would come from portfolio in real app
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
        fontWeight: '700',
        color: '#666',
    },
    chartContainer: {
        marginHorizontal: 24,
        marginBottom: 24,
        borderWidth: 1,
        borderRadius: 12,
        padding: 5,
        overflow: 'hidden',
    },
    tabContainer: {
        flexDirection: 'row',
        borderWidth: 1,
        marginHorizontal: 24,
        marginBottom: 16,
        borderRadius: 12,
        overflow: 'hidden',
    },
    tab: {
        flex: 1,
        paddingVertical: 12,
        alignItems: 'center',
        borderBottomWidth: 0,
    },
    tabText: {
        fontSize: 13,
        fontWeight: '700',
    },
    tabContent: {
        paddingHorizontal: 24,
        paddingBottom: 24,
        gap: 20,
    },
    statsSection: {
        gap: 12,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '700',
    },
    statsGrid: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
        gap: 12,
    },
    statItem: {
        alignItems: 'flex-start',
        gap: 4,
    },
    statLabel: {
        fontSize: 10,
        fontWeight: '600',
    },
    statValue: {
        fontSize: 14,
        fontWeight: '700',
    },
    placeholderText: {
        fontSize: 13,
    },
    searchContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        borderWidth: 1,
        borderRadius: 10,
        paddingHorizontal: 12,
        gap: 10,
        marginBottom: 16,
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
        marginBottom: 16,
        maxHeight: 250,
        overflow: 'hidden',
    },
    searchResultsList: {
        maxHeight: 250,
    },
    searchResultItem: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 12,
        borderBottomWidth: 1,
    },
    searchResultLeft: {
        flex: 1,
    },
    searchResultSymbol: {
        fontSize: 14,
        fontWeight: '700',
        marginBottom: 2,
    },
    searchResultName: {
        fontSize: 11,
        fontWeight: '500',
    },
    searchResultPrice: {
        fontSize: 13,
        fontWeight: '700',
    },
    comparisonContainer: {
        gap: 16,
    },
    comparisonHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    comparisonTitle: {
        fontSize: 16,
        fontWeight: '800',
    },
    comparisonStats: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        gap: 16,
    },
    comparisonRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    comparisonLabel: {
        fontSize: 13,
        fontWeight: '600',
        flex: 1,
    },
    comparisonValues: {
        flexDirection: 'row',
        gap: 20,
        alignItems: 'center',
    },
    comparisonValue: {
        fontSize: 13,
        fontWeight: '600',
        minWidth: 80,
        textAlign: 'right',
    },
    changeIndicator: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
        minWidth: 80,
        alignItems: 'center',
    },
    sectorTag: {
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 6,
        minWidth: 80,
        alignItems: 'center',
    },
    sectorTagText: {
        fontSize: 11,
        fontWeight: '700',
    },
    emptyCompare: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 60,
        alignItems: 'center',
        gap: 12,
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
    },
    actionButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 14,
        borderRadius: 12,
        gap: 6,
    },
    actionButtonText: {
        fontSize: 14,
        fontWeight: '700',
        color: 'white',
    },
});