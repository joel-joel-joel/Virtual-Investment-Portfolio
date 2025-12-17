import React, { useMemo } from "react";
import { View, Text, StyleSheet, useColorScheme, Dimensions } from "react-native";
import { getThemeColors } from "../../../src/constants/colors";
import Carousel from "react-native-reanimated-carousel";
import type { HoldingDTO } from "@/src/types/api";

const screenWidth = Dimensions.get("window").width - 48;

const sectorColors: Record<string, string> = {
    Technology: "#0369A1",
    Semiconductors: "#EF6C00",
    FinTech: "#15803D",
    "Consumer/Tech": "#6D28D9",
    Healthcare: "#BE123C",
    Markets: "#7C3AED",
};

interface TopMoversProps {
    holdings: HoldingDTO[];
}

export const TopMovers: React.FC<TopMoversProps> = ({ holdings }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Hardcoded top movers
    const topMovers = [
        { id: 1, symbol: "NVDA", change: "+8.5%", sector: "Technology" },
        { id: 2, symbol: "TSLA", change: "+6.2%", sector: "Consumer/Tech" },
        { id: 3, symbol: "AMD", change: "+5.1%", sector: "Semiconductors" },
        { id: 4, symbol: "META", change: "-4.3%", sector: "Technology" },
        { id: 5, symbol: "INTEL", change: "-3.7%", sector: "Semiconductors" },
    ];

    // Calculate top % holdings (max 5) sorted by current value
    const topHoldings = useMemo(() => {
        if (!holdings || holdings.length === 0) return [];

        const validHoldings = holdings.filter(
            h => h && typeof h.currentValue === 'number' && !isNaN(h.currentValue) && h.currentValue >= 0
        );

        const totalValue = validHoldings.reduce((sum, h) => sum + h.currentValue, 0);

        return validHoldings
            .map(holding => ({
                stockCode: holding.stockSymbol,
                percentage: totalValue > 0 ? (holding.currentValue / totalValue) * 100 : 0,
                currentValue: holding.currentValue,
            }))
            .sort((a, b) => b.percentage - a.percentage)
            .slice(0, 5); // Top 5 maximum
    }, [holdings]);

    return (
        <View style={{ flexDirection: "row", justifyContent: "space-between", marginTop: 20 }}>
            {/* Top Movers */}
            <View style={{ flex: 1, alignItems: "center" }}>
                <Text style={[styles.header, { color: Colors.text, fontStyle: "italic" }]}>
                    Top Movers
                </Text>
                <Carousel
                    width={screenWidth / 2 - 30}
                    height={88}
                    data={topMovers}
                    loop
                    vertical
                    mode="parallax"
                    modeConfig={{ parallaxScrollingScale: 0.75, parallaxScrollingOffset: 30 }}
                    renderItem={({ item }) => (
                        <View style={[styles.card, { backgroundColor: Colors.card }]}>
                            <Text
                                style={{
                                    color: sectorColors[item.sector] || Colors.tint,
                                    fontWeight: "700",
                                }}
                            >
                                {item.symbol}
                            </Text>
                            <Text
                                style={{
                                    color: item.change.startsWith("+") ? "#2E7D32" : "#C62828",
                                    fontWeight: "800",
                                }}
                            >
                                {item.change}
                            </Text>
                        </View>
                    )}
                />
            </View>

            {/* Top % Holdings */}
            <View style={{ flex: 1, alignItems: "center" }}>
                <Text style={[styles.header, { color: Colors.text, fontStyle: "italic" }]}>
                    Top % Holdings
                </Text>
                {topHoldings.length > 0 ? (
                    <Carousel
                        width={screenWidth / 2 - 30}
                        height={88}
                        data={topHoldings}
                        loop={topHoldings.length > 1}
                        vertical
                        mode="parallax"
                        modeConfig={{
                            parallaxScrollingScale: 0.75,
                            parallaxScrollingOffset: 30,
                        }}
                        renderItem={({ item }) => (
                            <View style={[styles.card, { backgroundColor: Colors.card }]}>
                                <Text
                                    style={{
                                        color: Colors.tint,
                                        fontWeight: "700",
                                    }}
                                >
                                    {item.stockCode}
                                </Text>
                                <Text
                                    style={{
                                        color: Colors.text,
                                        fontWeight: "800",
                                    }}
                                >
                                    {item.percentage.toFixed(1)}%
                                </Text>
                            </View>
                        )}
                    />
                ) : (
                    <View style={[styles.card, { backgroundColor: Colors.card }]}>
                        <Text
                            style={{
                                color: Colors.text,
                                opacity: 0.6,
                                fontSize: 12,
                            }}
                        >
                            No holdings yet
                        </Text>
                    </View>
                )}
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