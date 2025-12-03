import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Dimensions,
    Image,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '../../constants/colors';

interface EarningsItem {
    id: number;
    symbol: string;
    company: string;
    date: string;
    time: string;
    eps?: string;
    expectedEps?: string;
    sector: string;
    image?: any;
}

interface EarningsCalendarProps {
    earnings?: EarningsItem[];
}

const sectorColors = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Retail': { color: '#EA580C', bgLight: '#FEF3C7' },
};

const getDayOfWeek = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { weekday: 'short' });
};

const getFormattedDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const EarningsCard = ({ item, colors, sectorColor }: { item: EarningsItem; colors: any; sectorColor: any }) => {
    const [expanded, setExpanded] = useState(false);

    return (
        <TouchableOpacity
            onPress={() => setExpanded(!expanded)}
            style={[
                styles.earningsCard,
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
                <View style={styles.dateSection}>
                    <View style={[styles.dateBadge, { backgroundColor: sectorColor.bgLight }]}>
                        <Text style={[styles.dateDay, { color: sectorColor.color }]}>
                            {getDayOfWeek(item.date)}
                        </Text>
                        <Text style={[styles.dateNum, { color: sectorColor.color }]}>
                            {getFormattedDate(item.date)}
                        </Text>
                    </View>
                </View>

                <View style={styles.companySection}>
                    {item.image && (
                        <Image
                            source={item.image}
                            style={styles.companyImage}
                            resizeMode="contain"
                        />
                    )}
                    <View style={{ flex: 1 }}>
                        <Text style={[styles.symbol, { color: sectorColor.color }]}>
                            {item.symbol}
                        </Text>
                        <Text style={[styles.company, { color: colors.text, opacity: 0.7 }]} numberOfLines={1}>
                            {item.company}
                        </Text>
                    </View>
                </View>

                <View style={styles.timeSection}>
                    <MaterialCommunityIcons
                        name="clock-outline"
                        size={16}
                        color={colors.tint}
                        style={{ marginRight: 4 }}
                    />
                    <Text style={[styles.time, { color: colors.text }]}>
                        {item.time}
                    </Text>
                </View>
            </View>

            {/* Expanded Content */}
            {expanded && (
                <View style={[styles.expandedContent, { borderTopColor: colors.border }]}>
                    <View style={styles.sectorBadge}>
                        <View style={[styles.sectorBadgeContent, { backgroundColor: sectorColor.bgLight }]}>
                            <MaterialCommunityIcons
                                name="tag"
                                size={12}
                                color={sectorColor.color}
                            />
                            <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                                {item.sector}
                            </Text>
                        </View>
                    </View>

                    {item.expectedEps && (
                        <View style={styles.epsRow}>
                            <View style={styles.epsItem}>
                                <Text style={[styles.epsLabel, { color: colors.text, opacity: 0.6 }]}>
                                    Expected EPS
                                </Text>
                                <Text style={[styles.epsValue, { color: colors.tint }]}>
                                    {item.expectedEps}
                                </Text>
                            </View>
                            {item.eps && (
                                <>
                                    <View style={[styles.separator, { backgroundColor: colors.border }]} />
                                    <View style={styles.epsItem}>
                                        <Text style={[styles.epsLabel, { color: colors.text, opacity: 0.6 }]}>
                                            Previous EPS
                                        </Text>
                                        <Text style={[styles.epsValue, { color: colors.text }]}>
                                            {item.eps}
                                        </Text>
                                    </View>
                                </>
                            )}
                        </View>
                    )}

                    <TouchableOpacity style={[styles.viewButton, { backgroundColor: colors.tint }]}>
                        <Text style={styles.viewButtonText}>View Earnings Details →</Text>
                    </TouchableOpacity>
                </View>
            )}
        </TouchableOpacity>
    );
};

export const EarningsCalendar: React.FC<EarningsCalendarProps> = ({
                                                                      earnings = defaultEarnings
                                                                  }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const [selectedSector, setSelectedSector] = useState<string | null>(null);

    // Filter earnings by sector if selected
    const filteredEarnings = selectedSector
        ? earnings.filter(e => e.sector === selectedSector)
        : earnings;

    // Group earnings by date
    const earningsByDate = filteredEarnings.reduce((acc, item) => {
        if (!acc[item.date]) acc[item.date] = [];
        acc[item.date].push(item);
        return acc;
    }, {} as Record<string, EarningsItem[]>);

    const uniqueSectors = Array.from(new Set(earnings.map(e => e.sector)));

    return (
        <View style={styles.wrapper}>
            {/* Header */}
            <View style={[styles.header, { borderBottomColor: Colors.border }]}>
                <View>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Earnings Calendar
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Next 7 days
                    </Text>
                </View>
                <MaterialCommunityIcons
                    name="calendar-range"
                    size={28}
                    color={Colors.tint}
                    style={{ opacity: 0.7 }}
                />
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
                        styles.filterButton,
                        selectedSector === null && [
                            styles.filterButtonActive,
                            { backgroundColor: Colors.tint }
                        ],
                        selectedSector === null && { backgroundColor: Colors.tint },
                    ]}
                >
                    <Text
                        style={[
                            styles.filterButtonText,
                            selectedSector === null && { color: 'white', fontWeight: '700' }
                        ]}
                    >
                        All
                    </Text>
                </TouchableOpacity>

                {uniqueSectors.map((sector) => {
                    const sectorColor = sectorColors[sector as keyof typeof sectorColors] || sectorColors['Technology'];
                    const isSelected = selectedSector === sector;

                    return (
                        <TouchableOpacity
                            key={sector}
                            onPress={() => setSelectedSector(sector)}
                            style={[
                                styles.filterButton,
                                isSelected && [
                                    styles.filterButtonActive,
                                    { backgroundColor: sectorColor.bgLight }
                                ],
                                !isSelected && { backgroundColor: Colors.card }
                            ]}
                        >
                            <Text
                                style={[
                                    styles.filterButtonText,
                                    isSelected && { color: sectorColor.color, fontWeight: '700' }
                                ]}
                            >
                                {sector}
                            </Text>
                        </TouchableOpacity>
                    );
                })}
            </ScrollView>

            {/* Earnings List */}
            <ScrollView style={styles.earningsContainer} showsVerticalScrollIndicator={false}>
                {Object.entries(earningsByDate).map(([date, items]) => {
                    const sectorColor = sectorColors[items[0].sector as keyof typeof sectorColors] || sectorColors['Technology'];

                    return (
                        <View key={date} style={styles.dateGroup}>
                            <Text style={[styles.dateGroupHeader, { color: sectorColor.color }]}>
                                {getDayOfWeek(date).toUpperCase()} • {getFormattedDate(date)}
                            </Text>
                            {items.map((item) => (
                                <EarningsCard
                                    key={item.id}
                                    item={item}
                                    colors={Colors}
                                    sectorColor={sectorColors[item.sector as keyof typeof sectorColors] || sectorColors['Technology']}
                                />
                            ))}
                        </View>
                    );
                })}

                {filteredEarnings.length === 0 && (
                    <View style={styles.emptyState}>
                        <MaterialCommunityIcons
                            name="calendar-blank"
                            size={48}
                            color={Colors.text}
                            style={{ opacity: 0.3, marginBottom: 12 }}
                        />
                        <Text style={[styles.emptyStateText, { color: Colors.text, opacity: 0.6 }]}>
                            No earnings this period
                        </Text>
                    </View>
                )}
            </ScrollView>
        </View>
    );
};

