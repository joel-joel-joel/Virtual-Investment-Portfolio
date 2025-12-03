import React, { useState, useMemo } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TextInput,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
    Image,
    ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '@/src/constants/colors';

interface Stock {
    id: string;
    symbol: string;
    name: string;
    price: number;
    change: number;
    changePercent: number;
    sector: string;
    marketCap: string;
    image?: any;
}

interface SearchFilters {
    sector: string | null;
    marketCap: string | null;
}

const sectorColors = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Retail': { color: '#EA580C', bgLight: '#FFEDD5' },
};

const allStocks: Stock[] = [
    {
        id: '1',
        symbol: 'AAPL',
        name: 'Apple Inc.',
        price: 195.50,
        change: 2.50,
        changePercent: 1.30,
        sector: 'Technology',
        marketCap: '3.2T',
    },
    {
        id: '2',
        symbol: 'MSFT',
        name: 'Microsoft Corporation',
        price: 380.50,
        change: 5.25,
        changePercent: 1.40,
        sector: 'Technology',
        marketCap: '2.8T',
    },
    {
        id: '3',
        symbol: 'NVDA',
        name: 'NVIDIA Corporation',
        price: 892.50,
        change: 25.30,
        changePercent: 2.92,
        sector: 'Semiconductors',
        marketCap: '2.2T',
    },
    {
        id: '4',
        symbol: 'GOOGL',
        name: 'Alphabet Inc.',
        price: 140.75,
        change: 2.10,
        changePercent: 1.52,
        sector: 'Technology',
        marketCap: '1.8T',
    },
    {
        id: '5',
        symbol: 'TSLA',
        name: 'Tesla Inc.',
        price: 245.30,
        change: -3.50,
        changePercent: -1.41,
        sector: 'Consumer/Tech',
        marketCap: '780B',
    },
    {
        id: '6',
        symbol: 'AMZN',
        name: 'Amazon.com Inc.',
        price: 170.90,
        change: 3.20,
        changePercent: 1.91,
        sector: 'Retail',
        marketCap: '1.7T',
    },
    {
        id: '7',
        symbol: 'AMD',
        name: 'Advanced Micro Devices',
        price: 165.45,
        change: 4.50,
        changePercent: 2.79,
        sector: 'Semiconductors',
        marketCap: '268B',
    },
    {
        id: '8',
        symbol: 'META',
        name: 'Meta Platforms',
        price: 480.25,
        change: 8.75,
        changePercent: 1.86,
        sector: 'Technology',
        marketCap: '1.2T',
    },
    {
        id: '9',
        symbol: 'COIN',
        name: 'Coinbase Global',
        price: 178.30,
        change: 12.30,
        changePercent: 7.40,
        sector: 'FinTech',
        marketCap: '90B',
    },
    {
        id: '10',
        symbol: 'UBER',
        name: 'Uber Technologies',
        price: 72.15,
        change: 1.50,
        changePercent: 2.12,
        sector: 'Consumer/Tech',
        marketCap: '150B',
    },
];

const recentSearches = ['AAPL', 'NVDA', 'MSFT', 'TSLA'];
const sectors = ['Technology', 'Semiconductors', 'FinTech', 'Consumer/Tech', 'Healthcare', 'Retail'];

const SearchResultCard = ({
                              stock,
                              colors,
                              sectorColor,
                              isWatchlisted,
                              onToggleWatchlist,
                          }: {
    stock: Stock;
    colors: any;
    sectorColor: any;
    isWatchlisted: boolean;
    onToggleWatchlist: (stockId: string) => void;
}) => {
    const isPositive = stock.changePercent >= 0;

    return (
        <TouchableOpacity
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
                    onPress={() => onToggleWatchlist(stock.id)}
                    style={[styles.actionButton, { backgroundColor: isWatchlisted ? colors.tint : colors.tint + '15' }]}
                    activeOpacity={0.6}
                >
                    <MaterialCommunityIcons
                        name={isWatchlisted ? "heart" : "heart-outline"}
                        size={18}
                        color={isWatchlisted ? "white" : colors.tint}
                    />
                </TouchableOpacity>
                <TouchableOpacity
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
    const [recentSearchesList, setRecentSearchesList] = useState(recentSearches);
    const [watchlistedStocks, setWatchlistedStocks] = useState<Set<string>>(new Set());

    // Filter and search stocks
    const filteredStocks = useMemo(() => {
        let results = allStocks;

        // Search by symbol or name
        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            results = results.filter(stock =>
                stock.symbol.toLowerCase().includes(query) ||
                stock.name.toLowerCase().includes(query)
            );
        }

        // Filter by sector
        if (filters.sector) {
            results = results.filter(stock => stock.sector === filters.sector);
        }

        return results;
    }, [searchQuery, filters]);

    const handleClearSearch = () => {
        setSearchQuery('');
        setFilters({ sector: null, marketCap: null });
    };

    const handleClearRecentSearches = () => {
        setRecentSearchesList([]);
    };

    const handleRecentSearch = (query: string) => {
        setSearchQuery(query);
    };

    const toggleWatchlist = (stockId: string) => {
        setWatchlistedStocks(prev => {
            const newSet = new Set(prev);
            if (newSet.has(stockId)) {
                newSet.delete(stockId);
            } else {
                newSet.add(stockId);
            }
            return newSet;
        });
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
                        placeholder="Search by symbol or company..."
                        placeholderTextColor={Colors.text + '99'}
                        value={searchQuery}
                        onChangeText={setSearchQuery}
                    />
                    {searchQuery ? (
                        <TouchableOpacity onPress={() => setSearchQuery('')}>
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
            {!searchQuery && filteredStocks.length === allStocks.length ? (
                // Recent Searches
                <ScrollView
                    style={styles.content}
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={styles.contentContainer}
                >
                    <View style={styles.section}>
                        <View style={styles.sectionHeader}>
                            <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                Recent Searches
                            </Text>
                            {recentSearchesList.length > 0 && (
                                <TouchableOpacity onPress={handleClearRecentSearches}>
                                    <Text style={[styles.clearButton, { color: Colors.tint }]}>
                                        Clear
                                    </Text>
                                </TouchableOpacity>
                            )}
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

                    {/* Popular Stocks */}
                    <View style={styles.section}>
                        <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                            Popular Stocks
                        </Text>
                        {allStocks.slice(0, 5).map(stock => {
                            const sectorColor = sectorColors[stock.sector as keyof typeof sectorColors] || sectorColors['Technology'];
                            return (
                                <SearchResultCard
                                    key={stock.id}
                                    stock={stock}
                                    colors={Colors}
                                    sectorColor={sectorColor}
                                    isWatchlisted={watchlistedStocks.has(stock.id)}
                                    onToggleWatchlist={toggleWatchlist}
                                />
                            );
                        })}
                    </View>
                </ScrollView>
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
                        const sectorColor = sectorColors[stock.sector as keyof typeof sectorColors] || sectorColors['Technology'];
                        return (
                            <SearchResultCard
                                key={stock.id}
                                stock={stock}
                                colors={Colors}
                                sectorColor={sectorColor}
                                isWatchlisted={watchlistedStocks.has(stock.id)}
                                onToggleWatchlist={toggleWatchlist}
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
                        Try searching with a different term or adjust your filters
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
        fontSize: 20,
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
        paddingVertical: 200,
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
})