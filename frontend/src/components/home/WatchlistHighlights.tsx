import React, { useState, useEffect, useCallback } from "react";
import { View, Text, useColorScheme, StyleSheet, Dimensions, ActivityIndicator } from "react-native";
import { getThemeColors } from "../../../src/constants/colors";
import { getSectorColor } from "@/src/services/sectorColorService";
import Carousel from "react-native-reanimated-carousel";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { getWatchlist } from "@/src/services/portfolioService";
import type { WatchlistDTO } from "@/src/types/api";
import { useFocusEffect } from "@react-navigation/native";

const screenWidth = Dimensions.get("window").width - 48;

export const WatchlistHighlights = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    const [watchlistStocks, setWatchlistStocks] = useState<WatchlistDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // ✅ NEW: Fetch watchlist data
    const loadWatchlist = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            console.log('📥 WatchlistHighlights: Fetching watchlist...');
            const data = await getWatchlist();

            console.log('✅ Watchlist data received:', data);

            // Sort by addedAt (most recent first) and take top 5
            const sortedByRecent = [...data].sort((a, b) => {
                const dateA = new Date(a.addedAt).getTime();
                const dateB = new Date(b.addedAt).getTime();
                return dateB - dateA; // Most recent first
            });

            const topFive = sortedByRecent.slice(0, 5);

            console.log('📊 Top 5 most recent:', topFive);
            setWatchlistStocks(topFive);
        } catch (err) {
            console.error('❌ Failed to load watchlist:', err);
            setError('Failed to load watchlist');
            setWatchlistStocks([]);
        } finally {
            setLoading(false);
        }
    }, []);

    // Load watchlist on mount
    useEffect(() => {
        loadWatchlist();
    }, [loadWatchlist]);

    // Reload when screen comes into focus
    useFocusEffect(
        useCallback(() => {
            loadWatchlist();
        }, [loadWatchlist])
    );

    // Loading state
    if (loading) {
        return (
            <View style={[styles.wrapper, { backgroundColor: Colors.card }]}>
                <View style={styles.header}>
                    <View>
                        <Text style={[styles.title, { color: Colors.text, fontStyle: "italic" }]}>
                            Watchlist Highlights
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Based on your watchlist & holdings
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="search-web"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>
                <ActivityIndicator size="large" color={Colors.tint} />
            </View>
        );
    }

    // Error state
    if (error || watchlistStocks.length === 0) {
        return (
            <View style={[styles.wrapper, { backgroundColor: Colors.card }]}>
                <View style={styles.header}>
                    <View>
                        <Text style={[styles.title, { color: Colors.text, fontStyle: "italic" }]}>
                            Watchlist Highlights
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            {error || "Add stocks to your watchlist"}
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="search-web"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>
            </View>
        );
    }

    return (
        <View style={[styles.wrapper, { backgroundColor: Colors.card }]}>
            <View style={styles.header}>
                <View>
                    <Text style={[styles.title, { color: Colors.text, fontStyle: "italic" }]}>
                        Watchlist Highlights
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                        Top {watchlistStocks.length} recently added
                    </Text>
                </View>
                <MaterialCommunityIcons
                    name="search-web"
                    size={28}
                    color={Colors.tint}
                    style={{ opacity: 0.7 }}
                />
            </View>

            <Carousel
                width={screenWidth / 1.2}
                height={120}
                data={watchlistStocks}
                loop={watchlistStocks.length > 1}
                mode="parallax"
                modeConfig={{
                    parallaxScrollingScale: 0.85,
                    parallaxScrollingOffset: 40,
                }}
                renderItem={({ item }) => {
                    // ✅ NEW: Use getSectorColor for consistent color coding
                    const sectorColor = getSectorColor(item.sector || 'Other');
                    const isPositive = (item.priceChangePercent ?? 0) >= 0;

                    return (
                        <View style={[styles.stockCard, { backgroundColor: sectorColor.bgLight }]}>
                            {/* Stock Symbol with sector color */}
                            <Text
                                style={[
                                    styles.stockSymbol,
                                    { color: sectorColor.color }
                                ]}
                            >
                                {item.stockCode}
                            </Text>

                            {/* Current Price */}
                            <Text style={[styles.stockPrice, { color: Colors.text }]}>
                                A${(item.currentPrice ?? 0).toFixed(2)}
                            </Text>

                            {/* Price Change */}
                            <View style={styles.changeContainer}>
                                <MaterialCommunityIcons
                                    name={isPositive ? 'trending-up' : 'trending-down'}
                                    size={12}
                                    color={isPositive ? '#2E7D32' : '#C62828'}
                                    style={{ marginRight: 2 }}
                                />
                                <Text
                                    style={[
                                        styles.stockChange,
                                        { color: isPositive ? '#2E7D32' : '#C62828' }
                                    ]}
                                >
                                    {isPositive ? '+' : ''}{(item.priceChangePercent ?? 0).toFixed(2)}%
                                </Text>
                            </View>
                        </View>
                    );
                }}
            />
        </View>
    );
};

const styles = StyleSheet.create({
    wrapper: {
        marginTop: 20,
        justifyContent: "center",
        alignItems: "center",
        padding: 20,
        borderRadius: 20,
        height: 200,
    },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "center",
        width: "100%",
        marginBottom: 15,
    },
    title: {
        fontSize: 18,
        fontWeight: "800",
        marginBottom: 2,
        textAlign: "left",
        marginLeft: -3
    },
    subtitle: {
        fontSize: 12,
        marginBottom: -3,
        textAlign: "left",
        marginTop: 3
    },
    stockCard: {
        borderRadius: 12,
        padding: 12,
        alignItems: "center",
        justifyContent: "center",
    },
    stockSymbol: {
        fontSize: 14,
        fontWeight: "bold"
    },
    stockPrice: {
        fontSize: 16,
        fontWeight: "bold",
        marginTop: 3
    },
    changeContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 3,
    },
    stockChange: {
        fontSize: 14,
        fontWeight: "bold",
        opacity: 0.7
    },
});