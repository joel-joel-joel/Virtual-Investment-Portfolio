import React, { useState, useMemo } from 'react';
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
import { getThemeColors } from '@/src/constants/colors';
import { useRouter } from 'expo-router';

interface WatchlistStock {
    id: string;
    symbol: string;
    name: string;
    price: number;
    change: number;
    changePercent: number;
    sector: string;
    dayHigh: number;
    dayLow: number;
}

interface SortOption {
    field: 'symbol' | 'price' | 'changePercent' | 'sector';
    order: 'asc' | 'desc';
}

const sectorColors = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Market': { color: '#EA580C', bgLight: '#FFEDD5' },
};

const defaultWatchlist: WatchlistStock[] = [
    {
        id: '1',
        symbol: 'AAPL',
        name: 'Apple Inc.',
        price: 195.50,
        change: 2.50,
        changePercent: 1.30,
        sector: 'Technology',
        dayHigh: 198.75,
        dayLow: 193.25,
    },
    {
        id: '2',
        symbol: 'MSFT',
        name: 'Microsoft Corporation',
        price: 380.50,
        change: 5.25,
        changePercent: 1.40,
        sector: 'Technology',
        dayHigh: 385.00,
        dayLow: 375.00,
    },
    {
        id: '3',
        symbol: 'NVDA',
        name: 'NVIDIA Corporation',
        price: 892.50,
        change: 25.30,
        changePercent: 2.92,
        sector: 'Semiconductors',
        dayHigh: 910.00,
        dayLow: 880.00,
    },
    {
        id: '4',
        symbol: 'TSLA',
        name: 'Tesla Inc.',
        price: 245.30,
        change: -3.50,
        changePercent: -1.41,
        sector: 'Consumer/Tech',
        dayHigh: 250.00,
        dayLow: 242.00,
    },
    {
        id: '5',
        symbol: 'GOOGL',
        name: 'Alphabet Inc.',
        price: 140.75,
        change: 2.10,
        changePercent: 1.52,
        sector: 'Technology',
        dayHigh: 143.00,
        dayLow: 138.50,
    },
    {
        id: '6',
        symbol: 'AMZN',
        name: 'Amazon.com Inc.',
        price: 170.90,
        change: 3.20,
        changePercent: 1.91,
        sector: 'Market',
        dayHigh: 174.00,
        dayLow: 168.00,
    },
    {
        id: '7',
        symbol: 'AMD',
        name: 'Advanced Micro Devices',
        price: 165.45,
        change: 4.50,
        changePercent: 2.79,
        sector: 'Semiconductors',
        dayHigh: 168.00,
        dayLow: 162.00,
    },
    {
        id: '8',
        symbol: 'META',
        name: 'Meta Platforms',
        price: 480.25,
        change: 8.75,
        changePercent: 1.86,
        sector: 'Technology',
        dayHigh: 490.00,
        dayLow: 475.00,
    },
];

