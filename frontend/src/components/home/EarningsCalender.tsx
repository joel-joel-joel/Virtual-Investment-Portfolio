import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Image,
    ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
import { getSectorColor } from '@/src/services/sectorColorService';
import { getUpcomingEarnings, getUpcomingEarningsForAccount } from '@/src/services/earningService';
import { useAuth } from '@/src/context/AuthContext';
import type { HoldingDTO } from '@/src/types/api';
import { useTheme } from '@/src/context/ThemeContext';


interface EarningsItem {
    earningsId: string;
    stockId: string;
    stockSymbol: string;
    companyName: string;
    earningsDate: string;
    estimatedEps?: string;
    actualEps?: string;
    reportTime?: string;
    sector: string;
}

interface EarningsCalendarProps {
    holdings: HoldingDTO[];
    activeAccount?: any;
}

const getDayOfWeek = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { weekday: 'short' });
};

const getFormattedDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
};

const formatTime = (timeStr?: string) => {
    if (!timeStr) return 'Time TBA';
    // Assumes format like "16:00" or "Before Market" / "After Market"
    return timeStr;
};

const EarningsCard = ({
                          item,
                          Colors,
                          sectorColor
                      }: {
    item: EarningsItem;
    Colors: any;
    sectorColor: any;
}) => {
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const [expanded, setExpanded] = useState(false);

    const handleViewDetails = () => {
        const stockData = {
            symbol: item.stockSymbol,
            name: item.companyName,
            price: 0, // Will be fetched from holdings or API
            change: 0,
            changePercent: 0,
            sector: item.sector,
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
            nextEarningsDate: item.earningsDate,
            nextDividendDate: '',
            earningsPerShare: item.actualEps || item.estimatedEps || '0',
        };

        navigation.navigate('StockTicker', { stock: stockData });
    };

    return (
        <TouchableOpacity
            onPress={() => setExpanded(!expanded)}
            style={[
                styles.earningsCard,
                {
                    backgroundColor: Colors.card,
                    borderColor: Colors.border,
                    borderWidth: 1,
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
                            {getDayOfWeek(item.earningsDate)}
                        </Text>
                        <Text style={[styles.dateNum, { color: sectorColor.color }]}>
                            {getFormattedDate(item.earningsDate)}
                        </Text>
                    </View>
                </View>

                <View style={styles.companySection}>
                    <View style={{ flex: 1 }}>
                        <Text style={[styles.symbol, { color: sectorColor.color }]}>
                            {item.stockSymbol}
                        </Text>
                        <Text style={[styles.company, { color: Colors.text, opacity: 0.7 }]} numberOfLines={1}>
                            {item.companyName}
                        </Text>
                    </View>
                </View>

                <View style={styles.timeSection}>
                    <MaterialCommunityIcons
                        name="clock-outline"
                        size={16}
                        color={Colors.tint}
                        style={{ marginRight: 4 }}
                    />
                    <Text style={[styles.time, { color: Colors.text }]}>
                        {formatTime(item.reportTime)}
                    </Text>
                </View>
            </View>

            {/* Expanded Content */}
            {expanded && (
                <View style={[styles.expandedContent, { borderTopColor: Colors.border }]}>
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

                    {(item.estimatedEps || item.actualEps) && (
                        <View style={styles.epsRow}>
                            {item.estimatedEps && (
                                <View style={styles.epsItem}>
                                    <Text style={[styles.epsLabel, { color: Colors.text, opacity: 0.6 }]}>
                                        Expected EPS
                                    </Text>
                                    <Text style={[styles.epsValue, { color: Colors.tint }]}>
                                        {item.estimatedEps}
                                    </Text>
                                </View>
                            )}
                            {item.actualEps && (
                                <>
                                    {item.estimatedEps && <View style={[styles.separator, { backgroundColor: Colors.border }]} />}
                                    <View style={styles.epsItem}>
                                        <Text style={[styles.epsLabel, { color: Colors.text, opacity: 0.6 }]}>
                                            Previous EPS
                                        </Text>
                                        <Text style={[styles.epsValue, { color: Colors.text }]}>
                                            {item.actualEps}
                                        </Text>
                                    </View>
                                </>
                            )}
                        </View>
                    )}

                    <TouchableOpacity
                        onPress={handleViewDetails}
                        style={[styles.viewButton, { backgroundColor: Colors.tint }]}
                    >
                        <Text style={styles.viewButtonText}>View Stock Details →</Text>
                    </TouchableOpacity>
                </View>
            )}
        </TouchableOpacity>
    );
};

export const EarningsCalendar: React.FC<EarningsCalendarProps> = ({
                                                                      holdings,
                                                                      activeAccount,
                                                                  }) => {
    const {Colors} = useTheme();
    const [earnings, setEarnings] = useState<EarningsItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [selectedSector, setSelectedSector] = useState<string | null>(null);

    // Fetch upcoming earnings for holdings
    useEffect(() => {
        const fetchEarningsData = async () => {
            console.log('📅 [EarningsCalendar] useEffect triggered');
            console.log('📊 [EarningsCalendar] Holdings count:', holdings?.length || 0);

            if (!holdings || holdings.length === 0) {
                console.warn('⚠️ [EarningsCalendar] No holdings provided, skipping fetch');
                console.log('📊 [EarningsCalendar] Holdings value:', holdings);
                setLoading(false);
                return;
            }

            console.log('📊 [EarningsCalendar] Holdings symbols:', holdings.filter(h => h != null).map(h => h.stockSymbol).join(', '));

            try {
                setLoading(true);

                // Fetch upcoming earnings for the active account
                console.log('🔄 [EarningsCalendar] Calling getUpcomingEarningsForAccount...');
                if (!activeAccount?.accountId) {
                    console.warn('⚠️ [EarningsCalendar] No active account provided, skipping earnings fetch');
                    setEarnings([]);
                    setLoading(false);
                    return;
                }
                const upcomingEarnings = await getUpcomingEarningsForAccount(activeAccount.accountId);
                console.log('✅ [EarningsCalendar] getUpcomingEarningsForAccount returned:', upcomingEarnings?.length || 0, 'items');

                if (!upcomingEarnings || upcomingEarnings.length === 0) {
                    console.warn('⚠️ [EarningsCalendar] No upcoming earnings data received');
                    setEarnings([]);
                    return;
                }

                // Filter earnings for holdings only and enrich with sector data
                const holdingSymbols = new Set(holdings.filter(h => h != null).map(h => h.stockSymbol));
                console.log('🔍 [EarningsCalendar] Holding symbols set:', Array.from(holdingSymbols).join(', '));

                const enrichedEarnings = upcomingEarnings
                    .filter(e => {
                        const matches = holdingSymbols.has(e.stockCode);
                        if (!matches) {
                            console.log(`   - Filtered out ${e.stockCode} (not in holdings)`);
                        }
                        return matches;
                    })
                    .map(e => {
                        // Find holding to get sector
                        const holding = holdings.find(h => h.stockSymbol === e.stockCode);
                        console.log(`   - Enriching ${e.stockCode}: sector=${holding?.sector || 'Unknown'}`);
                        return {
                            earningsId: e.earningsId,
                            stockId: e.stockId,
                            stockSymbol: e.stockCode,
                            companyName: e.companyName,
                            earningsDate: e.earningsDate,
                            estimatedEps: e.estimatedEPS ? `$${parseFloat(String(e.estimatedEPS)).toFixed(2)}` : undefined,
                            actualEps: e.actualEPS ? `$${parseFloat(String(e.actualEPS)).toFixed(2)}` : undefined,
                            reportTime: e.reportTime || 'Time TBA',
                            sector: holding?.sector || 'Unknown',
                        };
                    })
                    .sort((a, b) => new Date(a.earningsDate).getTime() - new Date(b.earningsDate).getTime());

                console.log('✅ [EarningsCalendar] Enriched earnings count:', enrichedEarnings.length);
                if (enrichedEarnings.length > 0) {
                    console.log('📋 [EarningsCalendar] First enriched earning:', enrichedEarnings[0]);
                    console.log('📋 [EarningsCalendar] Last enriched earning:', enrichedEarnings[enrichedEarnings.length - 1]);
                }

                setEarnings(enrichedEarnings);
            } catch (error) {
                console.error('❌ [EarningsCalendar] Failed to fetch earnings:', error);
                console.error('   Error details:', error);
                setEarnings([]);
            } finally {
                setLoading(false);
            }
        };

        fetchEarningsData();
    }, [holdings, activeAccount?.accountId]);

    // Filter earnings by sector if selected
    const filteredEarnings = selectedSector
        ? earnings.filter(e => e.sector === selectedSector)
        : earnings;
    console.log('🔽 [EarningsCalendar] Filtered earnings count:', filteredEarnings.length, 'selectedSector:', selectedSector);

    // Group earnings by date
    const earningsByDate = filteredEarnings.reduce((acc, item) => {
        if (!acc[item.earningsDate]) acc[item.earningsDate] = [];
        acc[item.earningsDate].push(item);
        return acc;
    }, {} as Record<string, EarningsItem[]>);

    const dateGroupCount = Object.keys(earningsByDate).length;
    console.log('📅 [EarningsCalendar] Date groups:', dateGroupCount);
    if (dateGroupCount > 0) {
        console.log('   Date groups:', Object.keys(earningsByDate).join(', '));
    }

    const uniqueSectors = Array.from(new Set(earnings.map(e => e.sector))).sort();
    console.log('🏷️ [EarningsCalendar] Unique sectors:', uniqueSectors.join(', '));

    // Loading state
    if (loading) {
        console.log('⏳ [EarningsCalendar] Rendering LOADING state');
        return (
            <View style={styles.wrapper}>
                <View style={[styles.header, { borderBottomColor: Colors.border }]}>
                    <View>
                        <Text style={[styles.title, { color: Colors.text }]}>
                            Earnings Calendar
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Next 90 days
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="calendar-range"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>
                <View style={{ padding: 24, justifyContent: 'center', alignItems: 'center' }}>
                    <ActivityIndicator size="large" color={Colors.tint} />
                    <Text style={[styles.loadingText, { color: Colors.text, marginTop: 12 }]}>
                        Loading earnings data...
                    </Text>
                </View>
            </View>
        );
    }

    // Main render
    console.log('🎨 [EarningsCalendar] Rendering main view with earnings');
    console.log('   Total earnings:', earnings.length);
    console.log('   Earnings to display:', filteredEarnings.length);

    return (
        <View style={styles.wrapper}>
            {/* Header */}
            <View style={[styles.header, { borderBottomColor: Colors.border }]}>
                <View>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Earnings Calendar
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Next 90 days
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
            {earnings.length > 0 && (
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
                        const sectorColor = getSectorColor(sector);
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
            )}

            {/* Earnings List */}
            <ScrollView style={styles.earningsContainer} showsVerticalScrollIndicator={false}>
                {Object.entries(earningsByDate).length > 0 ? (
                    <>
                        {console.log('🎯 [EarningsCalendar] Rendering', Object.entries(earningsByDate).length, 'date groups')}
                        {Object.entries(earningsByDate).map(([date, items]) => {
                            console.log(`   - Rendering date group: ${date} with ${items.length} items`);
                            const sectorColor = getSectorColor(items[0].sector);

                            return (
                                <View key={date} style={styles.dateGroup}>
                                    <Text key={`header-${date}`} style={[styles.dateGroupHeader, { color: sectorColor.color }]}>
                                        {getDayOfWeek(date).toUpperCase()} • {getFormattedDate(date)}
                                    </Text>
                                    {items.map((item) => (
                                        <EarningsCard
                                            key={item.earningsId}
                                            item={item}
                                            Colors={Colors}
                                            sectorColor={getSectorColor(item.sector)}
                                        />
                                    ))}
                                </View>
                            );
                        })}
                    </>
                ) : (
                    <>
                        {console.log('📭 [EarningsCalendar] Rendering EMPTY STATE - no earnings to display')}
                        <View style={styles.emptyState}>
                            <MaterialCommunityIcons
                                name="calendar-blank"
                                size={48}
                                color={Colors.text}
                                style={{ opacity: 0.3, marginBottom: 12 }}
                            />
                            <Text style={[styles.emptyStateText, { color: Colors.text, opacity: 0.6 }]}>
                                No upcoming earnings
                            </Text>
                        </View>
                    </>
                )}
            </ScrollView>
        </View>
    );
};

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
    loadingText: {
        fontSize: 14,
        fontWeight: '600',
    },
});