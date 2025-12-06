import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    useColorScheme,
    ScrollView,
    TouchableOpacity,
    TextInput,
    Alert,
} from 'react-native';
import { getThemeColors } from '@/src/constants/colors';
import { HeaderSection } from '@/src/components/home/HeaderSection';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter, useLocalSearchParams } from 'expo-router';

const sectorColors: Record<string, { color: string; bgLight: string }> = {
    'Technology': { color: '#0369A1', bgLight: '#EFF6FF' },
    'Semiconductors': { color: '#B45309', bgLight: '#FEF3C7' },
    'FinTech': { color: '#15803D', bgLight: '#F0FDF4' },
    'Consumer/Tech': { color: '#6D28D9', bgLight: '#F5F3FF' },
    'Healthcare': { color: '#BE123C', bgLight: '#FFE4E6' },
    'Retail': { color: '#EA580C', bgLight: '#FFEDD5' },
    'Markets': { color: '#7C3AED', bgLight: '#F5F3FF' },
};

export default function SellTransactionPage() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const router = useRouter();
    const params = useLocalSearchParams<{ stock?: string; owned?: string }>();

    // Parse stock data if provided
    let stockData = null;
    if (params.stock) {
        try {
            stockData = JSON.parse(params.stock);
        } catch (error) {
            console.error('Error parsing stock data:', error);
        }
    }

    // Mock owned shares (in a real app, this would come from your portfolio state)
    const ownedShares = params.owned ? parseFloat(params.owned) : 100; // Default to 100 for demo

    const [shares, setShares] = useState('');
    const [priceType, setPriceType] = useState<'market' | 'limit'>('market');
    const [limitPrice, setLimitPrice] = useState('');

    const currentPrice = stockData?.price || 0;
    const shareCount = parseFloat(shares) || 0;
    const effectivePrice = priceType === 'limit' ? (parseFloat(limitPrice) || currentPrice) : currentPrice;
    const totalProceeds = shareCount * effectivePrice;
    const estimatedFees = totalProceeds * 0.001; // 0.1% fee
    const netAmount = totalProceeds - estimatedFees;

    const sectorColor = stockData ? (sectorColors[stockData.sector] || sectorColors['Technology']) : sectorColors['Technology'];

    const handleGoBack = () => {
        router.back();
    };

    const handleSell = () => {
        if (!stockData) {
            Alert.alert('Error', 'No stock selected');
            return;
        }

        if (!shares || shareCount <= 0) {
            Alert.alert('Invalid Input', 'Please enter a valid number of shares');
            return;
        }

        if (shareCount > ownedShares) {
            Alert.alert('Insufficient Shares', `You only own ${ownedShares} shares of ${stockData.symbol}`);
            return;
        }

        if (priceType === 'limit' && (!limitPrice || parseFloat(limitPrice) <= 0)) {
            Alert.alert('Invalid Input', 'Please enter a valid limit price');
            return;
        }

        Alert.alert(
            'Confirm Sale',
            `Sell ${shareCount} shares of ${stockData.symbol} at ${priceType === 'market' ? 'market price' : `A$${effectivePrice.toFixed(2)}`}?\n\nNet proceeds: A$${netAmount.toFixed(2)}`,
            [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Confirm',
                    onPress: () => {
                        Alert.alert('Success', `Successfully sold ${shareCount} shares of ${stockData.symbol}!`);
                        router.back();
                    },
                },
            ]
        );
    };

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                {/* Header with Back Button */}
                <View style={[styles.topBar, { backgroundColor: Colors.background }]}>
                    <TouchableOpacity
                        onPress={handleGoBack}
                        style={[styles.backButton, { backgroundColor: Colors.card }]}
                    >
                        <MaterialCommunityIcons
                            name="chevron-left"
                            size={28}
                            color={Colors.text}
                        />
                    </TouchableOpacity>
                    <View style={styles.headerSpacer}>
                        <HeaderSection />
                    </View>
                </View>
                <Text style={styles.headerTitle}>Buy Stock
                </Text>

                {stockData ? (
                    <>
                        {/* Stock Info Card */}
                        <View style={[styles.stockCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <View style={styles.stockHeader}>
                                <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                                    <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                                        {stockData.sector.charAt(0)}
                                    </Text>
                                </View>
                                <View style={styles.stockInfo}>
                                    <Text style={[styles.stockSymbol, { color: sectorColor.color }]}>
                                        {stockData.symbol}
                                    </Text>
                                    <Text style={[styles.stockName, { color: Colors.text, opacity: 0.7 }]}>
                                        {stockData.name}
                                    </Text>
                                </View>
                            </View>
                            <View style={styles.priceRow}>
                                <Text style={[styles.priceLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Current Price
                                </Text>
                                <Text style={[styles.currentPrice, { color: Colors.text }]}>
                                    A${currentPrice.toFixed(2)}
                                </Text>
                            </View>
                            <View style={[styles.ownedRow, { backgroundColor: '#FFF9E6', borderColor: '#FFD700' }]}>
                                <MaterialCommunityIcons name="wallet-outline" size={16} color="#B8860B" />
                                <Text style={[styles.ownedText, { color: '#B8860B' }]}>
                                    You own {ownedShares} shares
                                </Text>
                            </View>
                        </View>

                        {/* Order Type Selector */}
                        <View style={styles.section}>
                            <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                Order Type
                            </Text>
                            <View style={styles.orderTypeContainer}>
                                <TouchableOpacity
                                    onPress={() => setPriceType('market')}
                                    style={[
                                        styles.orderTypeButton,
                                        {
                                            backgroundColor: priceType === 'market' ? '#C62828' : Colors.card,
                                            borderColor: priceType === 'market' ? '#C62828' : Colors.border,
                                        },
                                    ]}
                                >
                                    <MaterialCommunityIcons
                                        name="flash"
                                        size={18}
                                        color={priceType === 'market' ? 'white' : Colors.text}
                                    />
                                    <Text
                                        style={[
                                            styles.orderTypeText,
                                            { color: priceType === 'market' ? 'white' : Colors.text },
                                        ]}
                                    >
                                        Market Order
                                    </Text>
                                </TouchableOpacity>
                                <TouchableOpacity
                                    onPress={() => setPriceType('limit')}
                                    style={[
                                        styles.orderTypeButton,
                                        {
                                            backgroundColor: priceType === 'limit' ? '#C62828' : Colors.card,
                                            borderColor: priceType === 'limit' ? '#C62828' : Colors.border,
                                        },
                                    ]}
                                >
                                    <MaterialCommunityIcons
                                        name="target"
                                        size={18}
                                        color={priceType === 'limit' ? 'white' : Colors.text}
                                    />
                                    <Text
                                        style={[
                                            styles.orderTypeText,
                                            { color: priceType === 'limit' ? 'white' : Colors.text },
                                        ]}
                                    >
                                        Limit Order
                                    </Text>
                                </TouchableOpacity>
                            </View>
                            <Text style={[styles.orderTypeHint, { color: Colors.text, opacity: 0.6 }]}>
                                {priceType === 'market'
                                    ? 'Execute immediately at current market price'
                                    : 'Execute only when price reaches your specified limit'}
                            </Text>
                        </View>

                        {/* Shares Input */}
                        <View style={styles.section}>
                            <View style={styles.sectionHeader}>
                                <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                    Number of Shares
                                </Text>
                                <TouchableOpacity
                                    onPress={() => setShares(ownedShares.toString())}
                                    style={styles.maxButton}
                                >
                                    <Text style={[styles.maxButtonText, { color: '#C62828' }]}>
                                        Sell All
                                    </Text>
                                </TouchableOpacity>
                            </View>
                            <View
                                style={[
                                    styles.inputContainer,
                                    { backgroundColor: Colors.card, borderColor: Colors.border },
                                ]}
                            >
                                <MaterialCommunityIcons
                                    name="chart-bar"
                                    size={20}
                                    color={Colors.text}
                                    style={{ opacity: 0.6 }}
                                />
                                <TextInput
                                    style={[styles.input, { color: Colors.text }]}
                                    placeholder="0"
                                    placeholderTextColor={Colors.text + '99'}
                                    value={shares}
                                    onChangeText={setShares}
                                    keyboardType="numeric"
                                />
                                <Text style={[styles.inputUnit, { color: Colors.text, opacity: 0.6 }]}>
                                    / {ownedShares}
                                </Text>
                            </View>
                            {shareCount > ownedShares && (
                                <Text style={[styles.errorText, { color: '#C62828' }]}>
                                    You only own {ownedShares} shares
                                </Text>
                            )}
                        </View>

                        {/* Limit Price Input */}
                        {priceType === 'limit' && (
                            <View style={styles.section}>
                                <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                                    Limit Price
                                </Text>
                                <View
                                    style={[
                                        styles.inputContainer,
                                        { backgroundColor: Colors.card, borderColor: Colors.border },
                                    ]}
                                >
                                    <MaterialCommunityIcons
                                        name="currency-usd"
                                        size={20}
                                        color={Colors.text}
                                        style={{ opacity: 0.6 }}
                                    />
                                    <TextInput
                                        style={[styles.input, { color: Colors.text }]}
                                        placeholder={currentPrice.toFixed(2)}
                                        placeholderTextColor={Colors.text + '99'}
                                        value={limitPrice}
                                        onChangeText={setLimitPrice}
                                        keyboardType="decimal-pad"
                                    />
                                    <Text style={[styles.inputUnit, { color: Colors.text, opacity: 0.6 }]}>
                                        AUD
                                    </Text>
                                </View>
                            </View>
                        )}

                        {/* Order Summary */}
                        <View style={[styles.summaryCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <Text style={[styles.summaryTitle, { color: Colors.text }]}>
                                Sale Summary
                            </Text>

                            <View style={styles.summaryRow}>
                                <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.7 }]}>
                                    Shares
                                </Text>
                                <Text style={[styles.summaryValue, { color: Colors.text }]}>
                                    {shareCount || 0}
                                </Text>
                            </View>

                            <View style={styles.summaryRow}>
                                <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.7 }]}>
                                    Price per share
                                </Text>
                                <Text style={[styles.summaryValue, { color: Colors.text }]}>
                                    A${effectivePrice.toFixed(2)}
                                </Text>
                            </View>

                            <View style={styles.summaryRow}>
                                <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.7 }]}>
                                    Gross proceeds
                                </Text>
                                <Text style={[styles.summaryValue, { color: Colors.text }]}>
                                    A${totalProceeds.toFixed(2)}
                                </Text>
                            </View>

                            <View style={styles.summaryRow}>
                                <Text style={[styles.summaryLabel, { color: Colors.text, opacity: 0.7 }]}>
                                    Est. Fees (0.1%)
                                </Text>
                                <Text style={[styles.summaryValue, { color: '#C62828' }]}>
                                    -A${estimatedFees.toFixed(2)}
                                </Text>
                            </View>

                            <View style={[styles.summaryDivider, { backgroundColor: Colors.border }]} />

                            <View style={styles.summaryRow}>
                                <Text style={[styles.summaryLabelTotal, { color: Colors.text }]}>
                                    Net Proceeds
                                </Text>
                                <Text style={[styles.summaryValueTotal, { color: '#2E7D32' }]}>
                                    +A${netAmount.toFixed(2)}
                                </Text>
                            </View>
                        </View>

                        {/* Sell Button */}
                        <TouchableOpacity
                            onPress={handleSell}
                            style={[
                                styles.sellButton,
                                { backgroundColor: shareCount > 0 && shareCount <= ownedShares ? '#C62828' : Colors.border },
                            ]}
                            disabled={shareCount <= 0 || shareCount > ownedShares}
                        >
                            <MaterialCommunityIcons
                                name="check-circle"
                                size={20}
                                color="white"
                            />
                            <Text style={styles.sellButtonText}>
                                Sell {shareCount > 0 ? `${shareCount} Shares` : 'Stock'}
                            </Text>
                        </TouchableOpacity>
                    </>
                ) : (
                    <View style={[styles.emptyState, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <MaterialCommunityIcons
                            name="alert-circle-outline"
                            size={48}
                            color={Colors.text}
                            style={{ opacity: 0.3 }}
                        />
                        <Text style={[styles.emptyText, { color: Colors.text, opacity: 0.6 }]}>
                            No stock selected
                        </Text>
                        <Text style={[styles.emptySubtext, { color: Colors.text, opacity: 0.5 }]}>
                            Please select a stock to sell
                        </Text>
                    </View>
                )}
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 20,
        gap: 12,
    },
    topBar: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 0,
        paddingTop: 12,
        paddingBottom: 0,
    },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
        marginTop: -10,
    },
    headerSpacer: {
        flex: 1,
        marginLeft: -32,
        marginTop: 0,
    },
    headerTitleContainer: {
        flex: 1,
    },
    headerTitle: {
        fontSize: 28, fontWeight: "800", fontStyle: "italic", marginLeft: 10,
        marginBottom: 15, marginTop: -20
    },
    stockCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        marginBottom: 24,
        gap: 12,
    },
    stockHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    sectorBadge: {
        width: 50,
        height: 50,
        borderRadius: 12,
        alignItems: 'center',
        justifyContent: 'center',
    },
    sectorBadgeText: {
        fontSize: 18,
        fontWeight: '800',
    },
    stockInfo: {
        flex: 1,
    },
    stockSymbol: {
        fontSize: 18,
        fontWeight: '800',
        marginBottom: 2,
    },
    stockName: {
        fontSize: 13,
        fontWeight: '500',
    },
    priceRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    priceLabel: {
        fontSize: 12,
        fontWeight: '600',
    },
    currentPrice: {
        fontSize: 20,
        fontWeight: '800',
    },
    ownedRow: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 8,
        borderRadius: 8,
        borderWidth: 1,
    },
    ownedText: {
        fontSize: 12,
        fontWeight: '700',
    },
    section: {
        marginBottom: 24,
    },
    sectionHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 12,
    },
    sectionTitle: {
        fontSize: 14,
        fontWeight: '700',
        marginBottom: 12,
    },
    maxButton: {
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 6,
        backgroundColor: '#FCE4E4',
    },
    maxButtonText: {
        fontSize: 12,
        fontWeight: '700',
    },
    orderTypeContainer: {
        flexDirection: 'row',
        gap: 12,
        marginBottom: 8,
    },
    orderTypeButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 14,
        borderRadius: 10,
        borderWidth: 1,
    },
    orderTypeText: {
        fontSize: 13,
        fontWeight: '700',
    },
    orderTypeHint: {
        fontSize: 11,
        fontWeight: '500',
        fontStyle: 'italic',
    },
    inputContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        borderWidth: 1,
        borderRadius: 10,
        paddingHorizontal: 12,
        gap: 10,
    },
    input: {
        flex: 1,
        fontSize: 16,
        fontWeight: '600',
        paddingVertical: 12,
    },
    inputUnit: {
        fontSize: 12,
        fontWeight: '600',
    },
    errorText: {
        fontSize: 11,
        fontWeight: '600',
        marginTop: 4,
    },
    summaryCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 16,
        marginBottom: 24,
        gap: 12,
    },
    summaryTitle: {
        fontSize: 16,
        fontWeight: '700',
        marginBottom: 8,
    },
    summaryRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    summaryLabel: {
        fontSize: 13,
        fontWeight: '600',
    },
    summaryValue: {
        fontSize: 13,
        fontWeight: '700',
    },
    summaryDivider: {
        height: 1,
        marginVertical: 8,
    },
    summaryLabelTotal: {
        fontSize: 15,
        fontWeight: '800',
    },
    summaryValueTotal: {
        fontSize: 18,
        fontWeight: '800',
    },
    sellButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 16,
        borderRadius: 12,
        marginBottom: 24,
    },
    sellButtonText: {
        fontSize: 16,
        fontWeight: '800',
        color: 'white',
    },
    emptyState: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 60,
        alignItems: 'center',
        gap: 12,
    },
    emptyText: {
        fontSize: 16,
        fontWeight: '700',
    },
    emptySubtext: {
        fontSize: 13,
        fontWeight: '500',
    },
});
