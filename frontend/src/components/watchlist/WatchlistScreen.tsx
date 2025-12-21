import React, { useState, useMemo, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Alert,
    ActivityIndicator,
    RefreshControl,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
import { getWatchlist, removeFromWatchlist } from '@/src/services/portfolioService';
import { useFocusEffect } from '@react-navigation/native';
import { getSectorColor } from '@/src/services/sectorColorService';
import {StockDTO} from "@/src/types/api";

interface WatchlistStock {
    watchlistId: string;
    userId: string;
    stockId: string;
    symbol: string;
    name: string;
    price: number;
    change: number;
    changePercent: number;
    addedAt: string;
    sector?: string;
}

// Dynamic sector colors are now fetched from sectorColorService

const WatchlistCard = ({
                           stock,
                           colors,
                           sectorColor,
                           onRemove,
                           isRemoving,
                       }: {
    stock: WatchlistStock;
    colors: any;
    sectorColor: any;
    onRemove: (stockId: string) => void;
    isRemoving: boolean;
}) => {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const isPositive = stock.changePercent >= 0;
    const [expanded, setExpanded] = useState(false);

    const handleInvest = () => {
        const stockData = {
            symbol: stock.symbol,
            name: stock.name,
            price: stock.price,
            change: stock.change,
            changePercent: stock.changePercent,
            stockId: stock.stockId,
        };

        navigation.navigate('BuyTransaction', { stock: stockData });
    };

    const handleNavigateToStock = () => {
        const stockData = {
            symbol: stock.symbol,
            name: stock.name,
            price: stock.price,
            change: stock.change,
            changePercent: stock.changePercent,
            sector: stock.sector || 'Other',
        };

        navigation.navigate('StockTicker', { stock: stockData });
    };

    return (
        <TouchableOpacity
            onPress={() => !isRemoving && setExpanded(!expanded)}
            onLongPress={handleNavigateToStock}
            style={[
                styles.watchlistCard,
                {
                    backgroundColor: colors.card,
                    borderColor: colors.border,
                    maxHeight: expanded ? 280 : 100,
                    opacity: isRemoving ? 0.5 : 1,
                }
            ]}
            activeOpacity={0.7}
            disabled={isRemoving}
        >
            {/* Header Row */}
            <View style={styles.cardHeader}>
                <View style={styles.cardLeft}>
                    <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                        <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                            {stock.symbol.slice(0, 1)}
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
                    {/* Stats */}
                    <View style={styles.rangeSection}>
                        <View style={styles.rangeItem}>
                            <Text style={[styles.rangeLabel, { color: colors.text, opacity: 0.6 }]}>
                                Change
                            </Text>
                            <Text style={[styles.rangeValue, { color: isPositive ? '#2E7D32' : '#C62828' }]}>
                                {isPositive ? '+' : ''}A${Math.abs(stock.change).toFixed(2)}
                            </Text>
                        </View>
                        <View style={styles.rangeItem}>
                            <Text style={[styles.rangeLabel, { color: colors.text, opacity: 0.6 }]}>
                                Current Price
                            </Text>
                            <Text style={[styles.rangeValue, { color: colors.text }]}>
                                A${stock.price.toFixed(2)}
                            </Text>
                        </View>
                        {stock.sector && (
                            <View style={styles.rangeItem}>
                                <Text style={[styles.rangeLabel, { color: colors.text, opacity: 0.6 }]}>
                                    Sector
                                </Text>
                                <Text style={[styles.rangeValue, { color: sectorColor.color }]}>
                                    {stock.sector}
                                </Text>
                            </View>
                        )}
                    </View>

                    {/* Action Buttons */}
                    <View style={styles.actionButtons}>
                        <TouchableOpacity
                            onPress={handleInvest}
                            style={[styles.actionButton, { backgroundColor: colors.tint }]}
                            disabled={isRemoving}
                        >
                            <MaterialCommunityIcons name="plus" size={16} color="white" />
                            <Text style={styles.actionButtonText}>Invest</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            onPress={() => onRemove(stock.stockId)}
                            style={[styles.actionButton, { backgroundColor: '#FCE4E4' }]}
                            disabled={isRemoving}
                        >
                            {isRemoving ? (
                                <ActivityIndicator size="small" color="#C62828" />
                            ) : (
                                <>
                                    <MaterialCommunityIcons name="trash-can-outline" size={16} color="#C62828" />
                                    <Text style={[styles.actionButtonText, { color: '#C62828' }]}>Remove</Text>
                                </>
                            )}
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
    const {Colors} = useTheme();
    const [watchlist, setWatchlist] = useState<WatchlistStock[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [removingStockId, setRemovingStockId] = useState<string | null>(null);
    const [sortBy, setSortBy] = useState<'symbol' | 'price' | 'changePercent'>('symbol');
    const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');

    // Load watchlist from API with proper error handling
    const loadWatchlist = useCallback(async (showLoading = true) => {
        try {
            if (showLoading) setLoading(true);
            const data = await getWatchlist();

            console.log('Raw watchlist data from API:', JSON.stringify(data, null, 2));

            // Transform API data to component format with null safety
            const formattedData: WatchlistStock[] = data.map(item => {
                // Safe parsing with fallback values and detailed logging
                const price = item.currentPrice != null ? parseFloat(item.currentPrice.toString()) : 0;
                const change = item.priceChange != null ? parseFloat(item.priceChange.toString()) : 0;  // ✅ Fixed: was item.change
                const changePercent = item.priceChangePercent != null ? parseFloat(item.priceChangePercent.toString()) : 0;  // ✅ Fixed: was item.changePercent

                console.log(`Stock ${item.stockCode}: price=${price}, change=${change}, changePercent=${changePercent}`);

                // Fetch sector from profile if available
                let sector = 'Other';
                if (item.sector) {
                    sector = item.sector;
                }

                return {
                    watchlistId: item.watchlistId || '',
                    userId: item.userId || '',
                    stockId: item.stockId || '',
                    symbol: item.stockCode || 'N/A',
                    name: item.companyName || 'Unknown Company',
                    price,
                    change,
                    changePercent,
                    addedAt: item.addedAt || new Date().toISOString(),
                    sector,
                };
            });

            setWatchlist(formattedData);
        } catch (error) {
            console.error('Error loading watchlist:', error);
            Alert.alert(
                'Error',
                'Failed to load watchlist. Please check your connection and try again.',
                [{ text: 'OK' }]
            );
        } finally {
            if (showLoading) setLoading(false);
        }
    }, []);

    // Initial load
    useEffect(() => {
        loadWatchlist();
    }, [loadWatchlist]);

    // Reload when screen comes into focus (e.g., after adding a stock)
    useFocusEffect(
        useCallback(() => {
            loadWatchlist(false);
        }, [loadWatchlist])
    );

    // Pull to refresh handler
    const onRefresh = useCallback(async () => {
        setRefreshing(true);
        await loadWatchlist(false);
        setRefreshing(false);
    }, [loadWatchlist]);

    // Sort watchlist
    const sortedWatchlist = useMemo(() => {
        let results = [...watchlist];

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
    }, [watchlist, sortBy, sortOrder]);

    // Handle remove from watchlist
    const handleRemoveFromWatchlist = async (stockId: string) => {
        const stock = watchlist.find(s => s.stockId === stockId);

        Alert.alert(
            'Remove from Watchlist',
            `Are you sure you want to remove ${stock?.symbol || 'this stock'} from your watchlist?`,
            [
                {
                    text: 'Cancel',
                    style: 'cancel'
                },
                {
                    text: 'Remove',
                    style: 'destructive',
                    onPress: async () => {
                        try {
                            setRemovingStockId(stockId);

                            // Call API to remove from backend
                            await removeFromWatchlist(stockId);

                            // Update local state
                            setWatchlist(prev => prev.filter(s => s.stockId !== stockId));

                            // Show success message (optional, comment out if too intrusive)
                            // Alert.alert('Success', 'Stock removed from watchlist');
                        } catch (error: any) {
                            console.error('Error removing from watchlist:', error);

                            // Extract more helpful error message
                            let errorMessage = 'Failed to remove stock. Please try again.';

                            if (error?.message) {
                                if (error.message.includes('transaction')) {
                                    errorMessage = 'Server configuration error. Please contact support or try again later.';
                                } else if (error.message.includes('Unauthorized')) {
                                    errorMessage = 'Your session has expired. Please log in again.';
                                } else if (error.message.includes('Not Found')) {
                                    errorMessage = 'Stock not found in watchlist.';
                                } else {
                                    errorMessage = error.message;
                                }
                            }

                            Alert.alert(
                                'Error',
                                errorMessage,
                                [{ text: 'OK' }]
                            );

                            // Reload the list to ensure consistency
                            await loadWatchlist(false);
                        } finally {
                            setRemovingStockId(null);
                        }
                    },
                },
            ]
        );
    };

    // Toggle sort order
    const toggleSortOrder = (field: 'symbol' | 'price' | 'changePercent') => {
        if (sortBy === field) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
        } else {
            setSortBy(field);
            setSortOrder('asc');
        }
    };

    // Loading state
    if (loading) {
        return (
            <View style={[styles.container, styles.centerContent, { backgroundColor: Colors.background }]}>
                <ActivityIndicator size="large" color={Colors.tint} />
                <Text style={[styles.loadingText, { color: Colors.text }]}>
                    Loading watchlist...
                </Text>
            </View>
        );
    }

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: Colors.text }]}>
                    My Watchlist
                </Text>
                <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                    {sortedWatchlist.length} {sortedWatchlist.length === 1 ? 'stock' : 'stocks'}
                </Text>
            </View>

            {/* Stats Row */}
            {watchlist.length > 0 && (
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
                        <Text style={[
                            styles.statValue,
                            {
                                color: watchlist.reduce((sum, s) => sum + s.changePercent, 0) / watchlist.length >= 0
                                    ? '#2E7D32'
                                    : '#C62828'
                            }
                        ]}>
                            {(watchlist.reduce((sum, s) => sum + s.changePercent, 0) / watchlist.length).toFixed(2)}%
                        </Text>
                    </View>
                </View>
            )}

            {/* Sort Options */}
            {watchlist.length > 0 && (
                <View style={styles.sortContainer}>
                    <TouchableOpacity
                        onPress={() => toggleSortOrder('symbol')}
                        style={[
                            styles.sortButton,
                            { backgroundColor: Colors.card }, // ✅ Add inline
                            sortBy === 'symbol' && { backgroundColor: Colors.tint }
                        ]}
                    >
                        <MaterialCommunityIcons
                            name="sort-ascending"
                            size={14}
                            color={sortBy === 'symbol' ? 'white' : Colors.tint}
                        />
                        <Text style={[
                            styles.sortButtonText,
                            sortBy === 'symbol' && { color: 'white', fontWeight: '700' }
                        ]}>
                            Symbol
                        </Text>
                    </TouchableOpacity>

                    <TouchableOpacity
                        onPress={() => toggleSortOrder('price')}
                        style={[
                            styles.sortButton,
                            { backgroundColor: Colors.card }, // ✅ Add inline
                            sortBy === 'price' && { backgroundColor: Colors.tint }
                        ]}
                    >
                        <MaterialCommunityIcons
                            name="currency-usd"
                            size={14}
                            color={sortBy === 'price' ? 'white' : Colors.tint}
                        />
                        <Text style={[
                            styles.sortButtonText,
                            sortBy === 'price' && { color: 'white', fontWeight: '700' }
                        ]}>
                            Price
                        </Text>
                    </TouchableOpacity>

                    <TouchableOpacity
                        onPress={() => toggleSortOrder('changePercent')}
                        style={[
                            styles.sortButton,
                            { backgroundColor: Colors.card }, // ✅ Add inline
                            sortBy === 'changePercent' && { backgroundColor: Colors.tint }
                        ]}
                    >
                        <MaterialCommunityIcons
                            name="percent"
                            size={14}
                            color={sortBy === 'changePercent' ? 'white' : Colors.tint}
                        />
                        <Text style={[
                            styles.sortButtonText,
                            sortBy === 'changePercent' && { color: 'white', fontWeight: '700' }
                        ]}>
                            Change
                        </Text>
                    </TouchableOpacity>
                </View>
            )}

            {/* Watchlist Items */}
            {sortedWatchlist.length > 0 ? (
                <ScrollView
                    style={styles.listContainer}
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={styles.listContent}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor={Colors.tint}
                        />
                    }
                >
                    {sortedWatchlist.map(stock => {
                        const sectorColor = getSectorColor(stock.sector);
                        return (
                            <WatchlistCard
                                key={stock.watchlistId}
                                stock={stock}
                                colors={Colors}
                                sectorColor={sectorColor}
                                onRemove={handleRemoveFromWatchlist}
                                isRemoving={removingStockId === stock.stockId}
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
    centerContent: {
        justifyContent: 'center',
        alignItems: 'center',
    },
    loadingText: {
        marginTop: 12,
        fontSize: 14,
        fontWeight: '600',
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
    sortContainer: {
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