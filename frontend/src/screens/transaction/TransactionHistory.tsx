import React, { useState, useMemo, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TouchableOpacity,
    useColorScheme,
    ScrollView,
    ActivityIndicator,
    RefreshControl,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
import { useAuth } from '@/src/context/AuthContext';
import { getAccountTransactions } from '@/src/services/portfolioService';
import { getStockById } from '@/src/services/entityService';
import { getSectorColor } from '@/src/services/sectorColorService';

interface Transaction {
    id: string;
    symbol: string;
    name: string;
    type: 'buy' | 'sell';
    shares: number;
    price: number;
    total: number;
    date: string;
    sector: string;
}



type SortOption = 'date' | 'symbol' | 'type' | 'amount';
type FilterOption = 'all' | 'buy' | 'sell';

interface TransactionHistoryProps {
    stockSymbol?: string;  // If provided, show only transactions for this stock
    showHeader?: boolean;
    maxTransactions?: number;  // If provided, limit the number of transactions displayed
}

const TransactionCard = ({ transaction, colors }: { transaction: Transaction; colors: any }) => {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const sectorColor = getSectorColor(transaction.sector);
    const isBuy = transaction.type === 'buy';

    const handleStockPress = () => {
        const stockData = {
            symbol: transaction.symbol,
            name: transaction.name,
            price: transaction.price,
            change: 0,
            changePercent: 0,
            sector: transaction.sector,
            marketCap: '0',
            peRatio: '0',
            dividend: '0',
            dayHigh: 0,
            dayLow: 0,
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

        navigation.navigate('StockTicker', { stock: stockData });
    };

    return (
        <TouchableOpacity
            style={[styles.transactionCard, { backgroundColor: colors.card, borderColor: colors.border }]}
            activeOpacity={0.7}
            onPress={handleStockPress}
        >
            {/* Header Row */}
            <View style={styles.cardHeader}>
                <View style={styles.symbolSection}>
                    <View style={[styles.typeIconContainer, { backgroundColor: isBuy ? '#E7F5E7' : '#FCE4E4' }]}>
                        <MaterialCommunityIcons
                            name={isBuy ? 'arrow-down' : 'arrow-up'}
                            size={18}
                            color={isBuy ? '#2E7D32' : '#C62828'}
                        />
                    </View>
                    <View style={styles.stockInfo}>
                        <View style={styles.symbolRow}>
                            <Text style={[styles.symbol, { color: sectorColor.color }]}>
                                {transaction.symbol}
                            </Text>
                            <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                                <Text style={[styles.sectorText, { color: sectorColor.color }]}>
                                    {transaction.sector}
                                </Text>
                            </View>
                        </View>
                        <Text style={[styles.companyName, { color: colors.text, opacity: 0.7 }]}>
                            {transaction.name}
                        </Text>
                    </View>
                </View>
                <View style={styles.dateSection}>
                    <Text style={[styles.date, { color: colors.text, opacity: 0.6 }]}>
                        {new Date(transaction.date).toLocaleDateString('en-AU', {
                            day: 'numeric',
                            month: 'short',
                            year: 'numeric',
                        })}
                    </Text>
                </View>
            </View>

            {/* Details Row */}
            <View style={styles.detailsRow}>
                <View style={styles.detailItem}>
                    <Text style={[styles.detailLabel, { color: colors.text, opacity: 0.6 }]}>
                        Type
                    </Text>
                    <View style={[styles.typeBadge, { backgroundColor: isBuy ? '#E7F5E7' : '#FCE4E4' }]}>
                        <Text style={[styles.typeText, { color: isBuy ? '#2E7D32' : '#C62828' }]}>
                            {isBuy ? 'BUY' : 'SELL'}
                        </Text>
                    </View>
                </View>

                <View style={styles.detailItem}>
                    <Text style={[styles.detailLabel, { color: colors.text, opacity: 0.6 }]}>
                        Shares
                    </Text>
                    <Text style={[styles.detailValue, { color: colors.text }]}>
                        {transaction.shares}
                    </Text>
                </View>

                <View style={styles.detailItem}>
                    <Text style={[styles.detailLabel, { color: colors.text, opacity: 0.6 }]}>
                        Price
                    </Text>
                    <Text style={[styles.detailValue, { color: colors.text }]}>
                        A${transaction.price.toFixed(2)}
                    </Text>
                </View>

                <View style={[styles.detailItem, styles.totalItem]}>
                    <Text style={[styles.detailLabel, { color: colors.text, opacity: 0.6 }]}>
                        Total
                    </Text>
                    <Text style={[styles.totalValue, { color: isBuy ? '#2E7D32' : '#C62828' }]}>
                        {isBuy ? '-' : '+'}A${transaction.total.toFixed(2)}
                    </Text>
                </View>
            </View>
        </TouchableOpacity>
    );
};

export default function TransactionHistory({ stockSymbol, showHeader = true, maxTransactions }: TransactionHistoryProps) {
    const {Colors} = useTheme();
    const { user, activeAccount } = useAuth();

    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [sortBy, setSortBy] = useState<SortOption>('date');
    const [filterBy, setFilterBy] = useState<FilterOption>('all');
    const [showFilters, setShowFilters] = useState(false);
    const [startDate, setStartDate] = useState<string | null>(null);
    const [endDate, setEndDate] = useState<string | null>(null);
    const [showDatePicker, setShowDatePicker] = useState(false);

    // Fetch transactions from backend
    const fetchTransactions = useCallback(async () => {
        if (!user || !activeAccount) {
            setTransactions([]);
            setLoading(false);
            return;
        }

        try {
            setError(null);
            const data = await getAccountTransactions(activeAccount.accountId);

            // Debug: Log the actual API response structure
            console.log('API Response transactions:', data);
            if (data.length > 0) {
                console.log('First transaction structure:', Object.keys(data[0]));
                console.log('First transaction data:', data[0]);
            }

            // Transform backend data to component format
            // Fetch stock details for each transaction
            const transformedData: Awaited<null | {
                id: any;
                symbol: string;
                name: string;
                type: string;
                shares: number;
                price: number;
                total: number;
                date: string;
                sector: string
            }>[] = await Promise.all(
                data.map(async (item: any) => {
                    // Handle multiple possible field names from API
                    const transactionId = item.transactionId || item.id;
                    const stockId = item.stockId || item.stock?.stockId || item.stock?.id;

                    // If we still don't have these required fields, skip this transaction
                    if (!transactionId || !stockId) {
                        console.warn('Skipping transaction - missing required fields:', {
                            keys: Object.keys(item),
                            item
                        });
                        return null;
                    }

                    const total = Number(item.shareQuantity) * Number(item.pricePerShare);

                    // Fetch stock details to get symbol and company name
                    let stockSymbol = 'UNKNOWN';
                    let companyName = `Stock ${String(stockId).substring(0, 8)}`;
                    let sector = 'Other';


                    try {
                        const stockDetails = await getStockById(String(stockId));
                        stockSymbol = stockDetails.stockCode;
                        companyName = stockDetails.companyName;
                        sector = stockDetails.industry || 'Other'; // Backend StockDTO doesn't include sector
                    } catch (error) {
                        console.warn(`Failed to fetch stock details for ${stockId}:`, error);
                        // Use fallback values
                    }

                    // Parse transaction date - try transactionDate first (backend), then createdAt
                    const dateString = item.transactionDate || item.createdAt || new Date().toISOString();
                    const transactionDate = new Date(dateString).toISOString().split('T')[0];

                    return {
                        id: transactionId,
                        symbol: stockSymbol,
                        name: companyName,
                        type: item.transactionType === 'BUY' ? 'buy' : 'sell',
                        shares: Number(item.shareQuantity),
                        price: Number(item.pricePerShare),
                        total: total,
                        date: transactionDate,
                        sector: sector,
                    };
                })
            );

            // Filter out null entries (skipped transactions)
            const validTransactions = transformedData.filter((t): t is Transaction => t !== null);
            setTransactions(validTransactions);
        } catch (error: any) {
            console.error('Failed to fetch transactions:', error);
            setError(error.message || 'Failed to load transactions');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [user, activeAccount]);

    // Initial fetch and refetch when account changes
    useEffect(() => {
        setLoading(true);
        fetchTransactions();
    }, [fetchTransactions]);

    // Pull to refresh
    const onRefresh = useCallback(() => {
        setRefreshing(true);
        fetchTransactions();
    }, [fetchTransactions]);

    // Filter transactions by stock symbol if provided
    const filteredByStock = stockSymbol
        ? transactions.filter(t => t.symbol === stockSymbol)
        : transactions;

    // Apply type filter, date range filter, and sort
    const processedTransactions = useMemo(() => {
        let filtered = filteredByStock;

        // Filter by type
        if (filterBy !== 'all') {
            filtered = filtered.filter(t => t.type === filterBy);
        }

        // Filter by date range
        if (startDate || endDate) {
            filtered = filtered.filter(t => {
                const transactionDate = new Date(t.date);
                if (startDate && transactionDate < new Date(startDate)) {
                    return false;
                }
                if (endDate) {
                    const endDateTime = new Date(endDate);
                    endDateTime.setHours(23, 59, 59, 999); // Include entire end date
                    if (transactionDate > endDateTime) {
                        return false;
                    }
                }
                return true;
            });
        }

        // Sort
        const sorted = [...filtered].sort((a, b) => {
            switch (sortBy) {
                case 'date':
                    return new Date(b.date).getTime() - new Date(a.date).getTime();
                case 'symbol':
                    return a.symbol.localeCompare(b.symbol);
                case 'type':
                    return a.type.localeCompare(b.type);
                case 'amount':
                    return b.total - a.total;
                default:
                    return 0;
            }
        });

        // Limit to maxTransactions if specified
        if (maxTransactions && maxTransactions > 0) {
            return sorted.slice(0, maxTransactions);
        }

        return sorted;
    }, [filteredByStock, sortBy, filterBy, startDate, endDate, maxTransactions]);

    // Calculate summary stats
    const stats = useMemo(() => {
        const totalBuy = processedTransactions
            .filter(t => t.type === 'buy')
            .reduce((sum, t) => sum + t.total, 0);
        const totalSell = processedTransactions
            .filter(t => t.type === 'sell')
            .reduce((sum, t) => sum + t.total, 0);
        const netAmount = totalSell - totalBuy;

        return { totalBuy, totalSell, netAmount, count: processedTransactions.length };
    }, [processedTransactions]);

    // Loading state
    if (loading) {
        return (
            <View style={[styles.container, styles.centerContent]}>
                <ActivityIndicator size="large" color={Colors.tint} />
                <Text style={[styles.loadingText, { color: Colors.text, marginTop: 16 }]}>
                    Loading transactions...
                </Text>
            </View>
        );
    }

    // Error state
    if (error) {
        return (
            <View style={[styles.container, styles.centerContent, { paddingHorizontal: 24 }]}>
                <MaterialCommunityIcons
                    name="alert-circle-outline"
                    size={56}
                    color={Colors.text}
                    style={{ opacity: 0.3, marginBottom: 16 }}
                />
                <Text style={[styles.errorTitle, { color: Colors.text, marginBottom: 8 }]}>
                    Failed to Load Transactions
                </Text>
                <Text style={[styles.errorSubtitle, { color: Colors.text, opacity: 0.6, marginBottom: 24, textAlign: 'center' }]}>
                    {error}
                </Text>
                <TouchableOpacity
                    onPress={() => {
                        setLoading(true);
                        fetchTransactions();
                    }}
                    style={[styles.retryButton, { backgroundColor: Colors.tint }]}
                >
                    <MaterialCommunityIcons name="refresh" size={20} color="white" />
                    <Text style={styles.retryButtonText}>Retry</Text>
                </TouchableOpacity>
            </View>
        );
    }

    return (
        <ScrollView
            style={styles.scrollContainer}
            contentContainerStyle={styles.container}
            showsVerticalScrollIndicator={false}
            refreshControl={
                <RefreshControl
                    refreshing={refreshing}
                    onRefresh={onRefresh}
                    tintColor={Colors.tint}
                    colors={[Colors.tint]}
                />
            }
        >
            {showHeader && (
                <>
                    {/* Summary Stats */}
                    {transactions.length > 0 && (
                        <View style={[styles.statsContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <View style={styles.statBlock}>
                                <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Total Transactions
                                </Text>
                                <Text style={[styles.statValue, { color: Colors.text }]}>
                                    {stats.count}
                                </Text>
                            </View>
                            <View style={[styles.statDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.statBlock}>
                                <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Total Bought
                                </Text>
                                <Text style={[styles.statValue, { color: '#C62828' }]}>
                                    A${stats.totalBuy.toLocaleString('en-AU', { minimumFractionDigits: 2 })}
                                </Text>
                            </View>
                            <View style={[styles.statDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.statBlock}>
                                <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Total Sold
                                </Text>
                                <Text style={[styles.statValue, { color: '#2E7D32' }]}>
                                    A${stats.totalSell.toLocaleString('en-AU', { minimumFractionDigits: 2 })}
                                </Text>
                            </View>
                        </View>
                    )}

                    {/* Filter and Sort Controls */}
                    {transactions.length > 0 && (
                        <View style={styles.controlsContainer}>
                            <TouchableOpacity
                                onPress={() => setShowFilters(!showFilters)}
                                style={[styles.filterButton, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                            >
                                <MaterialCommunityIcons
                                    name="filter-variant"
                                    size={16}
                                    color={Colors.tint}
                                />
                                <Text style={[styles.filterButtonText, { color: Colors.tint }]}>
                                    Filter & Sort
                                </Text>
                                <MaterialCommunityIcons
                                    name={showFilters ? 'chevron-up' : 'chevron-down'}
                                    size={16}
                                    color={Colors.tint}
                                />
                            </TouchableOpacity>
                        </View>
                    )}

                    {showFilters && (
                        <View style={[styles.filtersPanel, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            {/* Filter by Type */}
                            <View style={styles.filterSection}>
                                <Text style={[styles.filterTitle, { color: Colors.text }]}>
                                    Filter by Type
                                </Text>
                                <View style={styles.filterOptions}>
                                    {(['all', 'buy', 'sell'] as FilterOption[]).map(option => (
                                        <TouchableOpacity
                                            key={option}
                                            onPress={() => setFilterBy(option)}
                                            style={[
                                                styles.filterOption,
                                                {
                                                    backgroundColor: filterBy === option ? Colors.tint : Colors.background,
                                                    borderColor: Colors.border,
                                                },
                                            ]}
                                        >
                                            <Text
                                                style={[
                                                    styles.filterOptionText,
                                                    { color: filterBy === option ? 'white' : Colors.text },
                                                ]}
                                            >
                                                {option.charAt(0).toUpperCase() + option.slice(1)}
                                            </Text>
                                        </TouchableOpacity>
                                    ))}
                                </View>
                            </View>

                            {/* Sort Options */}
                            <View style={styles.filterSection}>
                                <Text style={[styles.filterTitle, { color: Colors.text }]}>
                                    Sort by
                                </Text>
                                <View style={styles.filterOptions}>
                                    {([
                                        { key: 'date', label: 'Date' },
                                        { key: 'symbol', label: 'Symbol' },
                                        { key: 'type', label: 'Type' },
                                        { key: 'amount', label: 'Amount' },
                                    ] as { key: SortOption; label: string }[]).map(option => (
                                        <TouchableOpacity
                                            key={option.key}
                                            onPress={() => setSortBy(option.key)}
                                            style={[
                                                styles.filterOption,
                                                {
                                                    backgroundColor: sortBy === option.key ? Colors.tint : Colors.background,
                                                    borderColor: Colors.border,
                                                },
                                            ]}
                                        >
                                            <Text
                                                style={[
                                                    styles.filterOptionText,
                                                    { color: sortBy === option.key ? 'white' : Colors.text },
                                                ]}
                                            >
                                                {option.label}
                                            </Text>
                                        </TouchableOpacity>
                                    ))}
                                </View>
                            </View>

                            {/* Date Range Filter */}
                            <View style={styles.filterSection}>
                                <Text style={[styles.filterTitle, { color: Colors.text }]}>
                                    Date Range
                                </Text>
                                <View style={styles.dateRangeContainer}>
                                    <View style={styles.dateInputField}>
                                        <Text style={[styles.dateLabel, { color: Colors.text, opacity: 0.6 }]}>
                                            From
                                        </Text>
                                        <TouchableOpacity
                                            onPress={() => {
                                                // For now, show a simple date input
                                                // In a full implementation, would use a date picker
                                                const today = new Date().toISOString().split('T')[0];
                                                setStartDate(startDate === today ? null : today);
                                            }}
                                            style={[styles.dateButton, { backgroundColor: Colors.background, borderColor: Colors.border }]}
                                        >
                                            <Text style={[styles.dateButtonText, { color: Colors.text }]}>
                                                {startDate ? startDate : 'Select date'}
                                            </Text>
                                        </TouchableOpacity>
                                    </View>
                                    <View style={styles.dateInputField}>
                                        <Text style={[styles.dateLabel, { color: Colors.text, opacity: 0.6 }]}>
                                            To
                                        </Text>
                                        <TouchableOpacity
                                            onPress={() => {
                                                const today = new Date().toISOString().split('T')[0];
                                                setEndDate(endDate === today ? null : today);
                                            }}
                                            style={[styles.dateButton, { backgroundColor: Colors.background, borderColor: Colors.border }]}
                                        >
                                            <Text style={[styles.dateButtonText, { color: Colors.text }]}>
                                                {endDate ? endDate : 'Select date'}
                                            </Text>
                                        </TouchableOpacity>
                                    </View>
                                </View>
                                {(startDate || endDate) && (
                                    <TouchableOpacity
                                        onPress={() => {
                                            setStartDate(null);
                                            setEndDate(null);
                                        }}
                                        style={[styles.clearDateButton, { backgroundColor: Colors.background, borderColor: Colors.border }]}
                                    >
                                        <MaterialCommunityIcons name="close" size={16} color={Colors.text} />
                                        <Text style={[styles.clearDateText, { color: Colors.text }]}>
                                            Clear dates
                                        </Text>
                                    </TouchableOpacity>
                                )}
                            </View>
                        </View>
                    )}
                </>
            )}

            {/* Transaction List */}
            <View style={styles.transactionList}>
                {processedTransactions.length === 0 ? (
                    <View style={[styles.emptyState, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <MaterialCommunityIcons
                            name="history"
                            size={48}
                            color={Colors.text}
                            style={{ opacity: 0.3 }}
                        />
                        <Text style={[styles.emptyText, { color: Colors.text, opacity: 0.6 }]}>
                            {stockSymbol ? `No transactions for ${stockSymbol}` : 'No transactions yet'}
                        </Text>
                    </View>
                ) : (
                    processedTransactions.map(transaction => (
                        <TransactionCard
                            key={transaction.id}
                            transaction={transaction}
                            colors={Colors}
                        />
                    ))
                )}
            </View>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    scrollContainer: {
        flex: 1,
    },
    container: {
        gap: 16,
        paddingBottom: 24,
    },
    centerContent: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    statsContainer: {
        flexDirection: 'row',
        borderWidth: 1,
        borderRadius: 12,
        overflow: 'hidden',
    },
    statBlock: {
        flex: 1,
        paddingVertical: 12,
        paddingHorizontal: 8,
        alignItems: 'center',
    },
    statLabel: {
        fontSize: 10,
        fontWeight: '600',
        marginBottom: 4,
        textAlign: 'center',
    },
    statValue: {
        fontSize: 14,
        fontWeight: '800',
    },
    statDivider: {
        width: 1,
    },
    controlsContainer: {
        flexDirection: 'row',
        gap: 8,
    },
    filterButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 10,
        borderWidth: 1,
        borderRadius: 10,
    },
    filterButtonText: {
        fontSize: 12,
        fontWeight: '700',
    },
    filtersPanel: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        gap: 16,
    },
    filterSection: {
        gap: 10,
    },
    filterTitle: {
        fontSize: 12,
        fontWeight: '700',
    },
    filterOptions: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 8,
    },
    filterOption: {
        paddingHorizontal: 14,
        paddingVertical: 8,
        borderRadius: 8,
        borderWidth: 1,
    },
    filterOptionText: {
        fontSize: 11,
        fontWeight: '600',
    },
    transactionList: {
        gap: 12,
    },
    transactionCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        gap: 12,
    },
    cardHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
    },
    symbolSection: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        gap: 10,
        flex: 1,
    },
    typeIconContainer: {
        width: 36,
        height: 36,
        borderRadius: 8,
        alignItems: 'center',
        justifyContent: 'center',
    },
    stockInfo: {
        flex: 1,
        gap: 4,
    },
    symbolRow: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    symbol: {
        fontSize: 15,
        fontWeight: '800',
    },
    sectorBadge: {
        paddingHorizontal: 8,
        paddingVertical: 2,
        borderRadius: 6,
    },
    sectorText: {
        fontSize: 9,
        fontWeight: '700',
    },
    companyName: {
        fontSize: 11,
        fontWeight: '500',
    },
    dateSection: {
        alignItems: 'flex-end',
    },
    date: {
        fontSize: 10,
        fontWeight: '600',
    },
    detailsRow: {
        flexDirection: 'row',
        gap: 12,
    },
    detailItem: {
        flex: 1,
        gap: 4,
    },
    totalItem: {
        alignItems: 'flex-end',
    },
    detailLabel: {
        fontSize: 9,
        fontWeight: '600',
    },
    detailValue: {
        fontSize: 12,
        fontWeight: '700',
    },
    typeBadge: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
        alignSelf: 'flex-start',
    },
    typeText: {
        fontSize: 10,
        fontWeight: '800',
    },
    totalValue: {
        fontSize: 13,
        fontWeight: '800',
    },
    emptyState: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 40,
        alignItems: 'center',
        gap: 12,
    },
    emptyText: {
        fontSize: 13,
        fontWeight: '600',
    },
    loadingText: {
        fontSize: 14,
        fontWeight: '600',
    },
    errorTitle: {
        fontSize: 18,
        fontWeight: '700',
    },
    errorSubtitle: {
        fontSize: 14,
        fontWeight: '500',
    },
    retryButton: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
        paddingHorizontal: 24,
        paddingVertical: 12,
        borderRadius: 10,
    },
    retryButtonText: {
        fontSize: 14,
        fontWeight: '700',
        color: 'white',
    },
    dateRangeContainer: {
        flexDirection: 'row',
        gap: 12,
    },
    dateInputField: {
        flex: 1,
        gap: 6,
    },
    dateLabel: {
        fontSize: 11,
        fontWeight: '600',
    },
    dateButton: {
        borderWidth: 1,
        borderRadius: 8,
        paddingHorizontal: 12,
        paddingVertical: 10,
        alignItems: 'center',
        justifyContent: 'center',
    },
    dateButtonText: {
        fontSize: 12,
        fontWeight: '500',
    },
    clearDateButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 6,
        borderWidth: 1,
        borderRadius: 8,
        paddingVertical: 8,
        marginTop: 8,
    },
    clearDateText: {
        fontSize: 11,
        fontWeight: '600',
    },
});
