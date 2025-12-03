import React from "react";
import { View, Text, useColorScheme, StyleSheet, Dimensions, Image } from "react-native";
import { getThemeColors } from "../../../src/constants/colors";
import Carousel from "react-native-reanimated-carousel";
import { MaterialCommunityIcons } from "@expo/vector-icons";

const screenWidth = Dimensions.get("window").width - 48;

// ★ Sector color coding
const sectorColors = {
    "Technology": "#0369A1",
    "Semiconductors": "#EF6C00",
    "FinTech": "#15803D",
    "Consumer/Tech": "#6D28D9",
    "Healthcare": "#BE123C",
    "Markets": "#7C3AED",
};

// ★ Added sectors for each company
const watchlistStocks = [
    { id: 1, symbol: "AAPL", price: "A$150.25", change: "+2.5%", sector: "Technology" },
    { id: 2, symbol: "MSFT", price: "A$380.50", change: "+1.8%", sector: "Technology" },
    { id: 3, symbol: "GOOGL", price: "A$140.75", change: "+3.2%", sector: "Technology" },
    { id: 4, symbol: "TSLA", price: "A$245.30", change: "-1.5%", sector: "Consumer/Tech" },
    { id: 5, symbol: "AMZN", price: "A$170.90", change: "+2.1%", sector: "Technology" },
];

export const WatchlistHighlights = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

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

            <Carousel
                width={screenWidth / 1.2}
                height={120}
                data={watchlistStocks}
                loop
                mode="parallax"
                modeConfig={{
                    parallaxScrollingScale: 0.85,
                    parallaxScrollingOffset: 40,
                }}
                renderItem={({ item }) => (
                    <View style={[styles.stockCard, { backgroundColor: "white" }]}>
                        <Image
                            source={require('../../../assets/images/apple.png')}
                            style={{ width: 24, height: 24, marginBottom: 4 }}
                        />

                        {/* ★ Sector-colored stock symbol */}
                        <Text
                            style={[
                                styles.stockSymbol,
                                { color: sectorColors[item.sector as keyof typeof sectorColors] || Colors.tint }
                            ]}
                        >
                            {item.symbol}
                        </Text>

                        <Text style={[styles.stockPrice, { color: Colors.text }]}>
                            {item.price}
                        </Text>

                        <Text
                            style={[
                                styles.stockChange,
                                { color: item.change.startsWith("+") ? "#2E7D32" : "#C62828" }
                            ]}
                        >
                            {item.change}
                        </Text>
                    </View>
                )}
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
    stockSymbol: { fontSize: 14, fontWeight: "bold" },
    stockPrice: { fontSize: 16, fontWeight: "bold", marginTop: 3 },
    stockChange: { fontSize: 14, fontWeight: "bold", marginTop: 3, opacity: 0.7 },
});