const defaultEarnings: EarningsItem[] = [
    {
        id: 1,
        symbol: 'NVDA',
        company: 'NVIDIA Corporation',
        date: '2024-12-03',
        time: 'After Market',
        expectedEps: '$0.68',
        eps: '$0.62',
        sector: 'Semiconductors',
    },
    {
        id: 2,
        symbol: 'MSFT',
        company: 'Microsoft Corporation',
        date: '2024-12-03',
        time: '4:00 PM',
        expectedEps: '$3.15',
        eps: '$3.08',
        sector: 'Technology',
    },
    {
        id: 3,
        symbol: 'UBER',
        company: 'Uber Technologies',
        date: '2024-12-04',
        time: 'After Market',
        expectedEps: '$1.12',
        eps: '$0.98',
        sector: 'Consumer/Tech',
    },
    {
        id: 4,
        symbol: 'AMD',
        company: 'Advanced Micro Devices',
        date: '2024-12-04',
        time: '5:00 PM',
        expectedEps: '$0.92',
        eps: '$0.85',
        sector: 'Semiconductors',
    },
    {
        id: 5,
        symbol: 'COIN',
        company: 'Coinbase Global',
        date: '2024-12-05',
        time: 'After Market',
        expectedEps: '$1.45',
        eps: '$1.28',
        sector: 'FinTech',
    },
    {
        id: 6,
        symbol: 'META',
        company: 'Meta Platforms',
        date: '2024-12-06',
        time: '4:30 PM',
        expectedEps: '$4.32',
        eps: '$4.10',
        sector: 'Technology',
    },
    {
        id: 7,
        symbol: 'TSLA',
        company: 'Tesla Inc.',
        date: '2024-12-07',
        time: 'Before Market',
        expectedEps: '$0.75',
        eps: '$0.68',
        sector: 'Consumer/Tech',
    },
];

