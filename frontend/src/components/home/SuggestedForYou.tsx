import React, { useState } from "react";
import { View, Text, StyleSheet, Dimensions, ScrollView, TouchableOpacity, useColorScheme } from "react-native";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { getThemeColors } from "../../constants/colors";
import { useRouter } from "expo-router";

const { width } = Dimensions.get("window");

const sectorColors = {
    "Technology":      { color: "#0369A1" },
    "Semiconductors":  { color: "#EF6C00" },
    "FinTech":         { color: "#15803D" },
    "Consumer/Tech":   { color: "#6D28D9" },
    "Healthcare":      { color: "#BE123C" },
    "Markets":         { color: "#7C3AED" },
};

// ------------------------- STOCK DATA -------------------------
const STOCK_DATA: Record<string, any> = {
    'NVDA': {
        symbol: 'NVDA',
        name: 'NVIDIA Corporation',
        price: 892.50,
        change: 25.30,
        changePercent: 2.92,
        sector: 'Semiconductors',
        marketCap: '2.2T',
        peRatio: '52.3',
        dividend: '0.10',
        dayHigh: 910.00,
        dayLow: 880.00,
        yearHigh: 950.00,
        yearLow: 400.00,
        description: 'NVIDIA Corporation is a technology company that designs and manufactures graphics processing units (GPUs) and system-on-chip units.',
        employees: '32,005',
        founded: '1993',
        website: 'www.nvidia.com',
        nextEarningsDate: '2024-12-12',
        nextDividendDate: '2024-12-15',
        earningsPerShare: '$3.25',
    },
    'COIN': {
        symbol: 'COIN',
        name: 'Coinbase Global',
        price: 178.30,
        change: 12.30,
        changePercent: 7.40,
        sector: 'FinTech',
        marketCap: '90B',
        peRatio: '45.2',
        dividend: '0.00',
        dayHigh: 185.00,
        dayLow: 170.00,
        yearHigh: 200.00,
        yearLow: 60.00,
        description: 'Coinbase Global is a cryptocurrency exchange platform.',
        employees: '5,100',
        founded: '2012',
        website: 'www.coinbase.com',
        nextEarningsDate: '2025-02-12',
        nextDividendDate: 'N/A',
        earningsPerShare: '$3.95',
    },
    'UBER': {
        symbol: 'UBER',
        name: 'Uber Technologies',
        price: 72.15,
        change: 1.50,
        changePercent: 2.12,
        sector: 'Consumer/Tech',
        marketCap: '150B',
        peRatio: '65.2',
        dividend: '0.00',
        dayHigh: 75.00,
        dayLow: 70.00,
        yearHigh: 80.00,
        yearLow: 40.00,
        description: 'Uber Technologies is a ride-sharing and delivery platform.',
        employees: '76,715',
        founded: '2009',
        website: 'www.uber.com',
        nextEarningsDate: '2025-02-10',
        nextDividendDate: 'N/A',
        earningsPerShare: '$1.10',
    },
    'MSTR': {
        symbol: 'MSTR',
        name: 'Microstrategy',
        price: 410.80,
        change: 64.50,
        changePercent: 15.70,
        sector: 'Technology',
        marketCap: '45B',
        peRatio: '120.5',
        dividend: '0.00',
        dayHigh: 425.00,
        dayLow: 400.00,
        yearHigh: 500.00,
        yearLow: 150.00,
        description: 'Microstrategy is a software company that provides business intelligence solutions.',
        employees: '2,500',
        founded: '1989',
        website: 'www.microstrategy.com',
        nextEarningsDate: '2025-03-05',
        nextDividendDate: 'N/A',
        earningsPerShare: '$3.40',
    },
    'AMD': {
        symbol: 'AMD',
        name: 'Advanced Micro Devices',
        price: 165.45,
        change: 4.50,
        changePercent: 2.79,
        sector: 'Semiconductors',
        marketCap: '268B',
        peRatio: '42.1',
        dividend: '0.00',
        dayHigh: 168.00,
        dayLow: 162.00,
        yearHigh: 190.00,
        yearLow: 90.00,
        description: 'Advanced Micro Devices is a semiconductor company that designs and manufactures microprocessors.',
        employees: '23,500',
        founded: '1969',
        website: 'www.amd.com',
        nextEarningsDate: '2025-02-03',
        nextDividendDate: 'N/A',
        earningsPerShare: '$3.92',
    },
};

// ------------------------- SUGGESTION CARD -------------------------
// @ts-ignore
const SuggestionCard = ({ stock, reason, icon, sectorColor }) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const router = useRouter();
    const stockData = STOCK_DATA[stock.symbol];

    const handleViewDetails = () => {
        if (stockData) {
            router.push({
                pathname: '/stock/[ticker]',
                params: {
                    ticker: stockData.symbol,
                    stock: JSON.stringify(stockData),
                },
            });
        }
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
                    maxHeight: isExpanded ? 280 : 120,
                }
            ]}
        >
            {/* Header Row */}
            <View style={styles.cardHeader}>
                <View style={styles.leftContent}>
                    <View style={[styles.iconBadge, { backgroundColor: sectorColor + "20" }]}>
                        <MaterialCommunityIcons
                            name={icon}
                            size={20}
                            color={sectorColor}
                        />
                    </View>

                    <View style={styles.stockInfo}>
                        <Text style={[styles.stockTickerBig, { color: sectorColor }]}>
                            {stock.symbol}
                        </Text>
                        <Text style={[styles.reasonTag, { color: sectorColor }]}>
                            {reason}
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
                            <Text style={styles.metricValueBlack}>{stock.sector}</Text>
                        </View>
                    </View>

                    <View style={[styles.insightBox, { backgroundColor: "#F3F6FA", borderColor: "#F3F6FA" }]}>
                        <MaterialCommunityIcons
                            name="lightbulb-on"
                            size={16}
                            color="#000"
                            style={{ marginRight: 0 }}
                        />
                        <Text style={styles.insightTextBlack}>
                            {stock.insight}
                        </Text>
                    </View>

                    <TouchableOpacity style={styles.viewButtonBlue} onPress={handleViewDetails}>
                        <Text style={styles.viewButtonBlueText}>View Details →</Text>
                    </TouchableOpacity>
                </View>
            )}
        </TouchableOpacity>
    );
};

