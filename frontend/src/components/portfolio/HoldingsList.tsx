import { getSectorColor } from '@/src/services/sectorColorService';
import React, { useState, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Image,
    ActivityIndicator,
    RefreshControl,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '../../constants/colors';
import { useRouter } from 'expo-router';
import { useAuth } from '@/src/context/AuthContext';
import { getAccountHoldings } from '@/src/services/portfolioService';
import { getStockById } from '@/src/services/entityService';

interface Holding {
    id: string;
    symbol: string;
    company: string;
    shares: number;
    amountInvested: number;
    currentValue: number;
    returnAmount: number;
    returnPercent: number;
    sector: string;
    image?: any;
}

interface HoldingsListProps {
    holdings?: Holding[];
    onHoldingPress?: (holding: Holding) => void;
    onRefresh?: () => void;
}


const HoldingCard = ({
                         holding,
                         colors,
                         onPress,
                         sectorColor,
                     }: {
    holding: Holding;
    colors: any;
    onPress?: () => void;
    sectorColor: any;
}) => {
    const router = useRouter();
    const [isExpanded, setIsExpanded] = React.useState(true);
    const isPositive = holding.returnPercent >= 0;
    const costPerShare = holding.amountInvested / holding.shares;
    const currentPerShare = holding.currentValue / holding.shares;

    const handleNavigateToStock = () => {
        // Build stock data object for the ticker page
        const stockData = {
            symbol: holding.symbol,
            name: holding.company,
            price: currentPerShare,
            change: holding.returnAmount / holding.shares, // Per share change
            changePercent: holding.returnPercent,
            sector: holding.sector,
            marketCap: '0',
            peRatio: '0',
            dividend: '0',
            dayHigh: currentPerShare,
            dayLow: currentPerShare,
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

        // Navigate to stock ticker page
        router.push({
            pathname: '/stock/[ticker]',
            params: {
                ticker: holding.symbol,
                stock: JSON.stringify(stockData),
            },
        });
    };

    return (
        <TouchableOpacity
            onPress={handleNavigateToStock}
            onLongPress={() => setIsExpanded(!isExpanded)}
            activeOpacity={0.7}
            style={[
                styles.holdingCard,
                {
                    backgroundColor: colors.card,
                    borderColor: colors.border,
                }
            ]}
        >
            {/* Top Row - Symbol, Company, Sector */}
            <View style={styles.cardTop}>
                <View style={styles.companyInfo}>
                    {holding.image && (
                        <Image
                            source={holding.image}
                            style={styles.companyImage}
                            resizeMode="contain"
                        />
                    )}
                    <View style={{ flex: 1 }}>
                        <Text style={[styles.symbol, { color: sectorColor.color }]}>
                            {holding.symbol}
                        </Text>
                        <Text style={[styles.company, { color: colors.text, opacity: 0.7 }]} numberOfLines={1}>
                            {holding.company}
                        </Text>
                    </View>
                </View>

                <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                    <Text style={[styles.sectorText, { color: sectorColor.color }]}>
                        {holding.sector}
                    </Text>
                </View>
            </View>

            {/* Collapsed View - Quick Stats */}
            {!isExpanded && (
                <View style={styles.collapsedRow}>
                    <View style={styles.collapsedValue}>
                        <Text style={[styles.collapsedLabel, { color: colors.text, opacity: 0.6 }]}>
                            Value
                        </Text>
                        <Text style={[styles.collapsedAmount, { color: colors.text }]}>
                            A${holding.currentValue.toLocaleString('en-AU', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                        </Text>
                    </View>

                    <View style={[styles.collapsedReturnBadge, { backgroundColor: isPositive ? '#E7F5E7' : '#FCE4E4' }]}>
                        <MaterialCommunityIcons
                            name={isPositive ? 'trending-up' : 'trending-down'}
                            size={14}
                            color={isPositive ? '#2E7D32' : '#C62828'}
                        />
                        <Text style={[
                            styles.collapsedReturnText,
                            { color: isPositive ? '#2E7D32' : '#C62828' }
                        ]}>
                            {isPositive ? '+' : ''}{holding.returnPercent.toFixed(2)}%
                        </Text>
                    </View>
                </View>
            )}

            {/* Expanded View - Full Details */}
            {isExpanded && (
                <>
                    {/* Middle Row - Shares */}
                    <View style={styles.sharesRow}>
                        <Text style={[styles.sharesLabel, { color: colors.text, opacity: 0.6 }]}>
                            {holding.shares.toFixed(2)} shares
                        </Text>
                        <Text style={[styles.costPerShare, { color: colors.text, opacity: 0.6 }]}>
                            @ A${costPerShare.toFixed(2)} → A${currentPerShare.toFixed(2)}
                        </Text>
                    </View>

                    {/* Bottom Row - Values */}
                    <View style={styles.valuesRow}>
                        <View style={styles.valueColumn}>
                            <Text style={[styles.valueLabel, { color: colors.text, opacity: 0.6 }]}>
                                Invested
                            </Text>
                            <Text style={[styles.valueAmount, { color: colors.text }]}>
                                A${holding.amountInvested.toLocaleString('en-AU', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </Text>
                        </View>

                        <View style={styles.divider} />

                        <View style={styles.valueColumn}>
                            <Text style={[styles.valueLabel, { color: colors.text, opacity: 0.6 }]}>
                                Current Value
                            </Text>
                            <Text style={[styles.valueAmount, { color: colors.text }]}>
                                A${holding.currentValue.toLocaleString('en-AU', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </Text>
                        </View>

                        <View style={styles.divider} />

                        <View style={styles.valueColumn}>
                            <Text style={[styles.valueLabel, { color: colors.text, opacity: 0.6 }]}>
                                Return
                            </Text>
                            <View style={styles.returnValue}>
                                <MaterialCommunityIcons
                                    name={isPositive ? 'trending-up' : 'trending-down'}
                                    size={14}
                                    color={isPositive ? '#2E7D32' : '#C62828'}
                                    style={{ marginRight: 3 }}
                                />
                                <Text style={[
                                    styles.returnPercent,
                                    { color: isPositive ? '#2E7D32' : '#C62828' }
                                ]}>
                                    {isPositive ? '+' : ''}{holding.returnPercent.toFixed(2)}%
                                </Text>
                            </View>
                            <Text style={[
                                styles.returnAmount,
                                { color: isPositive ? '#2E7D32' : '#C62828' }
                            ]}>
                                {isPositive ? '+' : ''}A${Math.abs(holding.returnAmount).toLocaleString('en-AU', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </Text>
                        </View>
                    </View>
                </>
            )}
        </TouchableOpacity>
    );
};

export const HoldingsList: React.FC<HoldingsListProps> = ({
                                                              holdings: providedHoldings,
                                                              onHoldingPress,
                                                              onRefresh: onRefreshCallback,
                                                          }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const { user, activeAccount } = useAuth();

    const [internalHoldings, setInternalHoldings] = useState<Holding[]>([]);
    const [loading, setLoading] = useState(!providedHoldings);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [sortBy, setSortBy] = useState<'value' | 'return' | 'invested'>('value');

    // Use provided holdings if available, otherwise fetch
    const holdings = providedHoldings || internalHoldings;

    // Fetch holdings from backend (only if not provided)
    // In the HoldingsList component, modify the fetchHoldings function:

    const fetchHoldings = useCallback(async () => {
        if (providedHoldings) return;

        if (!user || !activeAccount) {
            setInternalHoldings([])
            setLoading(false);
            return;
        }

        try {
            setError(null);
            const data = await getAccountHoldings(activeAccount.accountId);

            console.log('📥 Raw API response:', JSON.stringify(data, null, 2));

            // Transform backend data to component format
            const transformedData: Holding[] = data
                .filter((item) => item.quantity > 0) // ✅ ADD THIS LINE - Filter out zero shares
                .map((item) => {
                    console.log(`🔍 Processing ${item.stockSymbol}:`, {
                        companyName: item.companyName,
                        sector: item.sector,
                    });

                    const currentValue = item.currentValue || (item.quantity * item.currentPrice);
                    const amountInvested = item.totalCostBasis;
                    const returnAmount = item.unrealizedGain;
                    const returnPercent = item.unrealizedGainPercent;

                    return {
                        id: item.holdingId,
                        symbol: item.stockSymbol,
                        company: item.companyName || item.stockSymbol,
                        shares: item.quantity,
                        amountInvested,
                        currentValue,
                        returnAmount,
                        returnPercent,
                        sector: item.sector || 'Unknown',
                    };
                });

            console.log('📋 Final transformed holdings:', transformedData);

            setInternalHoldings(transformedData);
        } catch (error: any) {
            console.error('Failed to fetch holdings:', error);
            setError(error.message || 'Failed to load holdings');
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    }, [user, activeAccount, providedHoldings]);

    // Initial fetch and refetch when account changes (only if not provided)
    useEffect(() => {
        if (!providedHoldings) {
            setLoading(true);
            fetchHoldings();
        }
    }, [fetchHoldings, providedHoldings]);

    // Pull to refresh
    const onRefresh = useCallback(() => {
        setRefreshing(true);
        if (onRefreshCallback) {
            onRefreshCallback();
            // Reset refreshing after a delay
            setTimeout(() => setRefreshing(false), 1000);
        } else {
            fetchHoldings();
        }
    }, [fetchHoldings, onRefreshCallback]);

    // Calculate totals
    const totalInvested = holdings.reduce((sum, h) => sum + h.amountInvested, 0);
    const totalValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);
    const totalReturn = totalValue - totalInvested;
    const totalReturnPercent = totalInvested > 0 ? (totalReturn / totalInvested) * 100 : 0;

    // Sort holdings
    const sortedHoldings = [...holdings].sort((a, b) => {
        switch (sortBy) {
            case 'value':
                return b.currentValue - a.currentValue;
            case 'return':
                return b.returnPercent - a.returnPercent;
            case 'invested':
                return b.amountInvested - a.amountInvested;
            default:
                return 0;
        }
    });


    // Loading state
    if (loading) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background, alignItems: 'center', justifyContent: 'center' }]}>
                <ActivityIndicator size="large" color={Colors.tint} />
                <Text style={[styles.loadingText, { color: Colors.text, marginTop: 16 }]}>
                    Loading holdings...
                </Text>
            </View>
        );
    }

    // Error state
    if (error) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 24 }]}>
                <MaterialCommunityIcons
                    name="alert-circle-outline"
                    size={56}
                    color={Colors.text}
                    style={{ opacity: 0.3, marginBottom: 16 }}
                />
                <Text style={[styles.errorTitle, { color: Colors.text, marginBottom: 8 }]}>
                    Failed to Load Holdings
                </Text>
                <Text style={[styles.errorSubtitle, { color: Colors.text, opacity: 0.6, marginBottom: 24, textAlign: 'center' }]}>
                    {error}
                </Text>
                <TouchableOpacity
                    onPress={() => {
                        setLoading(true);
                        fetchHoldings();
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
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: Colors.text }]}>
                    My Holdings
                </Text>
                <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                    {holdings.length} active positions
                </Text>
            </View>

            {/* Summary Card */}
            {holdings.length > 0 && (
                <View style={[styles.summaryCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <View style={styles.summaryRow}>
                        <View style={styles.summaryItem}>
                            <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.6 }]}>
                                Total Invested
                            </Text>
                            <Text style={[styles.summaryValue, { color: Colors.text }]}>
                                A${totalInvested.toLocaleString('en-AU', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                            </Text>
                        </View>

                        <View style={[styles.summarySeparator, { backgroundColor: Colors.border }]} />

                        <View style={styles.summaryItem}>
                            <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.6 }]}>
                                Current Value
                            </Text>
                            <Text style={[styles.summaryValue, { color: Colors.text }]}>
                                A${totalValue.toLocaleString('en-AU', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                            </Text>
                        </View>

                        <View style={[styles.summarySeparator, { backgroundColor: Colors.border }]} />

                        <View style={styles.summaryItem}>
                            <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.6 }]}>
                                Total Return
                            </Text>
                            <View style={styles.returnBadge}>
                                <MaterialCommunityIcons
                                    name={totalReturn >= 0 ? 'trending-up' : 'trending-down'}
                                    size={14}
                                    color={totalReturn >= 0 ? '#2E7D32' : '#C62828'}
                                />
                                <Text style={[
                                    styles.summaryReturn,
                                    { color: totalReturn >= 0 ? '#2E7D32' : '#C62828' }
                                ]}>
                                    {totalReturn >= 0 ? '+' : ''}A${Math.abs(totalReturn).toLocaleString('en-AU', { minimumFractionDigits: 0, maximumFractionDigits: 0 })} ({totalReturnPercent.toFixed(2)}%)
                                </Text>
                            </View>
                        </View>
                    </View>
                </View>
            )}

            {/* Sort Options */}
            {holdings.length > 0 && (
                <View style={styles.sortContainer}>
                    {(['value', 'return', 'invested'] as const).map((option) => (
                        <TouchableOpacity
                            key={option}
                            onPress={() => setSortBy(option)}
                            style={[
                                styles.sortButton,
                                sortBy === option && [
                                    styles.sortButtonActive,
                                    { backgroundColor: Colors.tint }
                                ],
                                sortBy !== option && { backgroundColor: Colors.card }
                            ]}
                        >
                            <Text
                                style={[
                                    styles.sortButtonText,
                                    sortBy === option && { color: 'white', fontWeight: '700' }
                                ]}
                            >
                                {option === 'value' ? 'Value' : option === 'return' ? 'Return' : 'Invested'}
                            </Text>
                        </TouchableOpacity>
                    ))}
                </View>
            )}

            {/* Holdings List */}
            {sortedHoldings.length > 0 ? (
                <ScrollView
                    showsVerticalScrollIndicator={false}
                    style={styles.holdingsList}
                    contentContainerStyle={styles.holdingsContent}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor={Colors.tint}
                            colors={[Colors.tint]}
                        />
                    }
                >
                    {sortedHoldings.map((holding) => {
                        const sectorColor = getSectorColor(holding.sector);
                        return (
                            <HoldingCard
                                key={holding.id}
                                holding={holding}
                                colors={Colors}
                                onPress={() => onHoldingPress?.(holding)}
                                sectorColor={sectorColor}
                            />
                        );
                    })}
                </ScrollView>
            ) : (
                <ScrollView
                    showsVerticalScrollIndicator={false}
                    style={styles.holdingsList}
                    contentContainerStyle={styles.emptyStateContainer}
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={onRefresh}
                            tintColor={Colors.tint}
                            colors={[Colors.tint]}
                        />
                    }
                >
                    <View style={styles.emptyState}>
                        <MaterialCommunityIcons
                            name="briefcase-outline"
                            size={56}
                            color={Colors.text}
                            style={{ opacity: 0.3, marginBottom: 16 }}
                        />
                        <Text style={[styles.emptyStateTitle, { color: Colors.text }]}>
                            No holdings yet
                        </Text>
                        <Text style={[styles.emptyStateSubtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Start investing to build your portfolio
                        </Text>
                    </View>
                </ScrollView>
            )}
        </View>
    );
};

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
    summaryCard: {
        marginHorizontal: 0,
        marginBottom: 16,
        borderRadius: 12,
        padding: 14,
    },
    summaryRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    summaryItem: {
        flex: 1,
    },
    summaryLabel: {
        fontSize: 11,
        fontWeight: '600',
        marginBottom: 4,
    },
    summaryValue: {
        fontSize: 14,
        fontWeight: '700',
    },
    summarySeparator: {
        width: 1,
        height: 40,
        marginHorizontal: 8,
    },
    returnBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 3,
    },
    summaryReturn: {
        fontSize: 12,
        fontWeight: '700',
    },
    sortContainer: {
        flexDirection: 'row',
        paddingHorizontal: 12,
        marginBottom: 16,
        gap: 8,
    },
    sortButton: {
        paddingHorizontal: 0,
        paddingVertical: 6,
        borderRadius: 8,
        flex: 1,
        alignItems: 'center',
    },
    sortButtonActive: {},
    sortButtonText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
    },
    holdingsList: {
        flex: 1,
    },
    holdingsContent: {
        paddingBottom: 24,
        gap: 12,
    },
    holdingCard: {
        borderRadius: 12,
        padding: 14,
        position: 'relative',
    },
    cardTop: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'flex-start',
        marginBottom: 10,
    },
    companyInfo: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 10,
        flex: 1,
    },
    companyImage: {
        width: 36,
        height: 36,
        borderRadius: 8,
    },
    symbol: {
        fontSize: 14,
        fontWeight: '700',
        marginBottom: 2,
    },
    company: {
        fontSize: 12,
        fontWeight: '500',
    },
    sectorBadge: {
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
    },
    sectorText: {
        fontSize: 10,
        fontWeight: '600',
    },
    sharesRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 10,
        paddingVertical: 6,
    },
    sharesLabel: {
        fontSize: 11,
        fontWeight: '600',
    },
    costPerShare: {
        fontSize: 10,
        fontWeight: '500',
    },
    valuesRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    valueColumn: {
        flex: 1,
        alignItems: 'center',
    },
    valueLabel: {
        fontSize: 10,
        fontWeight: '600',
        marginBottom: 4,
    },
    valueAmount: {
        fontSize: 12,
        fontWeight: '700',
    },
    divider: {
        width: 1,
        height: 40,
        backgroundColor: '#E0E0E0',
        marginHorizontal: 8,
    },
    returnValue: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
    },
    returnPercent: {
        fontSize: 12,
        fontWeight: '700',
    },
    returnAmount: {
        fontSize: 10,
        fontWeight: '600',
        marginTop: 2,
    },
    collapsedRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginTop: 8,
    },
    collapsedValue: {
        flex: 1,
    },
    collapsedLabel: {
        fontSize: 10,
        fontWeight: '600',
        marginBottom: 2,
    },
    collapsedAmount: {
        fontSize: 14,
        fontWeight: '700',
    },
    collapsedReturnBadge: {
        flexDirection: 'row',
        paddingHorizontal: 10,
        paddingVertical: 6,
        borderRadius: 8,
        alignItems: 'center',
        gap: 4,
    },
    collapsedReturnText: {
        fontSize: 12,
        fontWeight: '700',
    },
    emptyState: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
        paddingHorizontal: 24,
    },
    emptyStateContainer: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
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
})