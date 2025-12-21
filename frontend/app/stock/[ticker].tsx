import React from 'react';
import { View, Text, StyleSheet, useColorScheme } from 'react-native';
import { useRoute } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';
import type { RootStackParamList } from '@/src/navigation';
import { useTheme } from '@/src/context/ThemeContext';
import StockTickerScreen from '../../src/components/stock/StockTickerScreen';

interface StockData {
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
 * Stock Ticker Dynamic Route Page
 *
 * This is the entry point for viewing individual stock details.
 * It receives the stock data from route params and passes it
 * to the StockTickerScreen component which now includes:
 * - StockHeaderChart (header, price, chart, tabs)
 * - Tab content (overview, news, transactions, compare)
 *
 * File location: app/stock/[ticker].tsx
 */

export default function StockPage() {
    const route = useRoute<RouteProp<RootStackParamList, 'StockTicker'>>();
    const {Colors} = useTheme();

    // Get stock data from route params (already an object, no parsing needed)
    const stock = route.params?.stock || null;

    if (!stock) {
        return (
            <View style={[styles.errorContainer, { backgroundColor: Colors.background }]}>
                <Text style={[styles.errorText, { color: Colors.text }]}>Stock data not found</Text>
            </View>
        );
    }

    // Create a mock route object for StockTickerScreen compatibility
    const mockRoute = {
        params: { stock },
    };

    return <StockTickerScreen route={mockRoute} />;
}

const styles = StyleSheet.create({
    errorContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    errorText: {
        fontSize: 16,
        fontWeight: '500',
    },
});