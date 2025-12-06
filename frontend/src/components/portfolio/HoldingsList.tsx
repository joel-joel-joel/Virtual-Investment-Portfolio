import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Image,
    Dimensions,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '../../constants/colors';

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
}

const sectorColors = {
    "Technology": { color: "#0369A1", bgLight: "#EFF6FF" },
    "Semiconductors": { color: "#B45309", bgLight: "#FEF3C7" },
    "FinTech": { color: "#15803D", bgLight: "#F0FDF4" },
    "Consumer/Tech": { color: "#6D28D9", bgLight: "#F5F3FF" },
    "Healthcare": { color: "#BE123C", bgLight: "#FFE4E6" },
    "Markets": { color: "#7C3AED", bgLight: "#F3E8FF" },
};

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
    const [isExpanded, setIsExpanded] = React.useState(true);
    const isPositive = holding.returnPercent >= 0;
    const costPerShare = holding.amountInvested / holding.shares;
    const currentPerShare = holding.currentValue / holding.shares;

    return (
        <TouchableOpacity
            onPress={() => setIsExpanded(!isExpanded)}
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
                                                              holdings = defaultHoldings,
                                                              onHoldingPress,
                                                          }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const [sortBy, setSortBy] = useState<'value' | 'return' | 'invested'>('value');

    // Calculate totals
    const totalInvested = holdings.reduce((sum, h) => sum + h.amountInvested, 0);
    const totalValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);
    const totalReturn = totalValue - totalInvested;
    const totalReturnPercent = (totalReturn / totalInvested) * 100;

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

            {/* Sort Options */}
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

            {/* Holdings List */}
            <ScrollView
                showsVerticalScrollIndicator={false}
                style={styles.holdingsList}
                contentContainerStyle={styles.holdingsContent}
            >
                {sortedHoldings.map((holding) => {
                    const sectorColor = sectorColors[holding.sector as keyof typeof sectorColors] || sectorColors['Technology'];
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
        </View>
    );
};

const defaultHoldings: Holding[] = [
    {
        id: '1',
        symbol: 'AAPL',
        company: 'Apple Inc.',
        shares: 50,
        amountInvested: 7500,
        currentValue: 8250,
        returnAmount: 750,
        returnPercent: 10,
        sector: 'Technology',
    },
    {
        id: '2',
        symbol: 'MSFT',
        company: 'Microsoft Corporation',
        shares: 30,
        amountInvested: 9000,
        currentValue: 11400,
        returnAmount: 2400,
        returnPercent: 26.67,
        sector: 'Technology',
    },
    {
        id: '3',
        symbol: 'NVDA',
        company: 'NVIDIA Corporation',
        shares: 15,
        amountInvested: 5000,
        currentValue: 13380,
        returnAmount: 8380,
        returnPercent: 167.6,
        sector: 'Semiconductors',
    },
    {
        id: '4',
        symbol: 'TSLA',
        company: 'Tesla Inc.',
        shares: 20,
        amountInvested: 4000,
        currentValue: 4905,
        returnAmount: 905,
        returnPercent: 22.63,
        sector: 'Consumer/Tech',
    },
    {
        id: '5',
        symbol: 'GOOGL',
        company: 'Alphabet Inc.',
        shares: 25,
        amountInvested: 3125,
        currentValue: 3507.50,
        returnAmount: 382.50,
        returnPercent: 12.24,
        sector: 'Technology',
    },
    {
        id: '6',
        symbol: 'AMD',
        company: 'Advanced Micro Devices',
        shares: 40,
        amountInvested: 2800,
        returnAmount: -280,
        currentValue: 2520,
        returnPercent: -10,
        sector: 'Semiconductors',
    },
];

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
})