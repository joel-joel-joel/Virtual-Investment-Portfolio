import React, { useState } from "react";
import { View, Text, StyleSheet, Dimensions, ScrollView, TouchableOpacity, Image } from "react-native";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { getThemeColors } from "../../constants/colors";
import { useColorScheme } from "react-native";

const { width } = Dimensions.get("window");


const sectorColors = {
    "Technology":      { color: "#0369A1" },
    "Semiconductors":  { color: "#EF6C00" },
    "FinTech":         { color: "#15803D" },
    "Consumer/Tech":   { color: "#6D28D9" },
    "Healthcare":      { color: "#BE123C" },
    "Markets":         { color: "#7C3AED" },
};





// @ts-ignore
const SuggestionCard = ({ stock, reason, icon, sectorColor }) => {
    const [isExpanded, setIsExpanded] = useState(false);

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
                            style={{ marginRight: 8 }}
                        />
                        <Text style={styles.insightTextBlack}>
                            {stock.insight}
                        </Text>
                    </View>

                    <TouchableOpacity style={styles.viewButtonBlue}>
                        <Text style={styles.viewButtonBlueText}>View Details →</Text>
                    </TouchableOpacity>
                </View>
            )}
        </TouchableOpacity>
    );
};


export const SuggestedForYou = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Sample data - sorted by: volatility, news volume, sector exposure
    const suggestedStocks = [
        {
            symbol: "NVDA",
            price: "A$892.50",
            change: "+8.5%",
            reason: "High Volatility",
            icon: "lightning-bolt",
            volatility: "High (28%)",
            newsVolume: "Very High",
            sector: "Technology",
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
                    const sectorTheme = sectorColors[stock.sector] || sectorColors.Default;

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

const styles = StyleSheet.create({
    wrapper: {
        marginTop: 24,
        marginBottom: 20,
    },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "flex-start",
        paddingHorizontal: 24,
        paddingBottom: 12,
        borderBottomWidth: 1,
        marginBottom: 16,
    },
    title: {
        fontSize: 20,
        fontWeight: "800",
        fontStyle: "italic",
        marginLeft: -9,
    },
    subtitle: {
        fontSize: 12,
        marginTop: 4,
        marginLeft: -9,
    },
    cardsContainer: {
        paddingHorizontal: 24,
    },
    contentContainer: {
        gap: 12,
        paddingRight: 12,
    },
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
    leftContent: {
        flexDirection: "row",
        alignItems: "center",
        flex: 1,
        gap: 12,
    },
    iconBadge: {
        width: 48,
        height: 48,
        borderRadius: 12,
        justifyContent: "center",
        alignItems: "center",
    },
    stockInfo: {
        flex: 1,
        gap: 2,
    },
    stockTickerBig: {
        fontSize: 16,
        fontWeight: "800",
    },
    reasonTag: {
        fontSize: 11,
        fontWeight: "600",
    },
    priceSection: {
        alignItems: "flex-end",
        gap: 4,
    },
    priceText: {
        fontSize: 16,
        fontWeight: "700",
    },
    changeText: {
        fontSize: 13,
        fontWeight: "700",
    },
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
    metricItem: {
        alignItems: "center",
        gap: 4,
    },
    metricLabel: {
        fontSize: 10,
        fontWeight: "600",
    },
    metricValue: {
        fontSize: 12,
        fontWeight: "700",
    },
    insightBox: {
        flexDirection: "row",
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 10,
        borderWidth: 1,
        alignItems: "flex-start",
    },
    insightText: {
        fontSize: 12,
        fontWeight: "500",
        flex: 1,
        lineHeight: 16,
    },
    viewButton: {
        paddingVertical: 10,
        borderRadius: 10,
        alignItems: "center",
        borderWidth: 1,
        marginTop: 4,
    },
    viewButtonText: {
        fontSize: 13,
        fontWeight: "700",
    },
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
    footerText: {
        fontSize: 11,
        fontWeight: "500",
    },
    metricLabelBlack: {
        fontSize: 10,
        fontWeight: "600",
        color: "black",
    },
    metricValueBlack: {
        fontSize: 12,
        fontWeight: "700",
        color: "black",
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
    viewButtonBlueText: {
        fontSize: 13,
        fontWeight: "700",
        color: "white",
    },

});

export default SuggestedForYou;