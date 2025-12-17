import React, { useMemo } from "react";
import { View, Text, StyleSheet, useColorScheme, Dimensions } from "react-native";
import { getThemeColors } from "../../../src/constants/colors";
import { getSectorColor } from "@/src/services/sectorColorService";
import Carousel from "react-native-reanimated-carousel";
import type { HoldingDTO } from "@/src/types/api";

const screenWidth = Dimensions.get("window").width - 48;

interface TopMoversProps {
    holdings: HoldingDTO[];
}

export const TopMovers: React.FC<TopMoversProps> = ({ holdings }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Calculate top 5 biggest movers (by absolute % change, positive or negative)
    const topMovers = useMemo(() => {
        if (!holdings || holdings.length === 0) return [];

        const validHoldings = holdings.filter(
            h => h && typeof h.unrealizedGainPercent === 'number' && !isNaN(h.unrealizedGainPercent)
        );

        return validHoldings
            .map(holding => ({
                stockCode: holding.stockSymbol,
                changePercent: holding.unrealizedGainPercent,
                sector: holding.sector || 'Unknown',
            }))
            // Sort by absolute value of change (biggest movers first)
            .sort((a, b) => Math.abs(b.changePercent) - Math.abs(a.changePercent))
            .slice(0, 5); // Top 5 maximum
    }, [holdings]);

    // Calculate top 5 % holdings (max 5) sorted by current value
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
                sector: holding.sector || 'Unknown',
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
                {topMovers.length > 0 ? (
                    <Carousel
                        width={screenWidth / 2 - 30}
                        height={88}
                        data={topMovers}
                        loop={topMovers.length > 1}
                        vertical
                        mode="parallax"
                        modeConfig={{ parallaxScrollingScale: 0.75, parallaxScrollingOffset: 30 }}
                        renderItem={({ item }) => {
                            const sectorColor = getSectorColor(item.sector);
                            const isPositive = item.changePercent >= 0;

                            return (
                                <View style={[styles.card, { backgroundColor: Colors.card }]}>
                                    <Text
                                        style={{
                                            color: sectorColor.color,
                                            fontWeight: "700",
                                        }}
                                    >
                                        {item.stockCode}
                                    </Text>
                                    <Text
                                        style={{
                                            color: isPositive ? "#2E7D32" : "#C62828",
                                            fontWeight: "800",
                                        }}
                                    >
                                        {isPositive ? "+" : ""}{item.changePercent.toFixed(2)}%
                                    </Text>
                                </View>
                            );
                        }}
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
                        renderItem={({ item }) => {
                            const sectorColor = getSectorColor(item.sector);

                            return (
                                <View style={[styles.card, { backgroundColor: Colors.card }]}>
                                    <Text
                                        style={{
                                            color: sectorColor.color,
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
                            );
                        }}
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