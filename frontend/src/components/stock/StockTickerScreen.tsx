import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
    Alert,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { getThemeColors } from '@/src/constants/colors';
import { HeaderSection } from "@/src/components/home/HeaderSection";
import TransactionHistory from '@/src/components/transaction/TransactionHistory';

const screenWidth = Dimensions.get('window').width - 48;

const sectorColors: Record<string, { color: string; bgLight: string }> = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Retail': { color: '#EA580C', bgLight: '#FFEDD5' },
};

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

    const timeframes = ['1D', '1W', '1M', '3M', '1Y'];
    const isPositive = stock.changePercent >= 0;

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
                            onPress={() => setSelectedTimeframe(tf)}
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

                {/* Placeholder Chart */}
                <View style={[styles.chartPlaceholder, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <Text style={[styles.chartText, { color: Colors.text, opacity: 0.5 }]}>
                        Chart Placeholder
                    </Text>
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
                                <StatItem label="Day High" value={`A$${stock.dayHigh.toFixed(2)}`} colors={Colors} />
                                <StatItem label="Day Low" value={`A$${stock.dayLow.toFixed(2)}`} colors={Colors} />
                                <StatItem label="52W High" value={`A$${stock.yearHigh.toFixed(2)}`} colors={Colors} />
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
                                Compare Stock
                            </Text>
                            <TouchableOpacity
                                style={[styles.compareButton, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                                onPress={() => Alert.alert('Compare', 'Stock comparison feature coming soon')}
                            >
                                <MaterialCommunityIcons name="plus" size={20} color={Colors.tint} />
                                <Text style={[styles.compareButtonText, { color: Colors.tint }]}>
                                    Add stock to compare
                                </Text>
                            </TouchableOpacity>
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
    chartPlaceholder: {
        marginHorizontal: 24,
        marginBottom: 24,
        borderWidth: 1,
        borderRadius: 12,
        height: 200,
        alignItems: 'center',
        justifyContent: 'center',
    },
    chartText: {
        fontSize: 14,
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
    compareButton: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 14,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
    },
    compareButtonText: {
        fontSize: 13,
        fontWeight: '700',
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