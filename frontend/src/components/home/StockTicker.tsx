import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Animated, Dimensions, useColorScheme, ActivityIndicator } from 'react-native';
import { getThemeColors } from '../../constants/colors';
import { getSectorColor } from '@/src/services/sectorColorService';
import { getStockQuote } from '@/src/services/entityService';
import { getCompanyProfile } from '@/src/services/entityService';

interface TickerStock {
    symbol: string;
    price: string;
    change: string;
    sector: string;
}

interface StockTickerProps {
    refreshTrigger?: number;
}

const SCREEN_WIDTH = Dimensions.get('window').width;

// Popular stocks across different sectors for randomization
const POPULAR_STOCKS = [
    'AAPL', 'MSFT', 'GOOGL', 'AMZN', 'NVDA', 'TSLA',
    'META', 'NFLX', 'GOOG', 'INTC', 'AMD', 'AVGO',
    'JPM', 'GS', 'BAC', 'WFC', 'C', 'BLK',
    'JNJ', 'PFE', 'ABBV', 'UNH', 'MRK', 'LLY',
    'XOM', 'COP', 'CVX', 'MPC', 'PSX', 'OXY',
    'DIS', 'CMCSA', 'PARA', 'NWSA', 'FOX', 'WBD',
];

export const StockTicker: React.FC<StockTickerProps> = ({ refreshTrigger }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const [tickerStocks, setTickerStocks] = useState<TickerStock[]>([]);
    const [loading, setLoading] = useState(true);

    const scrollX = React.useRef(new Animated.Value(0)).current;

    // Fetch real-time data from Finnhub for randomized stocks
    useEffect(() => {
        const fetchTickerData = async () => {
            try {
                setLoading(true);

                // Randomly select 8-12 stocks from popular list
                const shuffled = [...POPULAR_STOCKS].sort(() => 0.5 - Math.random());
                const selectedStocks = shuffled.slice(0, Math.floor(Math.random() * 5) + 8);

                const tickerData = await Promise.all(selectedStocks.map(async (symbol) => {
                        try {
                            // Fetch real-time quote
                            const quote = await getStockQuote(symbol);

                            // Fetch company profile to get sector
                            let sector = 'Unknown';
                            try {
                                const profile = await getCompanyProfile(symbol);
                                sector = profile.finnhubIndustry || 'Unknown';
                            } catch (error) {
                                console.warn(`Failed to fetch profile for ${symbol}:`, error);
                            }

                            // Calculate price change
                            const change = quote.c - quote.pc; // current - previous close
                            const changePercent = ((change / quote.pc) * 100).toFixed(2);
                            const changeStr = `${parseFloat(changePercent) >= 0 ? '+' : ''}${changePercent}%`;

                            return {
                                symbol,
                                price: `A$${quote.c.toFixed(2)}`,
                                change: changeStr,
                                sector,
                            };
                        } catch (error) {
                            console.error(`Failed to fetch data for ${symbol}:`, error);
                            // Return null on error, we'll filter these out
                            return null;
                        }
                    })
                );

                // Filter out failed requests
                const validData = tickerData.filter((stock): stock is TickerStock => stock !== null);

                setTickerStocks(validData);
            } catch (error) {
                console.error('Failed to fetch ticker data:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchTickerData();
    }, [refreshTrigger]);

    // Start scroll animation once data is loaded
    useEffect(() => {
        if (tickerStocks.length === 0 || loading) return;

        const scrollAnimation = Animated.loop(
            Animated.timing(scrollX, {
                toValue: -SCREEN_WIDTH * 3,
                duration: 25000, // 25 seconds for smoother scrolling with more stocks
                useNativeDriver: true,
            })
        );

        scrollAnimation.start();

        return () => scrollAnimation.stop();
    }, [tickerStocks, loading, scrollX]);

    // Show loading state
    if (loading) {
        return (
            <View style={[styles.tickerWrapper, { backgroundColor: 'white', borderColor: Colors.border, justifyContent: 'center', alignItems: 'center' }]}>
                <ActivityIndicator size="small" color={Colors.tint} />
                <Text style={[styles.loadingText, { color: Colors.text, marginLeft: 8 }]}>
                    Loading market data...
                </Text>
            </View>
        );
    }

    // Show error state if no stocks loaded
    if (tickerStocks.length === 0) {
        return (
            <View style={[styles.tickerWrapper, { backgroundColor: 'white', borderColor: Colors.border, justifyContent: 'center' }]}>
                <Text style={[styles.emptyText, { color: Colors.text }]}>
                    Unable to load market data
                </Text>
            </View>
        );
    }

    // Duplicate stocks for infinite loop
    const duplicatedStocks = [...tickerStocks, ...tickerStocks, ...tickerStocks];

    return (
        <View style={[styles.tickerWrapper, { backgroundColor: 'white', borderColor: Colors.border }]}>
            <View style={styles.tickerContainer}>
                <Animated.View
                    style={[
                        styles.tickerContent,
                        {
                            transform: [{ translateX: scrollX }],
                        },
                    ]}
                >
                    {duplicatedStocks.map((stock, index) => {
                        // Use sector-based coloring
                        const sectorColor = getSectorColor(stock.sector);
                        const isPositive = !stock.change.startsWith('-');

                        return (
                            <View key={index} style={styles.tickerItem}>
                                <Text style={[styles.tickerSymbol, { color: sectorColor.color }]}>
                                    {stock.symbol}
                                </Text>
                                <Text style={[styles.tickerPrice, { color: Colors.text }]}>
                                    {stock.price}
                                </Text>
                                <Text
                                    style={[
                                        styles.tickerChange,
                                        {
                                            color: isPositive ? '#2E7D32' : '#C62828',
                                        },
                                    ]}
                                >
                                    {stock.change}
                                </Text>
                                <View style={[styles.separator, { backgroundColor: Colors.border }]} />
                            </View>
                        );
                    })}
                </Animated.View>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    tickerWrapper: {
        width: '100%',
        height: 50,
        borderRadius: 12,
        borderWidth: 1,
        marginTop: 5,
        overflow: 'hidden',
    },
    tickerContainer: {
        flex: 1,
        overflow: 'hidden',
    },
    tickerContent: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 8,
    },
    tickerItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 16,
        gap: 8,
    },
    tickerSymbol: {
        fontSize: 13,
        fontWeight: '700',
        minWidth: 45,
    },
    tickerPrice: {
        fontSize: 12,
        fontWeight: '600',
        minWidth: 70,
    },
    tickerChange: {
        fontSize: 11,
        fontWeight: '600',
        minWidth: 50,
    },
    separator: {
        width: 1,
        height: 50,
        marginLeft: 16,
    },
    emptyText: {
        fontSize: 12,
        fontWeight: '500',
        opacity: 0.6,
    },
    loadingText: {
        fontSize: 12,
        fontWeight: '500',
    },
});