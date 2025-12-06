import React, { useState, useMemo } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Svg, Circle, Path, G } from 'react-native-svg';
import { getThemeColors } from '../../constants/colors';

interface Holding {
    id: string;
    symbol: string;
    company: string;
    currentValue: number;
    sector: string;
}

interface AllocationOverviewProps {
    holdings?: Holding[];
}

const sectorColors = {
    "Technology":      { textColor: "#0369A1", cardColor: "#EFF6FF" },
    "Semiconductors":  { textColor: "#EF6C00", cardColor: "#FFF3E0" },
    "FinTech":         { textColor: "#15803D", cardColor: "#F0FDF4" },
    "Consumer/Tech":   { textColor: "#6D28D9", cardColor: "#F5F3FF" },
    "Healthcare":      { textColor: "#BE123C", cardColor: "#FFE4E6" },
    "Markets":         { textColor: "#7C3AED", cardColor: "#F3E8FF" },
};


const chartColors = [
    "#0369A1",  // Technology
    "#EF6C00",  // Semiconductors
    "#15803D",  // FinTech
    "#6D28D9",  // Consumer/Tech
    "#BE123C",  // Healthcare
    "#7C3AED",  // Markets
];


const screenWidth = Dimensions.get('window').width - 48;

const defaultHoldings: Holding[] = [
    {
        id: '1',
        symbol: 'AAPL',
        company: 'Apple Inc.',
        currentValue: 8250,
        sector: 'Technology',
    },
    {
        id: '2',
        symbol: 'MSFT',
        company: 'Microsoft Corporation',
        currentValue: 11400,
        sector: 'Technology',
    },
    {
        id: '3',
        symbol: 'NVDA',
        company: 'NVIDIA Corporation',
        currentValue: 13380,
        sector: 'Semiconductors',
    },
    {
        id: '4',
        symbol: 'TSLA',
        company: 'Tesla Inc.',
        currentValue: 4905,
        sector: 'Consumer/Tech',
    },
    {
        id: '5',
        symbol: 'GOOGL',
        company: 'Alphabet Inc.',
        currentValue: 3507.50,
        sector: 'Technology',
    },
    {
        id: '6',
        symbol: 'AMD',
        company: 'Advanced Micro Devices',
        currentValue: 2520,
        sector: 'Semiconductors',
    },
];

// Helper function to create pie slice path
const createPiePath = (
    centerX: number,
    centerY: number,
    radius: number,
    innerRadius: number,
    startAngle: number,
    endAngle: number
): string => {
    const start = (angle: number) => {
        return {
            x: centerX + radius * Math.cos((angle - 90) * Math.PI / 180),
            y: centerY + radius * Math.sin((angle - 90) * Math.PI / 180),
        };
    };

    const innerStart = (angle: number) => {
        return {
            x: centerX + innerRadius * Math.cos((angle - 90) * Math.PI / 180),
            y: centerY + innerRadius * Math.sin((angle - 90) * Math.PI / 180),
        };
    };

    const startOuter = start(startAngle);
    const endOuter = start(endAngle);
    const startInner = innerStart(startAngle);
    const endInner = innerStart(endAngle);

    const largeArc = endAngle - startAngle > 180 ? 1 : 0;

    return `
        M ${startOuter.x} ${startOuter.y}
        A ${radius} ${radius} 0 ${largeArc} 1 ${endOuter.x} ${endOuter.y}
        L ${endInner.x} ${endInner.y}
        A ${innerRadius} ${innerRadius} 0 ${largeArc} 0 ${startInner.x} ${startInner.y}
        Z
    `;
};