// ------------------------- MAIN COMPONENT -------------------------
export const SuggestedForYou = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Sample data with sectorColors applied
    const suggestedStocks = [
        {
            symbol: "NVDA",
            price: "A$892.50",
            change: "+8.5%",
            reason: "High Volatility",
            icon: "lightning-bolt",
            volatility: "High (28%)",
            newsVolume: "Very High",
            sector: "Semiconductors",
            insight: "Strong AI momentum. Your holdings overlap with similar trends. Consider averaging up.",
        },
        {
            symbol: "COIN",
            price: "A$178.30",
            change: "+12.3%",
            reason: "Trending News",
            icon: "trending-up",
            volatility: "Very High (35%)",
            newsVolume: "Extreme",
            sector: "FinTech",
            insight: "Crypto regulatory news spiking. Complements tech exposure without sector overlap.",
        },
        {
            symbol: "UBER",
            price: "A$72.15",
            change: "+2.1%",
            reason: "Sector Diversification",
            icon: "briefcase-variant",
            volatility: "Medium (16%)",
            newsVolume: "Moderate",
            sector: "Consumer/Tech",
            insight: "Less correlated with your current holdings. Adds stability while maintaining growth potential.",
        },
        {
            symbol: "MSTR",
            price: "A$410.80",
            change: "+15.7%",
            reason: "High Volatility",
            icon: "chart-line",
            volatility: "Extreme (42%)",
            newsVolume: "High",
            sector: "Technology",
            insight: "Bitcoin proxy with amplified moves. Higher risk, but aligns with your risk tolerance.",
        },
        {
            symbol: "AMD",
            price: "A$165.45",
            change: "+5.9%",
            reason: "Trending News",
            icon: "chip",
            volatility: "High (26%)",
            newsVolume: "High",
            sector: "Semiconductors",
            insight: "GPU innovation momentum. Competitive threat/opportunity to NVDA in your holdings.",
        },
    ];

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

            <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                scrollEventThrottle={16}
                style={styles.cardsContainer}
                contentContainerStyle={styles.contentContainer}
            >
                {suggestedStocks.map((stock) => {
                    // @ts-ignore
                    const sectorTheme = sectorColors[stock.sector] || { color: "#000" };
                    return (
                        <SuggestionCard
                            key={stock.symbol}
                            stock={stock}
                            reason={stock.reason}
                            icon={stock.icon}
                            sectorColor={sectorTheme.color}
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
        </View>
    );
};

// ------------------------- STYLES -------------------------
const styles = StyleSheet.create({
    wrapper: { marginTop: 24, marginBottom: 20 },
    header: { flexDirection: "row", justifyContent: "space-between", alignItems: "flex-start", paddingHorizontal: 24, paddingBottom: 12, borderBottomWidth: 1, marginBottom: 16 },
    title: { fontSize: 20, fontWeight: "800", fontStyle: "italic", marginLeft: -3 },
    subtitle: { fontSize: 12, marginTop: 4, marginLeft: -3 },
    cardsContainer: { paddingHorizontal: 24 },
    contentContainer: { gap: 12, paddingRight: 12 },
    suggestionCard: { width: width - 85, minHeight: 80, borderRadius: 16, borderWidth: 2, padding: 16, justifyContent: "flex-start" },
    cardHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", width: "100%" },
    leftContent: { flexDirection: "row", alignItems: "center", flex: 1, gap: 12 },
    iconBadge: { width: 48, height: 48, borderRadius: 12, justifyContent: "center", alignItems: "center" },
    stockInfo: { flex: 1, gap: 2 },
    stockTickerBig: { fontSize: 16, fontWeight: "800" },
    reasonTag: { fontSize: 11, fontWeight: "600" },
    priceSection: { alignItems: "flex-end", gap: 4 },
    priceText: { fontSize: 16, fontWeight: "700" },
    changeText: { fontSize: 13, fontWeight: "700" },
    expandedContent: { marginTop: 14, paddingTop: 14, borderTopWidth: 1, gap: 12 },
    metricRow: { flexDirection: "row", justifyContent: "space-around", marginBottom: 4 },
    metricItem: { alignItems: "center", gap: 4 },
    metricLabelBlack: { fontSize: 10, fontWeight: "600", color: "black" },
    metricValueBlack: { fontSize: 12, fontWeight: "700", color: "black" },
    insightBox: { flexDirection: "row", borderRadius: 10, paddingHorizontal: 12, paddingVertical: 10, borderWidth: 1, alignItems: "flex-start" },
    insightTextBlack: { fontSize: 12, fontWeight: "500", flex: 1, lineHeight: 16, color: "black" },
    viewButtonBlue: { paddingVertical: 10, borderRadius: 10, alignItems: "center", backgroundColor: "#266EF1" },
    viewButtonBlueText: { fontSize: 13, fontWeight: "700", color: "white" },
    footer: { flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8, paddingHorizontal: 24, paddingVertical: 10, marginHorizontal: 24, borderRadius: 10, marginTop: 12 },
    footerText: { fontSize: 11, fontWeight: "500" },
});

export default SuggestedForYou;
