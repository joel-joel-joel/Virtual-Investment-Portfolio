import React from 'react';
import { View, Text, StyleSheet, Animated, Dimensions, useColorScheme } from 'react-native';
import { getThemeColors } from '../../constants/colors';

interface TickerStock {
    symbol: string;
    price: string;
    change: string;
    sector: string; // make sure sector is included
}

interface StockTickerProps {
    stocks: TickerStock[];
}

const SCREEN_WIDTH = Dimensions.get('window').width;

const sectorColors: { [key: string]: string } = {
    Technology: "#0369A1",
    Semiconductors: "#EF6C00",
    FinTech: "#15803D",
    "Consumer/Tech": "#6D28D9",
    Healthcare: "#BE123C",
    Markets: "#7C3AED",
};

export const StockTicker: React.FC<StockTickerProps> = ({ stocks }) => {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Duplicate stocks for infinite loop
    const duplicatedStocks = [...stocks, ...stocks, ...stocks];

    const scrollX = React.useRef(new Animated.Value(0)).current;

    React.useEffect(() => {
        const scrollAnimation = Animated.loop(
            Animated.timing(scrollX, {
                toValue: -SCREEN_WIDTH * 3,
                duration: 20000, // 20 seconds for a full scroll
                useNativeDriver: true,
            })
        );

        scrollAnimation.start();

        return () => scrollAnimation.stop();
    }, []);

    return (
        <View style={[styles.tickerWrapper, { backgroundColor: "white", borderColor: Colors.border }]}>
            <View style={styles.tickerContainer}>
                <Animated.View
                    style={[
                        styles.tickerContent,
                        {
                            transform: [{ translateX: scrollX }],
                        },
                    ]}
                >
                    {duplicatedStocks.map((stock, index) => {
                        const symbolColor = sectorColors[stock.sector] || Colors.tint;

                        return (
                            <View key={index} style={styles.tickerItem}>
                                <Text style={[styles.tickerSymbol, { color: symbolColor }]}>
                                    {stock.symbol}
                                </Text>
                                <Text style={[styles.tickerPrice, { color: Colors.text }]}>
                                    {stock.price}
                                </Text>
                                <Text
                                    style={[
                                        styles.tickerChange,
                                        {
                                            color: stock.change.startsWith('+') ? '#2E7D32' : '#C62828',
                                        },
                                    ]}
                                >
                                    {stock.change}
                                </Text>
                                <View style={[styles.separator, { backgroundColor: Colors.border }]} />
                            </View>
                        );
                    })}
                </Animated.View>
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    tickerWrapper: {
        width: '100%',
        height: 30,
        borderRadius: 12,
        borderWidth: 1,
        marginTop: 20,
        overflow: 'hidden',
    },
    tickerContainer: {
        flex: 1,
        overflow: 'hidden',
    },
    tickerContent: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 8,
    },
    tickerItem: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 16,
        gap: 8,
    },
    tickerSymbol: {
        fontSize: 13,
        fontWeight: '700',
        minWidth: 45,
    },
    tickerPrice: {
        fontSize: 12,
        fontWeight: '600',
        minWidth: 70,
    },
    tickerChange: {
        fontSize: 11,
        fontWeight: '600',
        minWidth: 50,
    },
    separator: {
        width: 1,
        height: 30,
        marginLeft: 16,
    },
});
