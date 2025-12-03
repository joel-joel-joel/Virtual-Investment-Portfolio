import React from "react";
import { View, Text, StyleSheet, useColorScheme, Dimensions } from "react-native";
import { getThemeColors } from "../../../src/constants/colors";
import Carousel from "react-native-reanimated-carousel";

const screenWidth = Dimensions.get("window").width - 48;

const sectorColors = {
    Technology: "#0369A1",
    Semiconductors: "#EF6C00",
    FinTech: "#15803D",
    "Consumer/Tech": "#6D28D9",
    Healthcare: "#BE123C",
    Markets: "#7C3AED",
};


const topMovers = [
    { id: 1, symbol: "NVDA", change: "+8.5%", sector: "Technology" },
    { id: 2, symbol: "TSLA", change: "+6.2%", sector: "Consumer/Tech" },
    { id: 3, symbol: "AMD", change: "+5.1%", sector: "Semiconductors" },
    { id: 4, symbol: "META", change: "-4.3%", sector: "Technology" },
    { id: 5, symbol: "INTEL", change: "-3.7%", sector: "Semiconductors" },
];


const topHoldings = [
    { id: 1, symbol: "AAPL", percentage: "32.5%", sector: "Technology" },
    { id: 2, symbol: "MSFT", percentage: "18.3%", sector: "Technology" },
    { id: 3, symbol: "GOOGL", percentage: "15.7%", sector: "Technology" },
    { id: 4, symbol: "TSLA", percentage: "12.1%", sector: "Consumer/Tech" },
    { id: 5, symbol: "AMZN", percentage: "8.2%", sector: "Technology" },
];


export const TopMovers = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // @ts-ignore
    // @ts-ignore
    return (
        <View style={{ flexDirection: "row", justifyContent: "space-between", marginTop: 20 }}>
            {/* Top Movers */}
            <View style={{ flex: 1, alignItems: "center" }}>
                <Text style={[styles.header, { color: Colors.text, fontStyle: "italic" }]}>Top Movers</Text>
                <Carousel
                    width={screenWidth / 2 - 30}
                    height={88}
                    data={topMovers}
                    loop
                    vertical
                    mode="parallax"
                    modeConfig={{
                        parallaxScrollingScale: 0.75,
                        parallaxScrollingOffset: 30,
                    }}
                    renderItem={({ item }) => (
                        <View style={[styles.card, { backgroundColor: Colors.card }]}>
                            <Text style={{
                                color: sectorColors[item.sector as keyof typeof sectorColors] || Colors.tint,
                                fontWeight: "700"
                            }}>
                                {item.symbol}
                            </Text>

                            <Text style={{ color: item.change.startsWith("+") ? "#2E7D32" : "#C62828", fontWeight: "800" }}>
                                {item.change}
                            </Text>
                        </View>
                    )}
                />
            </View>

            {/* Top % Holdings */}
            <View style={{ flex: 1, alignItems: "center" }}>
                <Text style={[styles.header, { color: Colors.text, fontStyle: "italic" }]}>Top % Holdings</Text>
                <Carousel
                    width={screenWidth / 2 - 30}
                    height={88}
                    data={topHoldings}
                    loop
                    vertical
                    mode="parallax"
                    modeConfig={{
                        parallaxScrollingScale: 0.75,
                        parallaxScrollingOffset: 30,
                    }}
                    renderItem={({ item }) => (
                        <View style={[styles.card, { backgroundColor: Colors.card }]}>
                            <Text style={{
                                color: sectorColors[item.sector as keyof typeof sectorColors] || Colors.tint,
                                fontWeight: "700"
                            }}>
                                {item.symbol}
                            </Text>
                            <Text style={{ color: Colors.text, fontWeight: "800" }}>{item.percentage}</Text>
                        </View>
                    )}
                />
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    header: {
        fontSize: 12,
        fontWeight: "700",
        marginBottom: 8,
    },
    card: {
        borderRadius: 12,
        padding: 8,
        justifyContent: "center",
        alignItems: "center",
        marginVertical: 3,
        height: 70,
    },
});
