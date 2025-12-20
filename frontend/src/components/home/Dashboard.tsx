import React, { useRef, useEffect, useState } from "react";
import {
    View,
    Text,
    StyleSheet,
    useColorScheme,
    Dimensions,
    Animated,
    TouchableOpacity,
    GestureResponderEvent,
} from "react-native";
import { Svg, Polyline, Circle, Defs, LinearGradient, RadialGradient, Stop, Line } from "react-native-svg";
import { getThemeColors } from "../../../src/constants/colors";
import { getAccountOverview, getPortfolioChartData } from "@/src/services/portfolioService";
import { useAuth } from "@/src/context/AuthContext";

const screenWidth = Dimensions.get("window").width - 48;
const chartHeight = 140;
const chartPadding = 20;

const filterOptions = ["1W", "1M", "3M", "6M", "1Y", "ALL"];

export const Dashboard = () => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const { activeAccount } = useAuth();

    const chartWidth = screenWidth - chartPadding * 2;

    const initialFilter = filterOptions[5]; // Default to "ALL"
    const [selectedFilter, setSelectedFilter] = useState(initialFilter);
    const [chartData, setChartData] = useState<Array<{ date: string; value: number; isLive: boolean }>>([]);
    const [priceData, setPriceData] = useState<number[]>([]);

    const [totalPortfolioValue, setTotalPortfolioValue] = useState<number | null>(null);
    const [cashBalance, setCashBalance] = useState<number | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [absoluteGain, setAbsoluteGain] = useState<number | null>(null);
    const [percentageGain, setPercentageGain] = useState<number | null>(null);
    const [selectedPointIndex, setSelectedPointIndex] = useState<number | null>(null);
    const [isDragging, setIsDragging] = useState(false);
    const { user } = useAuth();

    const chartContainerRef = useRef<View>(null);

    useEffect(() => {
        if (activeAccount?.accountId) {
            fetchAccountOverview();
            fetchChartData();
        }
    }, [activeAccount?.accountId]);

    // Refetch chart data when filter changes
    useEffect(() => {
        if (activeAccount?.accountId) {
            fetchChartData();
        }
    }, [selectedFilter]);

    // Auto-refresh chart data every 30 seconds for live updates
    useEffect(() => {
        if (!activeAccount?.accountId) return;

        const interval = setInterval(() => {
            fetchChartData();
        }, 30000); // 30 seconds

        return () => clearInterval(interval);
    }, [activeAccount?.accountId, selectedFilter]);

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

    async function fetchChartData() {
        if (!activeAccount?.accountId) return;

        try {
            const data = await getPortfolioChartData(activeAccount.accountId);

            if (data.length === 0) {
                // No data available, use fallback
                setPriceData([]);
                setChartData([]);
                return;
            }

            // Filter data based on selected time period
            const filteredData = filterDataByPeriod(data, selectedFilter);
            setChartData(filteredData);

            // Extract values for chart rendering
            setPriceData(filteredData.map(point => point.value));

        } catch (error) {
            console.error("Error fetching chart data:", error);
            // Use empty data on error
            setPriceData([]);
            setChartData([]);
        }
    }

    function filterDataByPeriod(
        data: Array<{ date: string; value: number; isLive: boolean }>,
        period: string
    ): Array<{ date: string; value: number; isLive: boolean }> {
        if (period === "ALL" || data.length === 0) {
            return data;
        }

        const now = new Date();
        let cutoffDate = new Date();

        switch (period) {
            case "1W":
                cutoffDate.setDate(now.getDate() - 7);
                break;
            case "1M":
                cutoffDate.setMonth(now.getMonth() - 1);
                break;
            case "3M":
                cutoffDate.setMonth(now.getMonth() - 3);
                break;
            case "6M":
                cutoffDate.setMonth(now.getMonth() - 6);
                break;
            case "1Y":
                cutoffDate.setFullYear(now.getFullYear() - 1);
                break;
            default:
                return data;
        }

        return data.filter(point => new Date(point.date) >= cutoffDate);
    }

    const animation = useRef(new Animated.Value(0)).current;
    const [animatedPoints, setAnimatedPoints] = useState<string>("");
    const [progressIndex, setProgressIndex] = useState(0);

    const updateData = async (filter: string) => {
        setSelectedFilter(filter);
        setSelectedPointIndex(null); // Clear selection when changing filters
        animation.setValue(0);
    };

    const usableHeight = chartHeight - chartPadding * 2;
    const minPrice = priceData.length > 0 ? Math.min(...priceData) : 0;
    const maxPrice = priceData.length > 0 ? Math.max(...priceData) : 100;
    const priceRange = maxPrice - minPrice || 1;

    const fullPoints = priceData.length > 0 ? priceData.map((price, index) => {
        const x = chartPadding + (index / (priceData.length - 1)) * chartWidth;
        const y = chartHeight - chartPadding - ((price - minPrice) / priceRange) * usableHeight;
        return { x, y };
    }) : [];

    useEffect(() => {
        if (priceData.length === 0) return;

        const listenerId = animation.addListener(({ value }) => {
            const pointsCount = Math.floor(value * fullPoints.length);
            const pointsString = fullPoints
                .slice(0, pointsCount)
                .map((p) => `${p.x},${p.y}`)
                .join(" ");
            setAnimatedPoints(pointsString);
            setProgressIndex(pointsCount);
        });

        Animated.timing(animation, {
            toValue: 1,
            duration: 750,
            useNativeDriver: false,
        }).start();

        return () => {
            animation.removeListener(listenerId);
        };
    }, [priceData]);

    // ========================================================================
    // Chart Touch/Pan Handlers (NEW)
    // ========================================================================

    const handleChartPress = (event: GestureResponderEvent) => {
        const { locationX } = event.nativeEvent;
        const touchX = locationX;

        // Find closest point to touch
        const closestIndex = fullPoints.reduce((closest, point, index) => {
            const distance = Math.abs(point.x - touchX);
            const closestDistance = Math.abs(fullPoints[closest].x - touchX);
            return distance < closestDistance ? index : closest;
        }, 0);

        setSelectedPointIndex(closestIndex);
        setIsDragging(true);
    };

    const handleChartMove = (event: GestureResponderEvent) => {
        if (!isDragging) return;

        const { locationX } = event.nativeEvent;
        const touchX = locationX;

        // Clamp to chart boundaries
        const clampedX = Math.max(chartPadding, Math.min(touchX, chartWidth + chartPadding));

        // Find closest point to touch
        const closestIndex = fullPoints.reduce((closest, point, index) => {
            const distance = Math.abs(point.x - clampedX);
            const closestDistance = Math.abs(fullPoints[closest].x - clampedX);
            return distance < closestDistance ? index : closest;
        }, 0);

        setSelectedPointIndex(closestIndex);
    };

    const handleChartRelease = () => {
        setIsDragging(false);
    };

    // Get selected point for vertical line
    const selectedPoint = selectedPointIndex !== null ? fullPoints[selectedPointIndex] : null;

    return (
        <View style={styles.wrapper}>
            <View style={[styles.dashboard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                {/* Dashboard Text Block - UNCHANGED */}
                <View style={styles.dashboardTextBlock}>
                    <Text style={[styles.dashboardtitle, { color: Colors.text }]}>
                        {user ? `Welcome back, ${user.fullName}` : "Welcome to Pegasus!"}
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text }]}>
                        Cash and Holdings
                    </Text>
                    <View style={styles.currencyRow}>
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

                {/* SVG Line Chart - ENHANCED WITH PANNING & GRADIENT POINTS */}
                <View
                    ref={chartContainerRef}
                    style={styles.chartContainer}
                    onStartShouldSetResponder={() => true}
                    onMoveShouldSetResponder={() => isDragging}
                    onResponderGrant={handleChartPress}
                    onResponderMove={handleChartMove}
                    onResponderRelease={handleChartRelease}
                >
                    {priceData.length === 0 ? (
                        <View style={[styles.emptyChartContainer, { height: chartHeight }]}>
                            <Text style={[styles.emptyChartText, { color: Colors.text }]}>
                                No chart data available. Snapshots will appear here once generated.
                            </Text>
                        </View>
                    ) : (
                        <View>
                            <Svg width={screenWidth} height={chartHeight}>
                                <Defs>
                                    <LinearGradient id="gradient" x1="0" y1="0" x2="0" y2="1">
                                        <Stop offset="0" stopColor={Colors.tint} stopOpacity="0.4" />
                                        <Stop offset="1" stopColor={Colors.tint} stopOpacity="0" />
                                    </LinearGradient>

                                    {/* Radial gradient for selected point circle */}
                                    <RadialGradient id="pointGradient" cx="50%" cy="50%" r="50%">
                                        <Stop offset="0%" stopColor={Colors.tint} stopOpacity="1" />
                                        <Stop offset="70%" stopColor={Colors.tint} stopOpacity="0.6" />
                                        <Stop offset="100%" stopColor={Colors.tint} stopOpacity="0" />
                                    </RadialGradient>
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

                                {/* Vertical indicator line when dragging */}
                                {selectedPoint && (
                                    <Line
                                        x1={selectedPoint.x}
                                        y1={chartPadding}
                                        x2={selectedPoint.x}
                                        y2={chartHeight - chartPadding}
                                        stroke={Colors.tint}
                                        strokeWidth={2}
                                        strokeDasharray="4,4"
                                        opacity={isDragging ? 0.8 : 0.4}
                                    />
                                )}

                                {/* Selected point - only show when pressed */}
                                {selectedPoint && selectedPointIndex !== null && (
                                    <>
                                        {/* Outer gradient halo effect */}
                                        <Circle
                                            cx={selectedPoint.x}
                                            cy={selectedPoint.y}
                                            r={10}
                                            fill="url(#pointGradient)"
                                            opacity={0.8}
                                        />

                                        {/* Inner solid core */}
                                        <Circle
                                            cx={selectedPoint.x}
                                            cy={selectedPoint.y}
                                            r={6}
                                            fill={Colors.tint}
                                            opacity={1}
                                        />
                                    </>
                                )}
                            </Svg>
                        </View>
                    )}

                    {/* Tooltip showing selected point data */}
                    {selectedPointIndex !== null && chartData[selectedPointIndex] && (
                        <View style={[styles.tooltipContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <Text style={[styles.tooltipLabel, { color: Colors.text }]}>
                                {new Date(chartData[selectedPointIndex].date).toLocaleDateString("en-AU")}
                            </Text>
                            <Text style={[styles.tooltipValue, { color: Colors.tint }]}>
                                A${chartData[selectedPointIndex].value.toLocaleString("en-AU", {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                            })}
                            </Text>
                        </View>
                    )}

                    {/* Filter buttons */}
                    <View style={styles.filterContainer}>
                        {filterOptions.map((filter) => (
                            <TouchableOpacity
                                key={filter}
                                onPress={() => updateData(filter)}
                                style={[
                                    styles.filterButton,
                                    selectedFilter === filter && { backgroundColor: Colors.tint },
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
        justifyContent: "center",
        marginTop: 12,
        gap: 10,
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
    emptyChartContainer: {
        justifyContent: "center",
        alignItems: "center",
        paddingHorizontal: 20,
    },
    emptyChartText: {
        fontSize: 14,
        textAlign: "center",
        opacity: 0.7,
    },
    legendContainer: {
        flexDirection: "row",
        justifyContent: "center",
        gap: 16,
        marginTop: 12,
        paddingHorizontal: 16,
    },
    legendItem: {
        flexDirection: "row",
        alignItems: "center",
        gap: 6,
    },
    legendDot: {
        width: 8,
        height: 8,
        borderRadius: 4,
        opacity: 0.6,
    },
    liveDot: {
        width: 10,
        height: 10,
        borderRadius: 5,
        backgroundColor: "#10b981",
        borderWidth: 2,
        borderColor: "#059669",
        opacity: 1,
    },
    legendText: {
        fontSize: 10,
        opacity: 0.8,
    },
    tooltipContainer: {
        marginTop: 12,
        marginHorizontal: 16,
        padding: 12,
        borderRadius: 8,
        borderWidth: 1,
        alignItems: "center",
        gap: 4,
    },
    tooltipLabel: {
        fontSize: 12,
        opacity: 0.7,
        fontWeight: "500",
    },
    tooltipValue: {
        fontSize: 18,
        fontWeight: "bold",
    },
});