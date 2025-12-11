import React, { useRef, useEffect, useState } from "react";
import {
    View,
    Text,
    StyleSheet,
    useColorScheme,
    Dimensions,
    Animated,
    TouchableOpacity,
} from "react-native";
import { Svg, Polyline, Circle, Defs, LinearGradient, Stop } from "react-native-svg";
import { getThemeColors } from "../../../src/constants/colors";
import { getAccountOverview } from "@/src/services/portfolioService";
import { useAuth } from "@/src/context/AuthContext";

const screenWidth = Dimensions.get("window").width - 48;
const chartHeight = 140;
const chartPadding = 20;

// Example data for different periods
const chartDataSets: Record<string, number[]> = {
    "1W": [85, 88, 92, 90, 95, 97, 100, 98],
    "1M": [80, 85, 90, 92, 95, 97, 100, 102],
    "3M": [70, 75, 80, 85, 90, 95, 100, 105],
    "6M": [60, 65, 70, 75, 80, 85, 90, 95],
    "1Y": [50, 60, 65, 70, 75, 80, 85, 90],
};

const filterOptions = ["1W", "1M", "3M", "6M", "1Y"];

export const Dashboard = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const { activeAccount } = useAuth();

    const chartWidth = screenWidth - chartPadding * 2;

    const initialFilter = filterOptions[0];
    const [selectedFilter, setSelectedFilter] = useState(initialFilter);
    const [priceData, setPriceData] = useState(chartDataSets[initialFilter]);

    const [totalPortfolioValue, setTotalPortfolioValue] = useState<number | null>(null);
    const [cashBalance, setCashBalance] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [absoluteGain, setAbsoluteGain] = useState<number | null>(null);
    const [percentageGain, setPercentageGain] = useState<number | null>(null);


    useEffect(() => {
        if (activeAccount?.accountId) {
            fetchAccountOverview();
        }
    }, [activeAccount?.accountId]);

    async function fetchAccountOverview() {
        if (!activeAccount?.accountId) return;

        try {
            const data = await getAccountOverview(activeAccount.accountId);

            setTotalPortfolioValue(Number(data.totalPortfolioValue));
            setCashBalance(Number(data.cashBalance));

            // ✅ Calculate absolute and percentage gains
            const absGain = Number(data.totalUnrealizedGain);
            const percGain = data.totalCostBasis > 0
                ? (Number(data.totalUnrealizedGain) / Number(data.totalCostBasis)) * 100
                : 0;

            setAbsoluteGain(absGain);
            setPercentageGain(percGain);

        } catch (error) {
            console.error("Error fetching portfolio overview:", error);
        } finally {
            setIsLoading(false);
        }
    }



    const animation = useRef(new Animated.Value(0)).current;
    const [animatedPoints, setAnimatedPoints] = useState<string>("");
    const [progressIndex, setProgressIndex] = useState(0);

    const updateData = (filter: string) => {
        setSelectedFilter(filter);
        setPriceData(chartDataSets[filter]);
        animation.setValue(0);
    };

    const usableHeight = chartHeight - chartPadding * 2;
    const minPrice = Math.min(...priceData);
    const maxPrice = Math.max(...priceData);
    const priceRange = maxPrice - minPrice || 1;

    const fullPoints = priceData.map((price, index) => {
        const x = chartPadding + (index / (priceData.length - 1)) * chartWidth;
        const y = chartHeight - chartPadding - ((price - minPrice) / priceRange) * usableHeight;
        return { x, y };
    });

    const generatePointsString = (progress: number) => {
        const count = Math.floor(progress * fullPoints.length);
        if (count === 0) return "";
        return fullPoints
            .slice(0, count)
            .map((p) => `${p.x},${p.y}`)
            .join(" ");
    };

    useEffect(() => {
        Animated.timing(animation, {
            toValue: 1,
            duration: 750,
            useNativeDriver: false,
        }).start();

        return () => animation.removeAllListeners();
    }, [priceData]);

    return (
        <View style={styles.wrapper}>
            <View style={[styles.dashboard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                <View style={styles.dashboardTextBlock}>
                    <Text style={[styles.dashboardtitle, { color: Colors.text }]}>
                        Welcome to Pegasus!
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text }]}>
                        Cash and Holdings
                    </Text>
                    <View style={styles.currencyRow}>
                        <Text style={[styles.currency, { color: Colors.text }]}>A$</Text>
                        <View style={styles.currencyRow}>
                            <Text style={[styles.currency, { color: Colors.text }]}>A$</Text>
                            <Text style={[styles.cashamount, { color: Colors.text }]}>
                                {isLoading || totalPortfolioValue === null
                                    ? "Loading..."
                                    : totalPortfolioValue.toLocaleString("en-AU", {
                                        minimumFractionDigits: 2,
                                        maximumFractionDigits: 2,
                                    })
                                }
                            </Text>
                        </View>
                    </View>
                    <Text style={[styles.dashboarddetails, { color: Colors.text }]}>
                        {isLoading || absoluteGain === null || percentageGain === null
                            ? "Loading..."
                            : `Gain: A$${absoluteGain.toLocaleString("en-AU", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} (${percentageGain.toFixed(2)}%), Today`
                        }
                    </Text>

                </View>

                {/* SVG Line Chart */}
                <View style={styles.chartContainer}>
                    <Svg width={screenWidth} height={chartHeight}>
                        <Defs>
                            <LinearGradient id="gradient" x1="0" y1="0" x2="0" y2="1">
                                <Stop offset="0" stopColor={Colors.tint} stopOpacity="0.4" />
                                <Stop offset="1" stopColor={Colors.tint} stopOpacity="0" />
                            </LinearGradient>
                        </Defs>

                        {/* Gradient area under the line */}
                        {animatedPoints ? (
                            <Polyline
                                points={
                                    animatedPoints +
                                    ` ${chartWidth + chartPadding},${chartHeight - chartPadding} ${chartPadding},${chartHeight - chartPadding}`
                                }
                                fill="url(#gradient)"
                                stroke="none"
                            />
                        ) : null}

                        {/* Line on top */}
                        <Polyline
                            points={animatedPoints}
                            fill="none"
                            stroke={Colors.tint}
                            strokeWidth={3}
                            strokeLinecap="round"
                            strokeLinejoin="round"
                        />

                        {/* Data points */}
                        {fullPoints.map((p, index) => {
                            if (index >= progressIndex) return null;
                            return <Circle key={index} cx={p.x} cy={p.y} r={4} fill={Colors.tint} opacity={0.6} />;
                        })}
                    </Svg>

                    {/* Filter buttons */}
                    <View style={styles.filterContainer}>
                        {filterOptions.map((filter) => (
                            <TouchableOpacity
                                key={filter}
                                onPress={() => updateData(filter)}
                                style={[
                                    styles.filterButton,
                                    selectedFilter === filter && { backgroundColor: Colors.tint ,},
                                ]}
                            >
                                <Text
                                    style={[
                                        styles.filterText,
                                        selectedFilter === filter && { color: "white", fontWeight: "700" },
                                        selectedFilter !== filter && { color: "grey" },
                                    ]}
                                >
                                    {filter}
                                </Text>
                            </TouchableOpacity>
                        ))}
                    </View>
                </View>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    wrapper: {
        marginTop: -20,
        justifyContent: "center",
        alignItems: "center",
    },
    dashboard: {
        width: "100%",
        padding: 16,
        borderRadius: 12,
        borderWidth: 1,
    },
    dashboardTextBlock: {
        gap: 6,
        marginBottom: 16,
    },
    dashboardtitle: {
        fontSize: 24,
        fontWeight: "800",
        fontStyle: "italic",
        marginBottom: 4,
    },
    subtitle: {
        fontSize: 12,
        opacity: 0.7,
    },
    currencyRow: {
        flexDirection: "row",
        alignItems: "baseline",
        marginTop: 8,
    },
    currency: {
        fontSize: 16,
        fontWeight: "bold",
    },
    cashamount: {
        fontSize: 28,
        fontWeight: "bold",
        marginLeft: 4,
    },
    dashboarddetails: {
        fontSize: 12,
        marginTop: 8,
        opacity: 0.8,
    },
    chartContainer: {
        marginTop: 12,
        borderRadius: 8,
        marginLeft: -16,
        backgroundColor: "transparent",
    },
    filterContainer: {
        flexDirection: "row",
        justifyContent: "center",   // ensures they stay centered
        marginTop: 12,
        gap: 10,                     // reduced from 30 → closer together
    },
    filterButton: {
        paddingVertical: 6,
        paddingHorizontal: 10,
        borderRadius: 6,
    },
    filterText: {
        fontSize: 12,
        fontWeight: "600",
    },

});