export const AllocationOverview: React.FC<AllocationOverviewProps> = ({
                                                                          holdings = defaultHoldings,
                                                                      }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const [viewType, setViewType] = useState<'sector' | 'stock'>('sector');
    const [activeIndex, setActiveIndex] = useState<number | undefined>(undefined);

    const allocationBySector = useMemo(() => {
        return holdings.reduce((acc, holding) => {
            const sector = holding.sector;
            const existing = acc.find(item => item.name === sector);
            if (existing) {
                existing.value += holding.currentValue;
                existing.count += 1;
            } else {
                acc.push({ name: sector, value: holding.currentValue, count: 1 });
            }
            return acc;
        }, [] as Array<{ name: string; value: number; count: number }>);
    }, [holdings]);

    const allocationByStock = useMemo(() => {
        return holdings.map(holding => ({
            name: holding.symbol,
            value: holding.currentValue,
            count: 1,
        }));
    }, [holdings]);

    const allocationData = viewType === 'sector' ? allocationBySector : allocationByStock;
    const totalValue = allocationData.reduce((sum, item) => sum + item.value, 0);

    // Generate pie slices
    let currentAngle = 0;
    const slices = allocationData.map((item, index) => {
        const sliceAngle = (item.value / totalValue) * 360;
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

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
            <View style={styles.header}>
                <Text style={[styles.title, { color: Colors.text }]}>
                    Allocation Overview
                </Text>
                <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                    Total Portfolio: A${totalValue.toLocaleString('en-AU', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                </Text>
            </View>

            {/* Toggle Buttons */}
            <View style={styles.toggleContainer}>
                <TouchableOpacity
                    onPress={() => setViewType('sector')}
                    style={[
                        styles.toggleButton,
                        viewType === 'sector' && { backgroundColor: Colors.tint }
                    ]}
                >
                    <MaterialCommunityIcons
                        name="tag-multiple"
                        size={16}
                        color={viewType === 'sector' ? 'white' : Colors.text}
                        style={{ marginRight: 6 }}
                    />
                    <Text
                        style={[
                            styles.toggleButtonText,
                            viewType === 'sector' && { color: 'white', fontWeight: '700' }
                        ]}
                    >
                        By Sector
                    </Text>
                </TouchableOpacity>

                <TouchableOpacity
                    onPress={() => setViewType('stock')}
                    style={[
                        styles.toggleButton,
                        viewType === 'stock' && { backgroundColor: Colors.tint }
                    ]}
                >
                    <MaterialCommunityIcons
                        name="chart-pie"
                        size={16}
                        color={viewType === 'stock' ? 'white' : Colors.text}
                        style={{ marginRight: 6 }}
                    />
                    <Text
                        style={[
                            styles.toggleButtonText,
                            viewType === 'stock' && { color: 'white', fontWeight: '700' }
                        ]}
                    >
                        By Stock
                    </Text>
                </TouchableOpacity>
            </View>

            {/* Pie Chart Card */}
            <View style={[styles.chartCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                <View style={styles.chartWrapper}>
                    <Svg width={screenWidth * 0.9} height={240}>
                        <G>
                            {slices.map((slice, index) => (
                                <Path
                                    key={`slice-${index}`}
                                    d={createPiePath(screenWidth * 0.45, 120, 80, 50, slice.startAngle, slice.endAngle)}
                                    fill={slice.color}
                                    opacity={activeIndex === index ? 1 : 0.7}
                                    strokeWidth={1}
                                    stroke={Colors.card}
                                    onPress={() => setActiveIndex(activeIndex === index ? undefined : index)}
                                />
                            ))}
                        </G>
                    </Svg>
                </View>

                {/* Custom Legend */}
                <View style={styles.legendContainer}>
                    {allocationData.map((item, index) => (
                        <TouchableOpacity
                            key={item.name}
                            onPress={() => setActiveIndex(activeIndex === index ? undefined : index)}
                            style={styles.legendItem}
                        >
                            <View
                                style={[
                                    styles.legendColor,
                                    { backgroundColor: chartColors[index % chartColors.length] }
                                ]}
                            />
                            <Text style={[styles.legendText, { color: Colors.text }]}>
                                {item.name} ({((item.value / totalValue) * 100).toFixed(1)}%)
                            </Text>
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
                        const percentage = (item.value / totalValue) * 100;
                        const color = chartColors[index % chartColors.length];

                        return (
                            <TouchableOpacity
                                key={item.name}
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
                                            {item.name}
                                        </Text>
                                        <Text style={[styles.detailSubtext, { color: Colors.text, opacity: 0.6 }]}>
                                            {item.count} {viewType === 'sector' ? 'position' : 'holding'}{item.count > 1 ? 's' : ''}
                                        </Text>
                                    </View>
                                </View>

                                <View style={styles.detailRight}>
                                    <Text style={[styles.detailValue, { color: Colors.text }]}>
                                        A${item.value.toLocaleString('en-AU', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
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
    toggleContainer: {
        flexDirection: 'row',
        paddingHorizontal: 12,
        marginBottom: 16,
        gap: 10,
    },
    toggleButton: {
        flex: 1,
        flexDirection: 'row',
        paddingHorizontal: 12,
        paddingVertical: 8,
        borderRadius: 8,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#F0F0F0',
    },
    toggleButtonText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
    },
    chartCard: {
        marginBottom: 24,
        borderWidth: 1,
        borderRadius: 12,
        padding: 12,
    },
    chartWrapper: {
        width: '100%',
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 12,
    },
    legendContainer: {
        gap: 8,
    },
    legendItem: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    legendColor: {
        width: 12,
        height: 12,
        borderRadius: 6,
    },
    legendText: {
        fontSize: 12,
        fontWeight: '600',
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
});