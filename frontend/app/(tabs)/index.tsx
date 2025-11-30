import React from "react";
import { View, Text, StyleSheet, Image, Dimensions, ScrollView } from "react-native";
import { useColorScheme } from "react-native";
import { getThemeColors } from "../../src/constants/colors";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { icons } from "../../src/constants/icons";
import { LineChart } from "react-native-chart-kit";
import Carousel from "react-native-reanimated-carousel";
import { useSharedValue } from "react-native-reanimated";
import { StockTicker } from "../../src/components/StockTicker";

const screenWidth = Dimensions.get("window").width - 48;

export default function HomeScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const progress = useSharedValue<number>(0);
    const holdingsProgress = useSharedValue<number>(0);
    const moversProgress = useSharedValue<number>(0);

    const priceData = [90, 100, 102, 101, 105, 107, 110, 108];

    const watchlistStocks = [
        { id: 1, symbol: "AAPL", price: "A$150.25", change: "+2.5%" },
        { id: 2, symbol: "MSFT", price: "A$380.50", change: "+1.8%" },
        { id: 3, symbol: "GOOGL", price: "A$140.75", change: "+3.2%" },
        { id: 4, symbol: "TSLA", price: "A$245.30", change: "-1.5%" },
        { id: 5, symbol: "AMZN", price: "A$170.90", change: "+2.1%" },
    ];

    const topHoldings = [
        { id: 1, symbol: "AAPL", percentage: "32.5%" },
        { id: 2, symbol: "MSFT", percentage: "18.3%" },
        { id: 3, symbol: "GOOGL", percentage: "15.7%" },
        { id: 4, symbol: "TSLA", percentage: "12.1%" },
        { id: 5, symbol: "AMZN", percentage: "8.2%" },
    ];

    const topMovers = [
        { id: 1, symbol: "NVDA", change: "+8.5%" },
        { id: 2, symbol: "TSLA", change: "+6.2%" },
        { id: 3, symbol: "AMD", change: "+5.1%" },
        { id: 4, symbol: "META", change: "-4.3%" },
        { id: 5, symbol: "INTEL", change: "-3.7%" },
    ];

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            <ScrollView showsVerticalScrollIndicator={false}>
                {/* Top bar */}
                <View style={styles.topBar}>
                    <View style={styles.centerGroup}>
                        <Image
                            source={icons.pegasus}
                            style={[styles.icon, { tintColor: Colors.tint }]}
                        />
                    </View>
                    <MaterialCommunityIcons
                        name="dots-vertical"
                        size={28}
                        color={Colors.tint}
                        style={styles.rightIcon}
                    />
                </View>

                {/* Dashboard */}
                <View style={styles.DashboardWrapper}>
                    <View style={[styles.dashboard, { borderColor: "white"}]}>

                        {/* TOP ROW — TEXT + TALL BOX */}
                        <View style={styles.dashboardTopRow}>

                            {/* TEXT BLOCK */}
                            <View style={styles.dashboardTextBlock}>
                                <Text style={[styles.dashboardtitle, { color: Colors.text }]}>
                                    Welcome to Pegasus!
                                </Text>
                                <Text style={[styles.subtitle, { color: Colors.text }]}>
                                    Cash and Holdings
                                </Text>
                                <Text style={{ flexDirection: "row" }}>
                                    <Text style={[styles.currency, { color: Colors.text }]}>A$</Text>
                                    <Text style={[styles.cashamount, { color: Colors.text }]}>
                                        1,000,000.00
                                    </Text>
                                </Text>
                                <Text style={[styles.dashboarddetails, { color: Colors.text }]}>
                                    Gain: A$27.68 (0.89%), Today
                                </Text>
                            </View>
                        </View>

                        {/* CHART FULL WIDTH */}
                        <View style={[styles.chartContainer, {"overflow": "hidden"}]}>
                            <LineChart
                                data={{
                                    labels: ["Jan", "Feb", "Mar"],
                                    datasets: [{ data: priceData, color: () => "#266EF1", strokeWidth: 2 }],
                                }}
                                width={screenWidth}
                                height={140}
                                withDots={false}
                                withInnerLines={false}
                                withOuterLines={false}
                                withVerticalLabels={false}
                                withHorizontalLabels={false}
                                withShadow={false}
                                chartConfig={{
                                    backgroundColor: "white",
                                    backgroundGradientFrom: "white",
                                    backgroundGradientTo: "white",
                                    color: () => "#266EF1",
                                }}
                                style={{ borderRadius: 12 }}
                            />
                        </View>
                    </View>
                </View>

                {/* Watchlist Highlights with Parallax Carousel */}
                <View style={styles.watchlisthighlightswrapper}>
                    <View
                        style={[
                            styles.watchlisthighlights,
                            { backgroundColor: Colors.card, borderColor: Colors.card },
                        ]}
                    >
                        <Text
                            style={[styles.watchlisthighlightstitle, { color: Colors.text }]}
                        >
                            Watchlist Highlights
                        </Text>
                        <Text
                            style={[
                                styles.watchlisthighlightssubtitle,
                                { color: Colors.text },
                            ]}
                        >
                            See your favourite stocks live.
                        </Text>

                        <View style={[styles.watchlisthighlightscarousel, { marginTop: 15, overflow: "hidden" }]}>
                            <Carousel
                                width={(screenWidth) / 1.2}
                                height={100}
                                data={watchlistStocks}
                                autoPlayInterval={3000}
                                loop={true}
                                pagingEnabled={true}
                                snapEnabled={true}
                                mode="parallax"
                                modeConfig={{
                                    parallaxScrollingScale: 0.85,
                                    parallaxScrollingOffset: 60,
                                }}
                                onProgressChange={progress}
                                renderItem={({ item }) => (
                                    <View
                                        style={[
                                            styles.stockCard,
                                            { backgroundColor: "white" },
                                        ]}
                                    >
                                        <View style={styles.stockCardContent}>
                                            <Image
                                                source={require('../../assets/images/apple.png')}
                                                style={{ width: 24, height: 24, marginBottom: 4 }}
                                                resizeMode="contain"
                                            />
                                            <Text style={styles.stockSymbol}>
                                                {item.symbol}
                                            </Text>
                                            <Text style={styles.stockPrice}>
                                                {item.price}
                                            </Text>
                                            <Text
                                                style={[
                                                    styles.stockChange,
                                                    {
                                                        color: item.change.startsWith("+")
                                                            ? "#2E7D32"
                                                            : "#C62828",
                                                    },
                                                ]}
                                            >
                                                {item.change}
                                            </Text>
                                        </View>
                                    </View>
                                )}
                            />
                        </View>
                    </View>
                </View>

                {/* Continuous Stock Ticker */}
                <StockTicker stocks={watchlistStocks} />

                <View style={styles.bottomSection}>
                    {/* TOP MOVERS - LEFT SIDE */}
                    <View style={styles.bottomSideContainer}>
                        <Text style={[styles.bottomHeader, { color: Colors.text }]}>
                            Top Movers
                        </Text>
                        <View
                            style={[
                                styles.verticalCarouselWrapper,
                                { backgroundColor: Colors.card, borderColor: Colors.card}
                            ]}
                        >
                            <Carousel
                                width={Dimensions.get("window").width / 2 - 30}
                                height={88}
                                data={topMovers}
                                autoPlayInterval={4000}
                                loop={true}
                                pagingEnabled={true}
                                snapEnabled={true}
                                vertical={true}
                                mode="parallax"
                                modeConfig={{
                                    parallaxScrollingScale: 0.75,
                                    parallaxScrollingOffset: 35,
                                }}
                                onProgressChange={moversProgress}
                                renderItem={({ item }) => (
                                    <View
                                        style={[
                                            styles.moverCard,
                                            { backgroundColor: "white", borderColor: "white" }
                                        ]}
                                    >
                                        <Text style={[styles.moverSymbol, { color: Colors.tint }]}>
                                            {item.symbol}
                                        </Text>
                                        <Text
                                            style={[
                                                styles.moverChange,
                                                {
                                                    color: item.change.startsWith("+") ? "#2E7D32" : "#C62828",
                                                }
                                            ]}
                                        >
                                            {item.change}
                                        </Text>
                                    </View>
                                )}
                            />
                        </View>
                    </View>

                    {/* TOP HOLDINGS - RIGHT SIDE */}
                    <View style={styles.bottomSideContainer}>
                        <Text style={[styles.bottomHeader, { color: Colors.text }]}>
                            Top % Holdings
                        </Text>
                        <View
                            style={[
                                styles.verticalCarouselWrapper,
                                { backgroundColor: Colors.card, borderColor: Colors.card }
                            ]}
                        >
                            <Carousel
                                width={Dimensions.get("window").width / 2 - 30}
                                height={88}
                                data={topHoldings}
                                autoPlayInterval={4000}
                                loop={true}
                                pagingEnabled={true}
                                snapEnabled={true}
                                vertical={true}
                                mode="parallax"
                                modeConfig={{
                                    parallaxScrollingScale: 0.75,
                                    parallaxScrollingOffset: 35,
                                }}
                                onProgressChange={holdingsProgress}
                                renderItem={({ item }) => (
                                    <View
                                        style={[
                                            styles.holdingCard,
                                            { backgroundColor: "white", borderColor: "white" }
                                        ]}
                                    >
                                        <Text style={[styles.holdingSymbol, { color: "#266EF1" }]}>
                                            {item.symbol}
                                        </Text>
                                        <Text style={[styles.holdingPercentage, { color: "black" }]}>
                                            {item.percentage}
                                        </Text>
                                    </View>
                                )}
                            />
                        </View>
                    </View>
                </View>
            </ScrollView>
        </View>
    );
}

