import React, { useState, useEffect } from "react";
import {
    View,
    Text,
    StyleSheet,
    Dimensions,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    ActivityIndicator,
    RefreshControl,
} from "react-native";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { getThemeColors } from "../../constants/colors";
import { getSectorColor } from "@/src/services/sectorColorService";
import { generateStockSuggestions } from "@/src/services/stockSuggestionService";
import { useAuth } from "@/src/context/AuthContext";
import { useRouter } from "expo-router";
import type { SuggestionStock } from "@/src/types/api";

const { width } = Dimensions.get("window");

// ============================================================================
// SUGGESTION CARD COMPONENT
// ============================================================================

// @ts-ignore
const SuggestionCard = ({ stock, reason, icon, sectorColor }) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const router = useRouter();

    const handleViewDetails = () => {
        const stockData = {
            symbol: stock.symbol,
            name: stock.symbol, // Placeholder, will be fetched from API
            price: 0,
            change: 0,
            changePercent: 0,
            sector: stock.sector,
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
            earningsPerShare: '0',
        };

        router.push({
            pathname: '/stock/[ticker]',
            params: {
                ticker: stock.symbol,
                stock: JSON.stringify(stockData),
            },
        });
    };

    return (
        <TouchableOpacity
            activeOpacity={0.7}
            onPress={() => setIsExpanded(!isExpanded)}
            style={[
                styles.suggestionCard,
                {
                    backgroundColor: "white",
                    borderColor: sectorColor,
                    maxHeight: isExpanded ? 320 : 120,
                },
            ]}
        >
            {/* Header Row */}
            <View style={styles.cardHeader}>
                <View style={styles.leftContent}>
                    <View style={[styles.iconBadge, { backgroundColor: sectorColor + "20" }]}>
                        <MaterialCommunityIcons
                            name={icon as any}
                            size={20}
                            color={sectorColor}
                        />
                    </View>

                    <View style={styles.stockInfo}>
                        <Text style={[styles.stockTickerBig, { color: sectorColor }]}>
                            {stock.symbol}
                        </Text>
                        <Text style={[styles.reasonTag, { color: sectorColor }]}>
                            {stock.reason}
                        </Text>
                    </View>
                </View>

                <View style={styles.priceSection}>
                    <Text style={[styles.priceText, { color: sectorColor }]}>
                        {stock.price}
                    </Text>
                    <Text
                        style={[
                            styles.changeText,
                            {
                                color: stock.change.startsWith("+") ? "#2E7D32" : "#C62828",
                            },
                        ]}
                    >
                        {stock.change}
                    </Text>
                </View>
            </View>

            {/* Expanded Section */}
            {isExpanded && (
                <View style={[styles.expandedContent, { borderTopColor: "#F3F6FA" }]}>
                    <View style={styles.metricRow}>
                        <View style={styles.metricItem}>
                            <Text style={styles.metricLabelBlack}>Volatility</Text>
                            <Text style={styles.metricValueBlack}>{stock.volatility}</Text>
                        </View>

                        <View style={styles.metricItem}>
                            <Text style={styles.metricLabelBlack}>News Volume</Text>
                            <Text style={styles.metricValueBlack}>{stock.newsVolume}</Text>
                        </View>

                        <View style={styles.metricItem}>
                            <Text style={styles.metricLabelBlack}>Sector</Text>
                            <View style={{ maxWidth: 80 }}>
                                <Text
                                    style={styles.metricValueBlack}
                                    numberOfLines={1}
                                    ellipsizeMode="tail"
                                >
                                    {stock.sector}
                                </Text>
                            </View>
                        </View>
                    </View>

                    <View style={[styles.insightBox, { backgroundColor: "#F3F6FA", borderColor: "#F3F6FA" }]}>
                        <MaterialCommunityIcons
                            name="lightbulb-on"
                            size={16}
                            color="#000"
                            style={{ marginRight: 6 }}
                        />
                        <Text style={styles.insightTextBlack}>{stock.insight}</Text>
                    </View>

                    <TouchableOpacity style={styles.viewButtonBlue} onPress={handleViewDetails}>
                        <Text style={styles.viewButtonBlueText}>View Details →</Text>
                    </TouchableOpacity>
                </View>
            )}
        </TouchableOpacity>
    );
};

// ============================================================================
// MAIN COMPONENT
// ============================================================================

