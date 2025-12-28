import React, { useState, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    StyleSheet,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Svg, Path, G } from 'react-native-svg';
import { useTheme } from '@/src/context/ThemeContext';
import { useFocusEffect } from '@react-navigation/native';
import { apiFetch } from '../../../services/api';
import type { PortfolioOverviewDTO, HoldingDTO } from '../../../types/api';
import { getSectorColorPalette } from '@/src/services/sectorColorService';

interface AllocationItem {
    stockCode: string;
    percentage: number;
    currentValue: number;
    shares: number; // ✅ ADD THIS
}

interface SliceData extends AllocationItem {
    startAngle: number;
    endAngle: number;
    color: string;
}


interface AllocationOverviewProps {
    accountId: string;
}

export const AllocationOverview: React.FC<AllocationOverviewProps> = ({ accountId }) => {
    const {Colors} = useTheme();

    const [allocationData, setAllocationData] = useState<AllocationItem[]>([]);
    const [chartColors, setChartColors] = useState<string[]>([]);
    const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Extract fetch logic into a memoized callback (used by both useEffect and useFocusEffect)
    const fetchData = useCallback(async () => {
        if (!accountId) {
            console.log('⚠️ AllocationOverview: No accountId provided');
            setIsLoading(false);
            setError('No account selected');
            return;
        }

        try {
            setIsLoading(true);
            setError(null);

            console.log('🔄 AllocationOverview: Fetching portfolio overview for account:', accountId);

            const overview = await apiFetch<PortfolioOverviewDTO>(
                `/api/portfolio/overview/account/${accountId}`,
                {
                    method: 'GET',
                    requireAuth: true,
                }
            );

            console.log('✅ AllocationOverview: Got portfolio overview:', overview);

            if (!overview || !overview.holdings || overview.holdings.length === 0) {
                console.log('ℹ️ AllocationOverview: No holdings in portfolio');
                setAllocationData([]);
                setIsLoading(false);
                return;
            }

            // Calculate allocation from holdings
            const allocations = calculateAllocation(overview.holdings);
            console.log('📊 AllocationOverview: Calculated allocations:', allocations);

            setAllocationData(allocations);
        } catch (err: any) {
            console.error('❌ AllocationOverview: Error fetching portfolio overview');
            console.error('  Error:', err.message);

            const errorMessage = err.response?.data?.message ||
                err.message ||
                'Failed to load allocation data';
            setError(errorMessage);
            setAllocationData([]);
        } finally {
            setIsLoading(false);
        }
    }, [accountId]);

    // Fetch on mount and when account changes
    useEffect(() => {
        fetchData();
    }, [fetchData]);

    // Reload when screen comes into focus (after purchases, navigation, etc.)
    useFocusEffect(
        useCallback(() => {
            fetchData();
        }, [fetchData])
    );

// Calculate allocation percentages from holdings
    const calculateAllocation = (holdings: HoldingDTO[]): AllocationItem[] => {
        if (holdings.length === 0) {
            setChartColors([]);
            return [];
        }

        const totalValue = holdings.reduce((sum, h) => sum + h.currentValue, 0);

        if (totalValue === 0) {
            setChartColors([]);
            return [];
        }

        console.log('📊 DEBUG: Calculating allocation from holdings');
        console.log('Total holdings count:', holdings.length);

        const allocations = holdings.map((holding, index) => {
            console.log(`📌 Holding ${index + 1}:`);
            console.log('  Symbol:', holding.stockSymbol);
            console.log('  Quantity:', holding.quantity);
            console.log('  Current Value:', holding.currentValue);
            console.log('  Sector:', holding.sector);

            // ✅ IMPORTANT: Make sure quantity is actually a number
            const sharesCount = typeof holding.quantity === 'string'
                ? parseFloat(holding.quantity)
                : holding.quantity;

            console.log('  Parsed Quantity:', sharesCount);
            console.log('  Type:', typeof sharesCount);

            return {
                stockCode: holding.stockSymbol,
                currentValue: holding.currentValue,
                shares: sharesCount, // ✅ Make sure this is a number
                percentage: (holding.currentValue / totalValue) * 100,
            };
        });

        console.log('📋 Final allocations:', JSON.stringify(allocations, null, 2));

        // Generate colors for each stock based on stock code
        const colors = getSectorColorPalette(holdings.map(holding => holding.sector));
        setChartColors(colors);

        return allocations;
    };
    // Calculate total value from allocation data
    const totalValue = allocationData.reduce((sum, item) => sum + item.currentValue, 0);

    // Generate slices for pie chart
    let currentAngle = 0;
    const slices: SliceData[] = allocationData.map((item, index) => {
        const sliceAngle = totalValue > 0 ? (item.currentValue / totalValue) * 360 : 0;
        const startAngle = currentAngle;
        const endAngle = currentAngle + sliceAngle;
        currentAngle = endAngle;

        return {
            ...item,
            startAngle,
            endAngle,
            color: chartColors[index % chartColors.length],
        };
    });

    // Loading state
    if (isLoading) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background }]}>
                <View style={styles.header}>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Allocation Overview
                    </Text>
                </View>
                <View style={[styles.emptyState, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <MaterialCommunityIcons
                        name="loading"
                        size={56}
                        color={Colors.text}
                        style={{ opacity: 0.3, marginBottom: 16 }}
                    />
                    <Text style={[styles.emptyTitle, { color: Colors.text }]}>
                        Loading allocation...
                    </Text>
                </View>
            </View>
        );
    }

    // Error state
    if (error) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background }]}>
                <View style={styles.header}>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Allocation Overview
                    </Text>
                </View>
                <View style={[styles.emptyState, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <MaterialCommunityIcons
                        name="alert-circle-outline"
                        size={56}
                        color={Colors.text}
                        style={{ opacity: 0.3, marginBottom: 16 }}
                    />
                    <Text style={[styles.emptyTitle, { color: Colors.text }]}>
                        {error}
                    </Text>
                    <Text style={[styles.emptySubtitle, { color: Colors.text, opacity: 0.6 }]}>
                        {error === 'No account selected' ? 'Please select an account' : 'Please try again later'}
                    </Text>
                </View>
            </View>
        );
    }

    // Empty state - no holdings
    if (!allocationData || allocationData.length === 0) {
        return (
            <View style={[styles.container, { backgroundColor: Colors.background }]}>
                <View style={styles.header}>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Allocation Overview
                    </Text>
                </View>
                <View style={[styles.emptyState, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <MaterialCommunityIcons
                        name="chart-donut"
                        size={56}
                        color={Colors.text}
                        style={{ opacity: 0.3, marginBottom: 16 }}
                    />
                    <Text style={[styles.emptyTitle, { color: Colors.text }]}>
                        No allocation data
                    </Text>
                    <Text style={[styles.emptySubtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Start investing to see your portfolio allocation
                    </Text>
                </View>
            </View>
        );
    }

    // Main view with data
    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: Colors.text }]}>
                    Allocation Overview
                </Text>
                <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                    Total Holdings: A${totalValue.toLocaleString('en-AU', {
                    minimumFractionDigits: 0,
                    maximumFractionDigits: 0
                })}
                </Text>
            </View>

            {/* Pie Chart Card */}
            <View style={[styles.chartCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                <View style={styles.chartWrapper}>
                    <View style={{ width: '100%', height: 280, alignItems: 'center', justifyContent: 'center' }}>
                        <Svg width={250} height={250} viewBox="0 0 250 250">
                            <G>
                                {slices.map((slice, index) => {
                                    const radius = 80;
                                    const innerRadius = 50;
                                    const centerX = 125;
                                    const centerY = 125;

                                    const start = (angle: number) => ({
                                        x: centerX + radius * Math.cos((angle - 90) * Math.PI / 180),
                                        y: centerY + radius * Math.sin((angle - 90) * Math.PI / 180),
                                    });

                                    const innerStart = (angle: number) => ({
                                        x: centerX + innerRadius * Math.cos((angle - 90) * Math.PI / 180),
                                        y: centerY + innerRadius * Math.sin((angle - 90) * Math.PI / 180),
                                    });

                                    const startOuter = start(slice.startAngle);
                                    const endOuter = start(slice.endAngle);
                                    const startInner = innerStart(slice.startAngle);
                                    const endInner = innerStart(slice.endAngle);

                                    const largeArc = slice.endAngle - slice.startAngle > 180 ? 1 : 0;

                                    const pathData = `
                                        M ${startOuter.x} ${startOuter.y}
                                        A ${radius} ${radius} 0 ${largeArc} 1 ${endOuter.x} ${endOuter.y}
                                        L ${endInner.x} ${endInner.y}
                                        A ${innerRadius} ${innerRadius} 0 ${largeArc} 0 ${startInner.x} ${startInner.y}
                                        Z
                                    `;

                                    return (
                                        <Path
                                            key={`slice-${index}`}
                                            d={pathData}
                                            fill={slice.color}
                                            opacity={activeIndex === index ? 1 : 0.8}
                                            strokeWidth={2}
                                            stroke={Colors.card}
                                        />
                                    );
                                })}
                            </G>
                        </Svg>
                    </View>
                </View>

                {/* Legend */}
                <View style={styles.legendContainer}>
                    {allocationData.map((item, index) => (
                        <TouchableOpacity
                            key={item.stockCode}
                            onPress={() => setActiveIndex(activeIndex === index ? undefined : index)}
                            style={[
                                styles.legendItem,
                                activeIndex === index && { backgroundColor: Colors.card }
                            ]}
                        >
                            <View
                                style={[
                                    styles.legendColor,
                                    { backgroundColor: chartColors[index % chartColors.length] }
                                ]}
                            />
                            <View style={{ flex: 1 }}>
                                <Text style={[styles.legendText, { color: Colors.text }]}>
                                    {item.stockCode}
                                </Text>
                                <Text style={[styles.legendSubtext, { color: Colors.text, opacity: 0.6 }]}>
                                    {item.percentage.toFixed(1)}% • A${item.currentValue.toLocaleString('en-AU')}
                                </Text>
                            </View>
                        </TouchableOpacity>
                    ))}
                </View>
            </View>

            {/* Breakdown List */}
            <View style={styles.detailsContainer}>
                <Text style={[styles.detailsTitle, { color: Colors.text }]}>
                    Breakdown
                </Text>

                <ScrollView
                    style={styles.detailsList}
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={styles.detailsContent}
                >
                    {allocationData.map((item, index) => {
                        const percentage = item.percentage;
                        const color = chartColors[index % chartColors.length];

                        return (
                            <TouchableOpacity
                                key={item.stockCode}
                                onPress={() => setActiveIndex(activeIndex === index ? undefined : index)}
                                style={[
                                    styles.detailItem,
                                    {
                                        backgroundColor: Colors.card,
                                        borderColor: activeIndex === index ? Colors.tint : Colors.border,
                                        borderWidth: activeIndex === index ? 2 : 1,
                                    }
                                ]}
                            >
                                <View style={styles.detailLeft}>
                                    <View
                                        style={[
                                            styles.colorDot,
                                            { backgroundColor: color }
                                        ]}
                                    />
                                    <View style={styles.detailText}>
                                        <Text style={[styles.detailName, { color: Colors.text }]}>
                                            {item.stockCode}
                                        </Text>
                                        <Text style={[styles.detailSubtext, { color: Colors.text, opacity: 0.6 }]}>
                                            {item.shares.toFixed(2)} shares {/* ✅ CHANGED FROM "1 holding" */}
                                        </Text>
                                    </View>
                                </View>

                                <View style={styles.detailRight}>
                                    <Text style={[styles.detailValue, { color: Colors.text }]}>
                                        A${item.currentValue.toLocaleString('en-AU', {
                                        minimumFractionDigits: 0,
                                        maximumFractionDigits: 0
                                    })}
                                    </Text>
                                    <View style={styles.percentageBar}>
                                        <View
                                            style={[
                                                styles.percentageFill,
                                                {
                                                    backgroundColor: color,
                                                    width: `${percentage}%`,
                                                }
                                            ]}
                                        />
                                    </View>
                                    <Text style={[styles.detailPercent, { color: Colors.text, opacity: 0.7 }]}>
                                        {percentage.toFixed(1)}%
                                    </Text>
                                </View>
                            </TouchableOpacity>
                        );
                    })}
                </ScrollView>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
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
    chartCard: {
        marginBottom: 24,
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
    },
    chartWrapper: {
        width: '100%',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 20,
    },
    legendContainer: {
        gap: 12,
    },
    legendItem: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        padding: 10,
        borderRadius: 8,
    },
    legendColor: {
        width: 16,
        height: 16,
        borderRadius: 8,
    },
    legendText: {
        fontSize: 13,
        fontWeight: '700',
    },
    legendSubtext: {
        fontSize: 11,
        fontWeight: '500',
        marginTop: 2,
    },
    detailsContainer: {
        flex: 1,
    },
    detailsTitle: {
        fontSize: 16,
        fontWeight: '700',
        marginBottom: 12,
        marginLeft: 12,
    },
    detailsList: {
        flex: 1,
    },
    detailsContent: {
        gap: 10,
        paddingBottom: 24,
    },
    detailItem: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginHorizontal: 12,
    },
    detailLeft: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        flex: 1,
    },
    colorDot: {
        width: 12,
        height: 12,
        borderRadius: 6,
    },
    detailText: {
        flex: 1,
    },
    detailName: {
        fontSize: 13,
        fontWeight: '700',
        marginBottom: 2,
    },
    detailSubtext: {
        fontSize: 11,
        fontWeight: '500',
    },
    detailRight: {
        alignItems: 'flex-end',
        gap: 6,
    },
    detailValue: {
        fontSize: 13,
        fontWeight: '700',
    },
    percentageBar: {
        width: 60,
        height: 6,
        borderRadius: 3,
        backgroundColor: '#E0E0E0',
        overflow: 'hidden',
    },
    percentageFill: {
        height: '100%',
        borderRadius: 3,
    },
    detailPercent: {
        fontSize: 11,
        fontWeight: '600',
    },
    emptyState: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 60,
        alignItems: 'center',
        justifyContent: 'center',
        marginHorizontal: 12,
    },
    emptyTitle: {
        fontSize: 18,
        fontWeight: '700',
        marginBottom: 8,
    },
    emptySubtitle: {
        fontSize: 13,
        textAlign: 'center',
        paddingHorizontal: 24,
    },
});