// Keep all your existing styles...
const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
    },
    topBar: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        marginTop: -20,
        position: "relative",
    },
    centerGroup: {
        flexDirection: "row",
        alignItems: "center",
    },
    icon: {
        width: 125,
        height: 125,
        resizeMode: "contain",
    },
    title: {
        fontSize: 30,
        fontWeight: "800",
        marginLeft: -30,
        marginRight: 40,
    },
    rightIcon: {
        position: "absolute",
        right: 0,
        marginTop: 5,
    },
    DashboardWrapper: {
        marginTop: -35,
        justifyContent: "center",
        alignItems: "center",
    },
    subtitle: {
        fontSize: 12,
        marginTop: 5,
        opacity: 0.7,
    },
    dashboard: {
        width: "100%",
        padding: 20,
        borderRadius: 12,
        borderWidth: 1,
        backgroundColor: "white",
    },

    dashboardTopRow: {
        flexDirection: "row",
        width: "100%",
        alignItems: "stretch",   // important → makes box match the text column height
    },

    dashboardTextBlock: {
        flex: 1,  // text takes most space
        gap: 4,
    },
    dashboardtitle: {
        fontSize: 16,
        fontWeight: "bold",
        fontStyle: "italic",
    },
    cashamount: {
        fontSize: 24,
        marginTop: 10,
        fontWeight: "bold",
    },
    currency: {
        fontSize: 18,
        fontWeight: "bold",
        marginTop: 20,
    },
    dashboarddetails: {
        fontSize: 12,
        marginTop: 5,
        opacity: 0.8,
    },
    chartContainer: {
        marginTop: 20,
    },
    watchlisthighlightswrapper: {
        marginTop: 20,
        justifyContent: "center",
        alignItems: "center",
    },
    watchlisthighlights: {
        width: "100%",
        padding: 20,
        borderRadius: 12,
        borderWidth: 1,
        height: 200,
    },
    watchlisthighlightstitle: {
        fontSize: 16,
        fontWeight: "bold",
        fontStyle: "italic",
    },
    watchlisthighlightssubtitle: {
        fontSize: 12,
        marginTop: 5,
        opacity: 0.7,
    },
    watchlisthighlightscarousel: {
        justifyContent: "center",
        alignItems: "center",
    },
    stockCard: {
        borderRadius: 12,
        marginHorizontal: 5,
        justifyContent: "center",
        alignItems: "center",
        padding: 12,
        borderWidth: 2,
        borderColor: "white",
    },
    stockCardContent: {
        alignItems: "center",
    },
    stockSymbol: {
        color: "#266EF1",
        fontSize: 14,
        fontWeight: "bold",
    },
    stockPrice: {
        color: "#1A1A1A",
        fontSize: 16,
        fontWeight: "bold",
        marginTop: 3,
    },
    stockChange: {
        fontSize: 12,
        fontWeight: "600",
        marginTop: 3.1,
    },
    topholdingsContainer: {
        alignItems: "center",
        marginLeft: 20,
    },
    topholdingsHeader: {
        fontSize: 10,
        fontWeight: "700",
        marginBottom: 5,
    },
    topholdingsWrapper: {
        width: 90,
        borderWidth: 1,
        borderRadius: 12,
        height: 90,
    },
    bottomSection: {
        flexDirection: "row",
        width: "100%",
        marginTop: 20,
        gap: 12,
        justifyContent: "space-between",
    },
    bottomSideContainer: {
        flex: 1,
        alignItems: "center",
    },
    bottomHeader: {
        fontSize: 12,
        fontWeight: "700",
        marginBottom: 8,
    },
    verticalCarouselWrapper: {
        borderWidth: 1,
        borderRadius: 12,
        flex: 1,
        width: "100%",
    },
    moverCard: {
        borderRadius: 12,
        borderWidth: 2,
        padding: 8,
        justifyContent: "center",
        alignItems: "center",
        marginVertical: 3,
        height: 70,
    },
    moverSymbol: {
        fontSize: 14,
        fontWeight: "700",
    },
    moverChange: {
        fontSize: 18,
        fontWeight: "800",
        marginTop: 3,
    },
    holdingCard: {
        borderRadius: 12,
        borderWidth: 2,
        padding: 8,
        justifyContent: "center",
        alignItems: "center",
        marginVertical: 3,
        height: 70,
    },
    holdingSymbol: {
        fontSize: 14,
        fontWeight: "700",
    },
    holdingPercentage: {
        fontSize: 18,
        fontWeight: "800",
        marginTop: 3,
    },
});