export const SuggestedForYou = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const router = useRouter();
    const { activeAccount } = useAuth();

    const [suggestions, setSuggestions] = useState<SuggestionStock[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [refreshing, setRefreshing] = useState(false);

    // ========================================================================
    // Fetch Suggestions
    // ========================================================================

    const fetchSuggestions = async (forceRefresh = false) => {
        if (!activeAccount?.accountId) {
            setError("No active account");
            setLoading(false);
            return;
        }

        try {
            setLoading(true);
            setError(null);

            console.log(`🔍 Fetching suggestions for account ${activeAccount.accountId}`);
            const data = await generateStockSuggestions(
                activeAccount.accountId,
                5,
                forceRefresh
            );

            setSuggestions(data);
            console.log(`✅ Loaded ${data.length} suggestions`);
        } catch (err) {
            console.error("Failed to fetch suggestions:", err);
            setError("Unable to load suggestions");
        } finally {
            setLoading(false);
        }
    };

    // ========================================================================
    // Lifecycle
    // ========================================================================

    useEffect(() => {
        if (activeAccount?.accountId) {
            fetchSuggestions();
        }
    }, [activeAccount?.accountId]);



    // ========================================================================
    // Loading State
    // ========================================================================

    if (loading && suggestions.length === 0) {
        return (
            <View style={styles.wrapper}>
                <View style={[styles.header, { borderBottomColor: Colors.card }]}>
                    <View>
                        <Text style={[styles.title, { color: Colors.text }]}>
                            Suggested for You
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Based on your watchlist & holdings
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="lightbulb-multiple"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>

                <View style={{ paddingHorizontal: 24, paddingVertical: 40, alignItems: "center" }}>
                    <ActivityIndicator size="large" color={Colors.tint} />
                    <Text style={{ color: Colors.text, opacity: 0.6, marginTop: 12 }}>
                        Analyzing market trends...
                    </Text>
                </View>
            </View>
        );
    }

    // ========================================================================
    // Error State
    // ========================================================================

    if (error && suggestions.length === 0) {
        return (
            <View style={styles.wrapper}>
                <View style={[styles.header, { borderBottomColor: Colors.card }]}>
                    <View>
                        <Text style={[styles.title, { color: Colors.text }]}>
                            Suggested for You
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Based on your watchlist & holdings
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="lightbulb-multiple"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>

                <View style={{ paddingHorizontal: 24, paddingVertical: 40, alignItems: "center" }}>
                    <MaterialCommunityIcons
                        name="alert-circle-outline"
                        size={48}
                        color={Colors.text}
                        style={{ opacity: 0.3 }}
                    />
                    <Text style={{ color: Colors.text, opacity: 0.6, marginTop: 12, textAlign: "center" }}>
                        {error}
                    </Text>
                    <TouchableOpacity
                        onPress={() => fetchSuggestions()}
                        style={{ marginTop: 16, paddingHorizontal: 16, paddingVertical: 8 }}
                    >
                        <Text style={{ color: Colors.tint, fontWeight: "600", fontSize: 14 }}>
                            Retry
                        </Text>
                    </TouchableOpacity>
                </View>
            </View>
        );
    }

    // ========================================================================
    // Fully Diversified State
    // ========================================================================

    if (suggestions.length === 1 && suggestions[0].symbol === "DIVERSIFY") {
        return (
            <View style={styles.wrapper}>
                <View style={[styles.header, { borderBottomColor: Colors.card }]}>
                    <View>
                        <Text style={[styles.title, { color: Colors.text }]}>
                            Suggested for You
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Based on your watchlist & holdings
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="lightbulb-multiple"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>

                <View style={{ paddingHorizontal: 24, paddingVertical: 40, alignItems: "center" }}>
                    <MaterialCommunityIcons
                        name="check-circle-outline"
                        size={48}
                        color={Colors.tint}
                    />
                    <Text style={{ color: Colors.text, fontWeight: "600", marginTop: 12, fontSize: 16 }}>
                        Fully Diversified!
                    </Text>
                    <Text style={{ color: Colors.text, opacity: 0.6, marginTop: 8, textAlign: "center" }}>
                        {suggestions[0].insight}
                    </Text>
                </View>
            </View>
        );
    }

    // ========================================================================
    // Main Render
    // ========================================================================

    return (
        <View style={styles.wrapper}>
            <View style={[styles.header, { borderBottomColor: Colors.card }]}>
                <View>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Suggested for You
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Refreshes daily based on your watchlist & holdings
                    </Text>
                </View>
                <MaterialCommunityIcons
                    name="lightbulb-multiple"
                    size={28}
                    color={Colors.tint}
                    style={{ opacity: 0.7 }}
                />
            </View>

            <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                scrollEventThrottle={16}
                style={styles.cardsContainer}
                contentContainerStyle={styles.contentContainer}
            >
                {suggestions.map((stock) => {
                    const sectorColor = getSectorColor(stock.sector);
                    return (
                        <SuggestionCard
                            key={stock.symbol}
                            stock={stock}
                            reason={stock.reason}
                            icon={stock.icon}
                            sectorColor={sectorColor.color}
                        />
                    );
                })}
            </ScrollView>

            <View style={[styles.footer, { backgroundColor: Colors.card + "40" }]}>
                <MaterialCommunityIcons
                    name="information-outline"
                    size={16}
                    color={Colors.text}
                    style={{ opacity: 0.6 }}
                />
                <Text style={[styles.footerText, { color: Colors.text, opacity: 0.6 }]}>
                    Tap cards to explore insights.
                </Text>
            </View>

            {refreshing && (
                <View style={styles.loadingOverlay}>
                    <ActivityIndicator size="small" color={Colors.tint} />
                </View>
            )}
        </View>
    );
};