const WatchlistCard = ({
                           stock,
                           colors,
                           sectorColor,
                           onRemove,
                           onAddToPortfolio,
                       }: {
    stock: WatchlistStock;
    colors: any;
    sectorColor: any;
    onRemove: (id: string) => void;
    onAddToPortfolio: (stock: WatchlistStock) => void;
}) => {
    const router = useRouter();
    const isPositive = stock.changePercent >= 0;
    const [expanded, setExpanded] = useState(false);

    const handleInvest = () => {
        const stockData = {
            symbol: stock.symbol,
            name: stock.name,
            price: stock.price,
            change: stock.change,
            changePercent: stock.changePercent,
            sector: stock.sector,
            marketCap: '0',
            peRatio: '0',
            dividend: '0',
            dayHigh: stock.dayHigh,
            dayLow: stock.dayLow,
            yearHigh: 0,
            yearLow: 0,
            description: '',
            employees: '',
            founded: '',
            website: '',
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
            onPress={() => setExpanded(!expanded)}
            style={[
                styles.watchlistCard,
                {
                    backgroundColor: colors.card,
                    borderColor: colors.border,
                    maxHeight: expanded ? 280 : 100,
                }
            ]}
            activeOpacity={0.7}
        >
            {/* Header Row */}
            <View style={styles.cardHeader}>
                <View style={styles.cardLeft}>
                    <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                        <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                            {stock.sector.slice(0, 1)}
                        </Text>
                    </View>
                    <View style={styles.stockInfo}>
                        <Text style={[styles.symbol, { color: sectorColor.color }]}>
                            {stock.symbol}
                        </Text>
                        <Text style={[styles.name, { color: colors.text, opacity: 0.7 }]} numberOfLines={1}>
                            {stock.name}
                        </Text>
                    </View>
                </View>

                <View style={styles.cardRight}>
                    <Text style={[styles.price, { color: colors.text }]}>
                        A${stock.price.toFixed(2)}
                    </Text>
                    <View style={[styles.changeBadge, { backgroundColor: isPositive ? '#E7F5E7' : '#FCE4E4' }]}>
                        <MaterialCommunityIcons
                            name={isPositive ? 'trending-up' : 'trending-down'}
                            size={14}
                            color={isPositive ? '#2E7D32' : '#C62828'}
                        />
                        <Text style={[styles.changeText, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                            {isPositive ? '+' : ''}{stock.changePercent.toFixed(2)}%
                        </Text>
                    </View>
                </View>
            </View>

            {/* Expanded Content */}
            {expanded && (
                <View style={[styles.expandedContent, { borderTopColor: colors.border }]}>
                    {/* Day Range */}
                    <View style={styles.rangeSection}>
                        <View style={styles.rangeItem}>
                            <Text style={[styles.rangeLabel, { color: colors.text, opacity: 0.6 }]}>
                                Day Low
                            </Text>
                            <Text style={[styles.rangeValue, { color: colors.text }]}>
                                A${stock.dayLow.toFixed(2)}
                            </Text>
                        </View>
                        <View style={styles.rangeItem}>
                            <Text style={[styles.rangeLabel, { color: colors.text, opacity: 0.6 }]}>
                                Day High
                            </Text>
                            <Text style={[styles.rangeValue, { color: colors.text }]}>
                                A${stock.dayHigh.toFixed(2)}
                            </Text>
                        </View>
                        <View style={styles.rangeItem}>
                            <Text style={[styles.rangeLabel, { color: colors.text, opacity: 0.6 }]}>
                                Change
                            </Text>
                            <Text style={[styles.rangeValue, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                                {isPositive ? '+' : ''}A${stock.change.toFixed(2)}
                            </Text>
                        </View>
                    </View>

                    {/* Action Buttons */}
                    <View style={styles.actionButtons}>
                        <TouchableOpacity
                            onPress={handleInvest}
                            style={[styles.actionButton, { backgroundColor: colors.tint }]}
                        >
                            <MaterialCommunityIcons name="plus" size={16} color="white" />
                            <Text style={styles.actionButtonText}>Invest</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            onPress={() => onRemove(stock.id)}
                            style={[styles.actionButton, { backgroundColor: '#FCE4E4' }]}
                        >
                            <MaterialCommunityIcons name="trash-can-outline" size={16} color="#C62828" />
                            <Text style={[styles.actionButtonText, { color: '#C62828' }]}>Remove</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            )}

            {/* Collapsed Chevron */}
            {!expanded && (
                <MaterialCommunityIcons
                    name="chevron-down"
                    size={20}
                    color={colors.text}
                    style={styles.chevron}
                />
            )}
        </TouchableOpacity>
    );
};

export default function WatchlistScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const [watchlist, setWatchlist] = useState(defaultWatchlist);
    const [sortBy, setSortBy] = useState<'symbol' | 'price' | 'changePercent' | 'sector'>('symbol');
    const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
    const [selectedSector, setSelectedSector] = useState<string | null>(null);

    // Get unique sectors
    const sectors = Array.from(new Set(watchlist.map(s => s.sector)));

    // Filter and sort watchlist
    const filteredAndSortedWatchlist = useMemo(() => {
        let results = [...watchlist];

        // Filter by sector
        if (selectedSector) {
            results = results.filter(stock => stock.sector === selectedSector);
        }

        // Sort
        results.sort((a, b) => {
            let aVal: any = a[sortBy];
            let bVal: any = b[sortBy];

            if (typeof aVal === 'string') {
                aVal = aVal.toLowerCase();
                bVal = bVal.toLowerCase();
            }

            if (sortOrder === 'asc') {
                return aVal > bVal ? 1 : -1;
            } else {
                return aVal < bVal ? 1 : -1;
            }
        });

        return results;
    }, [watchlist, sortBy, sortOrder, selectedSector]);

    const handleRemoveFromWatchlist = (id: string) => {
        Alert.alert(
            'Remove from Watchlist',
            'Are you sure you want to remove this stock?',
            [
                { text: 'Cancel', onPress: () => {}, style: 'cancel' },
                {
                    text: 'Remove',
                    onPress: () => {
                        setWatchlist(prev => prev.filter(stock => stock.id !== id));
                    },
                    style: 'destructive',
                },
            ]
        );
    };

    const handleAddToPortfolio = (stock: WatchlistStock) => {
        Alert.alert(
            'Add to Portfolio',
            `Add ${stock.symbol} to your portfolio?`,
            [
                { text: 'Cancel', onPress: () => {}, style: 'cancel' },
                {
                    text: 'Add',
                    onPress: () => {
                        Alert.alert('Success', `${stock.symbol} added to portfolio!`);
                    },
                },
            ]
        );
    };

    const toggleSortOrder = (field: 'symbol' | 'price' | 'changePercent' | 'sector') => {
        if (sortBy === field) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            setSortBy(field);
            setSortOrder('asc');
        }
    };

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: Colors.text }]}>
                    My Watchlist
                </Text>
                <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                    {filteredAndSortedWatchlist.length} stocks
                </Text>
            </View>

            {/* Stats Row */}
            <View style={[styles.statsRow, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                <View style={styles.statItem}>
                    <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                        Watching
                    </Text>
                    <Text style={[styles.statValue, { color: Colors.text }]}>
                        {watchlist.length}
                    </Text>
                </View>
                <View style={[styles.statDivider, { backgroundColor: Colors.border }]} />
                <View style={styles.statItem}>
                    <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                        Avg Change
                    </Text>
                    <Text style={[styles.statValue, { color: watchlist.reduce((sum, s) => sum + s.changePercent, 0) / watchlist.length >= 0 ? '#2E7D32' : '#C62828' }]}>
                        {(watchlist.reduce((sum, s) => sum + s.changePercent, 0) / watchlist.length).toFixed(2)}%
                    </Text>
                </View>
            </View>

            {/* Sector Filter */}
            <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                style={styles.sectorFilter}
                contentContainerStyle={styles.sectorFilterContent}
            >
                <TouchableOpacity
                    onPress={() => setSelectedSector(null)}
                    style={[
                        styles.sectorFilterButton,
                        selectedSector === null && { backgroundColor: Colors.tint }
                    ]}
                >
                    <Text
                        style={[
                            styles.sectorFilterText,
                            selectedSector === null && { color: 'white', fontWeight: '700' }
                        ]}
                    >
                        All
                    </Text>
                </TouchableOpacity>
                {sectors.map(sector => (
                    <TouchableOpacity
                        key={sector}
                        onPress={() => setSelectedSector(selectedSector === sector ? null : sector)}
                        style={[
                            styles.sectorFilterButton,
                            selectedSector === sector && { backgroundColor: Colors.tint }
                        ]}
                    >
                        <Text
                            style={[
                                styles.sectorFilterText,
                                selectedSector === sector && { color: 'white', fontWeight: '700' }
                            ]}
                        >
                            {sector}
                        </Text>
                    </TouchableOpacity>
                ))}
            </ScrollView>

            {/* Sort Options */}
            <View style={styles.sortContainer}>
                <TouchableOpacity
                    onPress={() => toggleSortOrder('symbol')}
                    style={[styles.sortButton, sortBy === 'symbol' && { backgroundColor: Colors.tint }]}
                >
                    <MaterialCommunityIcons
                        name="sort-ascending"
                        size={14}
                        color={sortBy === 'symbol' ? 'white' : Colors.text}
                    />
                    <Text style={[styles.sortButtonText, sortBy === 'symbol' && { color: 'white', fontWeight: '700' }]}>
                        Symbol
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    onPress={() => toggleSortOrder('price')}
                    style={[styles.sortButton, sortBy === 'price' && { backgroundColor: Colors.tint }]}
                >
                    <MaterialCommunityIcons
                        name="currency-usd"
                        size={14}
                        color={sortBy === 'price' ? 'white' : Colors.text}
                    />
                    <Text style={[styles.sortButtonText, sortBy === 'price' && { color: 'white', fontWeight: '700' }]}>
                        Price
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    onPress={() => toggleSortOrder('changePercent')}
                    style={[styles.sortButton, sortBy === 'changePercent' && { backgroundColor: Colors.tint }]}
                >
                    <MaterialCommunityIcons
                        name="percent"
                        size={14}
                        color={sortBy === 'changePercent' ? 'white' : Colors.text}
                    />
                    <Text style={[styles.sortButtonText, sortBy === 'changePercent' && { color: 'white', fontWeight: '700' }]}>
                        Change
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    onPress={() => toggleSortOrder('sector')}
                    style={[styles.sortButton, sortBy === 'sector' && { backgroundColor: Colors.tint }]}
                >
                    <MaterialCommunityIcons
                        name="tag"
                        size={14}
                        color={sortBy === 'sector' ? 'white' : Colors.text}
                    />
                    <Text style={[styles.sortButtonText, sortBy === 'sector' && { color: 'white', fontWeight: '700' }]}>
                        Sector
                    </Text>
                </TouchableOpacity>
            </View>

            {/* Watchlist Items */}
            {filteredAndSortedWatchlist.length > 0 ? (
                <ScrollView
                    style={styles.listContainer}
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={styles.listContent}
                >
                    {filteredAndSortedWatchlist.map(stock => {
                        const sectorColor = sectorColors[stock.sector as keyof typeof sectorColors] || sectorColors['Technology'];
                        return (
                            <WatchlistCard
                                key={stock.id}
                                stock={stock}
                                colors={Colors}
                                sectorColor={sectorColor}
                                onRemove={handleRemoveFromWatchlist}
                                onAddToPortfolio={handleAddToPortfolio}
                            />
                        );
                    })}
                </ScrollView>
            ) : (
                <View style={styles.emptyState}>
                    <MaterialCommunityIcons
                        name="heart-outline"
                        size={56}
                        color={Colors.text}
                        style={{ opacity: 0.3, marginBottom: 16 }}
                    />
                    <Text style={[styles.emptyStateTitle, { color: Colors.text }]}>
                        No stocks in watchlist
                    </Text>
                    <Text style={[styles.emptyStateSubtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Add stocks from search to get started
                    </Text>
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
        fontSize: 13,
    },
    statsRow: {
        marginBottom: 16,
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 12,
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    statItem: {
        alignItems: 'center',
        gap: 4,
    },
    statLabel: {
        fontSize: 11,
        fontWeight: '600',
    },
    statValue: {
        fontSize: 16,
        fontWeight: '700',
    },
    statDivider: {
        width: 1,
        height: 40,
    },
    sectorFilter: {
        marginBottom: 12,
    },
    sectorFilterContent: {
        gap: 8,
        paddingRight: 12,
    },
    sectorFilterButton: {
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8,
        backgroundColor: '#F0F0F0',
    },
    sectorFilterText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
    },
    sortContainer: {
        paddingHorizontal: 24,
        marginBottom: 16,
        flexDirection: 'row',
        gap: 8,
    },
    sortButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 8,
        borderRadius: 8,
        backgroundColor: '#F0F0F0',
        gap: 4,
    },
    sortButtonText: {
        fontSize: 11,
        fontWeight: '600',
        color: '#666',
    },
    listContainer: {
        flex: 1,
    },
    listContent: {
        paddingBottom: 24,
        gap: 12,
    },
    watchlistCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
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
    symbol: {
        fontSize: 13,
        fontWeight: '700',
        marginBottom: 2,
    },
    name: {
        fontSize: 11,
        fontWeight: '500',
    },
    cardRight: {
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
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
    },
    changeText: {
        fontSize: 11,
        fontWeight: '700',
    },
    chevron: {
        position: 'absolute',
        right: 12,
        top: '50%',
        marginTop: -10,
        opacity: 0.4,
    },
    expandedContent: {
        marginTop: 12,
        paddingTop: 12,
        borderTopWidth: 1,
        gap: 12,
    },
    rangeSection: {
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    rangeItem: {
        alignItems: 'center',
        gap: 4,
    },
    rangeLabel: {
        fontSize: 10,
        fontWeight: '600',
    },
    rangeValue: {
        fontSize: 12,
        fontWeight: '700',
    },
    actionButtons: {
        flexDirection: 'row',
        gap: 10,
    },
    actionButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 10,
        borderRadius: 10,
        gap: 6,
    },
    actionButtonText: {
        fontSize: 12,
        fontWeight: '700',
        color: 'white',
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
});