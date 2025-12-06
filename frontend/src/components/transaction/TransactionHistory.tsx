import React, { useState, useMemo } from 'react';
import {
    View,
    Text,
    StyleSheet,
    TouchableOpacity,
    useColorScheme,
    ScrollView,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '@/src/constants/colors';
import { useRouter } from 'expo-router';

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

const sectorColors: Record<string, { color: string; bgLight: string }> = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Markets': { color: '#7C3AED', bgLight: '#F5F3FF' },
};

// Mock transaction data
const mockTransactions: Transaction[] = [
    {
        id: '1',
        symbol: 'AAPL',
        name: 'Apple Inc.',
        type: 'buy',
        shares: 50,
        price: 145.32,
        total: 7266.00,
        date: '2024-12-01',
        sector: 'Technology',
    },
    {
        id: '2',
        symbol: 'NVDA',
        name: 'NVIDIA Corporation',
        type: 'buy',
        shares: 30,
        price: 495.22,
        total: 14856.60,
        date: '2024-11-28',
        sector: 'Semiconductors',
    },
    {
        id: '3',
        symbol: 'MSFT',
        name: 'Microsoft Corporation',
        type: 'buy',
        shares: 25,
        price: 378.91,
        total: 9472.75,
        date: '2024-11-25',
        sector: 'Technology',
    },
    {
        id: '4',
        symbol: 'TSLA',
        name: 'Tesla Inc.',
        type: 'sell',
        shares: 10,
        price: 242.84,
        total: 2428.40,
        date: '2024-11-20',
        sector: 'Consumer/Tech',
    },
    {
        id: '5',
        symbol: 'GOOGL',
        name: 'Alphabet Inc.',
        type: 'buy',
        shares: 40,
        price: 139.57,
        total: 5582.80,
        date: '2024-11-18',
        sector: 'Technology',
    },
    {
        id: '6',
        symbol: 'AMD',
        name: 'Advanced Micro Devices',
        type: 'buy',
        shares: 60,
        price: 122.45,
        total: 7347.00,
        date: '2024-11-15',
        sector: 'Semiconductors',
    },
    {
        id: '7',
        symbol: 'AMZN',
        name: 'Amazon.com Inc.',
        type: 'buy',
        shares: 35,
        price: 173.12,
        total: 6059.20,
        date: '2024-11-10',
        sector: 'Consumer/Tech',
    },
    {
        id: '8',
        symbol: 'TSLA',
        name: 'Tesla Inc.',
        type: 'buy',
        shares: 20,
        price: 238.50,
        total: 4770.00,
        date: '2024-11-05',
        sector: 'Consumer/Tech',
    },
];

type SortOption = 'date' | 'symbol' | 'type' | 'amount';
type FilterOption = 'all' | 'buy' | 'sell';

interface TransactionHistoryProps {
    stockSymbol?: string;  // If provided, show only transactions for this stock
    showHeader?: boolean;
}

const TransactionCard = ({ transaction, colors }: { transaction: Transaction; colors: any }) => {
    const router = useRouter();
    const sectorColor = sectorColors[transaction.sector] || sectorColors['Technology'];
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

        router.push({
            pathname: '/stock/[ticker]',
            params: {
                ticker: transaction.symbol,
                stock: JSON.stringify(stockData),
            },
        });
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

export default function TransactionHistory({ stockSymbol, showHeader = true }: TransactionHistoryProps) {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    const [sortBy, setSortBy] = useState<SortOption>('date');
    const [filterBy, setFilterBy] = useState<FilterOption>('all');
    const [showFilters, setShowFilters] = useState(false);

    // Filter transactions by stock symbol if provided
    const filteredByStock = stockSymbol
        ? mockTransactions.filter(t => t.symbol === stockSymbol)
        : mockTransactions;

    // Apply type filter and sort
    const processedTransactions = useMemo(() => {
        let filtered = filteredByStock;

        // Filter by type
        if (filterBy !== 'all') {
            filtered = filtered.filter(t => t.type === filterBy);
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

        return sorted;
    }, [filteredByStock, sortBy, filterBy]);

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

    return (
        <View style={styles.container}>
            {showHeader && (
                <>
                    {/* Summary Stats */}
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

                    {/* Filter and Sort Controls */}
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
                            {stockSymbol ? `No transactions for ${stockSymbol}` : 'No transactions found'}
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
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        gap: 16,
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
});