const styles = StyleSheet.create({
    wrapper: {
        marginTop: 24,
        marginBottom: 5,
    },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "flex-start",
        paddingHorizontal: 24,
        paddingBottom: 12,
        borderBottomWidth: 0.4,
        marginBottom: 16,
    },
    title: {
        fontSize: 18,
        fontWeight: '800',
        fontStyle: 'italic',
    },
    subtitle: {
        fontSize: 12,
        marginTop: 4,
    },
    sectorFilter: {
        paddingHorizontal: 24,
        marginBottom: 16,
    },
    sectorFilterContent: {
        gap: 8,
        paddingRight: 12,
    },
    filterButton: {
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8,
        borderWidth: 1,
        borderColor: 'transparent',
    },
    filterButtonActive: {
        borderWidth: 0,
    },
    filterButtonText: {
        fontSize: 12,
        fontWeight: '600',
        color: '#666',
    },
    earningsContainer: {
        paddingHorizontal: 24,
        maxHeight: 500,
    },
    dateGroup: {
        marginBottom: 16,
    },
    dateGroupHeader: {
        fontSize: 11,
        fontWeight: '700',
        textTransform: 'uppercase',
        letterSpacing: 0.5,
        marginBottom: 8,
        opacity: 0.8,
    },
    earningsCard: {
        borderRadius: 12,
        padding: 12,
        marginBottom: 8,
    },
    cardHeader: {
        flexDirection: 'row',
        gap: 12,
        alignItems: 'center',
    },
    dateSection: {
        width: 60,
    },
    dateBadge: {
        paddingHorizontal: 8,
        paddingVertical: 6,
        borderRadius: 8,
        alignItems: 'center',
    },
    dateDay: {
        fontSize: 10,
        fontWeight: '700',
    },
    dateNum: {
        fontSize: 12,
        fontWeight: '800',
    },
    companySection: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    companyImage: {
        width: 32,
        height: 32,
        borderRadius: 8,
    },
    symbol: {
        fontSize: 13,
        fontWeight: '700',
        marginBottom: 2,
    },
    company: {
        fontSize: 11,
        fontWeight: '500',
    },
    timeSection: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    time: {
        fontSize: 11,
        fontWeight: '600',
    },
    expandedContent: {
        marginTop: 12,
        paddingTop: 12,
        borderTopWidth: 1,
        gap: 12,
    },
    sectorBadge: {
        marginBottom: 4,
    },
    sectorBadgeContent: {
        flexDirection: 'row',
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
        alignItems: 'center',
        gap: 4,
        alignSelf: 'flex-start',
    },
    sectorBadgeText: {
        fontSize: 10,
        fontWeight: '600',
    },
    epsRow: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        alignItems: 'center',
    },
    epsItem: {
        alignItems: 'center',
        gap: 4,
    },
    epsLabel: {
        fontSize: 10,
        fontWeight: '600',
    },
    epsValue: {
        fontSize: 13,
        fontWeight: '700',
    },
    separator: {
        width: 1,
        height: 30,
    },
    viewButton: {
        paddingVertical: 10,
        borderRadius: 10,
        alignItems: 'center',
        marginTop: 4,
    },
    viewButtonText: {
        color: 'white',
        fontSize: 12,
        fontWeight: '700',
    },
    emptyState: {
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 40,
    },
    emptyStateText: {
        fontSize: 14,
        fontWeight: '500',
    },
});