// ============================================================================
// STYLES
// ============================================================================

const styles = StyleSheet.create({
    wrapper: { marginTop: 24, marginBottom: 20 },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "flex-start",
        paddingHorizontal: 24,
        paddingBottom: 12,
        borderBottomWidth: 1,
        marginBottom: 16,
    },
    title: { fontSize: 20, fontWeight: "800", fontStyle: "italic", marginLeft: -3 },
    subtitle: { fontSize: 12, marginTop: 4, marginLeft: -3 },
    cardsContainer: { paddingHorizontal: 24 },
    contentContainer: { gap: 12, paddingRight: 12 },
    suggestionCard: {
        width: width - 85,
        minHeight: 80,
        borderRadius: 16,
        borderWidth: 2,
        padding: 16,
        justifyContent: "flex-start",
    },
    cardHeader: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        width: "100%",
    },
    leftContent: { flexDirection: "row", alignItems: "center", flex: 1, gap: 12 },
    iconBadge: {
        width: 48,
        height: 48,
        borderRadius: 12,
        justifyContent: "center",
        alignItems: "center",
    },
    stockInfo: { flex: 1, gap: 2 },
    stockTickerBig: { fontSize: 16, fontWeight: "800" },
    reasonTag: { fontSize: 11, fontWeight: "600" },
    priceSection: { alignItems: "flex-end", gap: 4 },
    priceText: { fontSize: 16, fontWeight: "700" },
    changeText: { fontSize: 13, fontWeight: "700" },
    expandedContent: {
        marginTop: 14,
        paddingTop: 14,
        borderTopWidth: 1,
        gap: 12,
    },
    metricRow: {
        flexDirection: "row",
        justifyContent: "space-around",
        marginBottom: 4,
    },
    metricItem: { alignItems: "center", gap: 4 },
    metricLabelBlack: { fontSize: 10, fontWeight: "600", color: "black" },
    metricValueBlack: { fontSize: 12, fontWeight: "700", color: "black"},
    insightBox: {
        flexDirection: "row",
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 10,
        borderWidth: 1,
        alignItems: "flex-start",
    },
    insightTextBlack: {
        fontSize: 12,
        fontWeight: "500",
        flex: 1,
        lineHeight: 16,
        color: "black",
    },
    viewButtonBlue: {
        paddingVertical: 10,
        borderRadius: 10,
        alignItems: "center",
        backgroundColor: "#266EF1",
    },
    viewButtonBlueText: { fontSize: 13, fontWeight: "700", color: "white" },
    footer: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        gap: 8,
        paddingHorizontal: 24,
        paddingVertical: 10,
        marginHorizontal: 24,
        borderRadius: 10,
        marginTop: 12,
    },
    footerText: { fontSize: 11, fontWeight: "500" },
    loadingOverlay: {
        position: "absolute",
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: "rgba(0, 0, 0, 0.15)",
        justifyContent: "center",
        alignItems: "center",
        pointerEvents: "none",
    },
});

export default SuggestedForYou;