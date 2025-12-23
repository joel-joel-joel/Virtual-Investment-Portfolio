import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TouchableOpacity,
    ScrollView,
    useColorScheme,
    Dimensions,
    Image,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import {LineChart} from "recharts";
import { getSectorColor } from '../../services/sectorColorService';

interface StockDetailProps {
    symbol: string;
    price: number;
    change: number;
    changePercent: number;
    sector: string;
    marketCap: string;
    peRatio: string;
    dividendYield: string;
    description: string;
    chartData: number[];
    image?: any;
}

const screenWidth = Dimensions.get('window').width - 48;

export const StockDetailScreen: React.FC<StockDetailProps> = ({
    symbol,
    price,
    change,
    changePercent,
    sector,
    marketCap,
    peRatio,
    dividendYield,
    description,
    chartData,
    image,
}) => {
    const {Colors} = useTheme()
    const [selectedTimeframe, setSelectedTimeframe] = useState<'1D' | '1W' | '1M' | '3M' | '1Y' | 'ALL'>('1M');

    const isPositive = change >= 0;
    const timeframes = ['1D', '1W', '1M', '3M', '1Y', 'ALL'] as const;

    // Get dynamic color for sector
    const sectorColor = getSectorColor(sector);

    return (
        <ScrollView style={[styles.container, { backgroundColor: Colors.background }]} showsVerticalScrollIndicator={false}>
            {/* Header */}
            <View style={styles.header}>
                <View style={styles.symbolSection}>
                    {image && (
                        <Image
                            source={image}
                            style={styles.stockImage}
                            resizeMode="contain"
                        />
                    )}
                    <View>
                        <Text style={[styles.symbol, { color: Colors.text }]}>
                            {symbol}
                        </Text>
                        <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                            <Text style={[styles.sectorText, { color: sectorColor.color }]}>
                                {sector}
                            </Text>
                        </View>
                    </View>
                </View>
                <MaterialCommunityIcons
                    name="heart-outline"
                    size={28}
                    color={Colors.tint}
                />
            </View>

            {/* Price Section */}
            <View style={[styles.priceCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                <View style={styles.priceRow}>
                    <Text style={[styles.currentPrice, { color: Colors.text }]}>
                        A${price.toFixed(2)}
                    </Text>
                    <View style={[styles.changeBadge, { backgroundColor: isPositive ? '#E7F5E7' : '#FCE4E4' }]}>
                        <MaterialCommunityIcons
                            name={isPositive ? 'trending-up' : 'trending-down'}
                            size={16}
                            color={isPositive ? '#2E7D32' : '#C62828'}
                            style={{ marginRight: 4 }}
                        />
                        <Text style={[styles.changeText, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                            {isPositive ? '+' : ''}{change.toFixed(2)} ({changePercent.toFixed(2)}%)
                        </Text>
                    </View>
                </View>
                <Text style={[styles.lastUpdated, { color: Colors.text, opacity: 0.6 }]}>
                    Today
                </Text>
            </View>

            {/* Chart */}
            <View style={[styles.chartCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                <View style={styles.timeframeSelector}>
                    {timeframes.map((tf) => (
                        <TouchableOpacity
                            key={tf}
                            onPress={() => setSelectedTimeframe(tf)}
                            style={[
                                styles.timeframeButton,
                                selectedTimeframe === tf && [
                                    styles.timeframeButtonActive,
                                    { backgroundColor: Colors.tint },
                                ],
                            ]}
                        >
                            <Text
                                style={[
                                    styles.timeframeText,
                                    selectedTimeframe === tf
                                        ? { color: 'white', fontWeight: '700' }
                                        : { color: Colors.text, opacity: 0.6 },
                                ]}
                            >
                                {tf}
                            </Text>
                        </TouchableOpacity>
                    ))}
                </View>

                <View style={{ overflow: 'hidden', borderRadius: 12, marginTop: 12 }}>
                    <LineChart
                        data={{
                            //@ts-ignore
                            labels: [],
                            datasets: [{ data: chartData, color: () => Colors.tint, strokeWidth: 2 }],
                        }}
                        width={screenWidth}
                        height={200}
                        withDots={false}
                        withInnerLines={false}
                        withOuterLines={false}
                        withVerticalLabels={false}
                        withHorizontalLabels={false}
                        withShadow={false}
                        chartConfig={{
                            backgroundColor: Colors.card,
                            backgroundGradientFrom: Colors.card,
                            backgroundGradientTo: Colors.card,
                            color: () => Colors.tint,
                        }}
                        style={{ borderRadius: 12 }}
                    />
                </View>
            </View>

            {/* Key Metrics */}
            <View style={styles.metricsSection}>
                <Text style={[styles.metricsTitle, { color: Colors.text }]}>
                    Key Metrics
                </Text>
                <View style={styles.metricsGrid}>
                    <View style={[styles.metricCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <Text style={[styles.metricLabel, { color: Colors.text, opacity: 0.7 }]}>
                            Market Cap
                        </Text>
                        <Text style={[styles.metricValue, { color: Colors.text }]}>
                            {marketCap}
                        </Text>
                    </View>
                    <View style={[styles.metricCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <Text style={[styles.metricLabel, { color: Colors.text, opacity: 0.7 }]}>
                            P/E Ratio
                        </Text>
                        <Text style={[styles.metricValue, { color: Colors.text }]}>
                            {peRatio}
                        </Text>
                    </View>
                    <View style={[styles.metricCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <Text style={[styles.metricLabel, { color: Colors.text, opacity: 0.7 }]}>
                            Dividend Yield
                        </Text>
                        <Text style={[styles.metricValue, { color: Colors.text }]}>
                            {dividendYield}
                        </Text>
                    </View>
                </View>
            </View>

            {/* About */}
            <View style={styles.aboutSection}>
                <Text style={[styles.aboutTitle, { color: Colors.text }]}>
                    About
                </Text>
                <Text style={[styles.aboutText, { color: Colors.text }]}>
                    {description}
                </Text>
            </View>

            {/* Action Buttons */}
            <View style={styles.actionButtons}>
                <TouchableOpacity style={[styles.buyButton, { backgroundColor: Colors.tint }]}>
                    <Text style={styles.buttonText}>Buy</Text>
                </TouchableOpacity>
                <TouchableOpacity style={[styles.sellButton, { backgroundColor: Colors.card, borderColor: Colors.tint, borderWidth: 2 }]}>
                    <Text style={[styles.buttonTextSecondary, { color: Colors.tint }]}>Sell</Text>
                </TouchableOpacity>
            </View>

            <View style={{ height: 30 }} />
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 20,
    },
    symbolSection: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    stockImage: {
        width: 48,
        height: 48,
        borderRadius: 12,
    },
    symbol: {
        fontSize: 24,
        fontWeight: '800',
        marginBottom: 4,
    },
    sectorBadge: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
        alignSelf: 'flex-start',
    },
    sectorText: {
        fontSize: 11,
        fontWeight: '600',
    },
    priceCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        marginBottom: 16,
    },
    priceRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 8,
    },
    currentPrice: {
        fontSize: 32,
        fontWeight: '800',
    },
    changeBadge: {
        flexDirection: 'row',
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8,
        alignItems: 'center',
    },
    changeText: {
        fontSize: 14,
        fontWeight: '700',
    },
    lastUpdated: {
        fontSize: 12,
    },
    chartCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        marginBottom: 16,
    },
    timeframeSelector: {
        flexDirection: 'row',
        gap: 8,
        justifyContent: 'space-between',
    },
    timeframeButton: {
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8,
        flex: 1,
        alignItems: 'center',
    },
    timeframeButtonActive: {
        borderWidth: 0,
    },
    timeframeText: {
        fontSize: 12,
        fontWeight: '600',
    },
    metricsSection: {
        marginBottom: 16,
    },
    metricsTitle: {
        fontSize: 16,
        fontWeight: '700',
        marginBottom: 12,
    },
    metricsGrid: {
        flexDirection: 'row',
        gap: 12,
    },
    metricCard: {
        flex: 1,
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
    },
    metricLabel: {
        fontSize: 11,
        fontWeight: '600',
        marginBottom: 4,
    },
    metricValue: {
        fontSize: 14,
        fontWeight: '700',
    },
    rangeCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        marginBottom: 16,
    },
    rangeTitle: {
        fontSize: 16,
        fontWeight: '700',
        marginBottom: 16,
    },
    rangeRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    rangeLabel: {
        fontSize: 11,
        fontWeight: '600',
        marginBottom: 4,
    },
    rangeValue: {
        fontSize: 13,
        fontWeight: '700',
    },
    rangeBar: {
        flex: 1,
        height: 6,
        borderRadius: 3,
        position: 'relative',
    },
    rangeIndicator: {
        width: 8,
        height: 8,
        borderRadius: 4,
        position: 'absolute',
        top: -1,
    },
    aboutSection: {
        marginBottom: 20,
    },
    aboutTitle: {
        fontSize: 16,
        fontWeight: '700',
        marginBottom: 8,
    },
    aboutText: {
        fontSize: 13,
        lineHeight: 20,
    },
    actionButtons: {
        flexDirection: 'row',
        gap: 12,
        marginBottom: 12,
    },
    buyButton: {
        flex: 1,
        paddingVertical: 14,
        borderRadius: 12,
        alignItems: 'center',
    },
    sellButton: {
        flex: 1,
        paddingVertical: 14,
        borderRadius: 12,
        alignItems: 'center',
    },
    buttonText: {
        color: 'white',
        fontSize: 16,
        fontWeight: '700',
    },
    buttonTextSecondary: {
        fontSize: 16,
        fontWeight: '700',
    },
});