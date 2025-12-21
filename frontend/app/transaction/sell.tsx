import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    useColorScheme,
    ScrollView,
    TouchableOpacity,
    TextInput,
    Alert,
    ActivityIndicator,
} from 'react-native';
import { useTheme } from '@/src/context/ThemeContext';
import { HeaderSection } from '@/src/components/home/HeaderSection';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation, useRoute } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RouteProp } from '@react-navigation/native';
import type { RootStackParamList } from '@/src/navigation';
import { createTransaction, getAccountHoldings } from '@/src/services/portfolioService';
import { createOrder } from '@/src/services/orderService';
import { getOrCreateStockBySymbol } from '@/src/services/entityService';
import { useAuth } from '@/src/context/AuthContext';
import type { FinnhubCompanyProfileDTO, FinnhubQuoteDTO, CreateTransactionRequest, CreateOrderRequest, TransactionType, OrderType, HoldingDTO } from '@/src/types/api';
import { getSectorColor } from '@/src/services/sectorColorService';


export default function SellTransactionPage() {
    const {Colors} = useTheme();
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const route = useRoute<RouteProp<RootStackParamList, 'SellTransaction'>>();
    const { activeAccount } = useAuth();

    // Get stock data from route params (already an object, no parsing needed)
    const stockData = route.params?.stock || null;

    const [shares, setShares] = useState('');
    const [priceType, setPriceType] = useState<'market' | 'limit'>('market');
    const [limitPrice, setLimitPrice] = useState('');
    const [loading, setLoading] = useState(false);
    const [realtimePrice, setRealtimePrice] = useState<number | null>(null);
    const [loadingPrice, setLoadingPrice] = useState(false);
    const [realtimeStockData, setRealtimeStockData] = useState<any | null>(null);

    // Holding state - fetched from database
    const [holding, setHolding] = useState<HoldingDTO | null>(null);
    const [loadingHolding, setLoadingHolding] = useState(false);
    const [holdingError, setHoldingError] = useState<string | null>(null);

    // Fetch holding from database
    useEffect(() => {
        const fetchHolding = async () => {
            if (!stockData?.symbol || !activeAccount) {
                return;
            }

            setLoadingHolding(true);
            setHoldingError(null);
            try {
                const holdings = await getAccountHoldings(activeAccount.accountId);

                // Find holding for this specific stock
                const stockHolding = holdings.find(
                    (h) => h.stockSymbol.toUpperCase() === stockData.symbol.toUpperCase()
                );

                if (stockHolding) {
                    setHolding(stockHolding);
                } else {
                    setHolding(null);
                    setHoldingError(`You don't own any shares of ${stockData.symbol}`);
                }
            } catch (error) {
                console.error('Error fetching holding:', error);
                setHoldingError('Failed to load your holdings');
                setHolding(null);
            } finally {
                setLoadingHolding(false);
            }
        };

        fetchHolding();
    }, [stockData?.symbol, activeAccount?.accountId]);

    // Fetch real-time price data
    useEffect(() => {
        const fetchRealtimePrice = async () => {
            if (!stockData?.symbol) return;

            setLoadingPrice(true);
            try {
                const apiUrl = process.env.EXPO_PUBLIC_API_BASE_URL || 'http://localhost:8080';

                const [profileResponse, quoteResponse] = await Promise.all([
                    fetch(`${apiUrl}/api/stocks/finnhub/profile/${stockData.symbol}`),
                    fetch(`${apiUrl}/api/stocks/finnhub/quote/${stockData.symbol}`)
                ]);

                if (!profileResponse.ok || !quoteResponse.ok) {
                    console.warn(`Failed to fetch real-time data for ${stockData.symbol}`);
                    setLoadingPrice(false);
                    return;
                }

                const profile: FinnhubCompanyProfileDTO = await profileResponse.json();
                const quote: FinnhubQuoteDTO = await quoteResponse.json();

                if (!quote || !quote.c || !quote.pc) {
                    console.warn(`Invalid quote data for ${stockData.symbol}`);
                    setLoadingPrice(false);
                    return;
                }

                const currentPrice = quote.c || 0;
                const previousClose = quote.pc || currentPrice;
                const change = currentPrice - previousClose;
                const changePercent = previousClose !== 0 ? (change / previousClose) * 100 : 0;

                setRealtimePrice(currentPrice);
                setRealtimeStockData({
                    ...stockData,
                    price: currentPrice,
                    change: change,
                    changePercent: changePercent,
                    dayHigh: quote?.h || stockData?.dayHigh || 0,
                    dayLow: quote?.l || stockData?.dayLow || 0,
                });
            } catch (error) {
                console.error(`Error fetching real-time price for ${stockData.symbol}:`, error);
            } finally {
                setLoadingPrice(false);
            }
        };

        fetchRealtimePrice();

        // Refresh price every 30 seconds
        const interval = setInterval(fetchRealtimePrice, 30000);

        return () => clearInterval(interval);
    }, [stockData?.symbol]);

    const displayStockData = realtimeStockData || stockData;
    const currentPrice = realtimePrice !== null ? realtimePrice : (stockData?.price || 0);
    const ownedShares = holding?.quantity || 0;
    const shareCount = parseFloat(shares) || 0;
    const effectivePrice = priceType === 'limit' ? (parseFloat(limitPrice) || currentPrice) : currentPrice;
    const totalProceeds = shareCount * effectivePrice;
    const estimatedFees = totalProceeds * 0.001; // 0.1% fee
    const netAmount = totalProceeds - estimatedFees;

    const sectorColor = displayStockData ? getSectorColor(displayStockData.sector) : { bgLight: '#FFF', color: '#000' };

    const handleGoBack = () => {
        navigation.goBack();
    };

    const handleSell = async () => {
        if (!stockData) {
            Alert.alert('Error', 'No stock selected');
            return;
        }

        if (!activeAccount) {
            Alert.alert('Error', 'No account selected. Please go to Profile and select an account.');
            return;
        }

        if (!holding) {
            Alert.alert('Error', `You don't own any shares of ${stockData.symbol}`);
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

        if (priceType === 'limit' && parseFloat(limitPrice) < currentPrice) {
            Alert.alert('Invalid Limit Price', `SELL limit price must be ≥ current price of A${currentPrice.toFixed(2)}`);
            return;
        }

        console.log('💰 Sell Transaction Initiation');
        console.log('  Account ID:', activeAccount.accountId);
        console.log('  Account Name:', activeAccount.accountName);
        console.log('  Stock:', stockData.symbol);
        console.log('  Holding ID:', holding.holdingId);
        console.log('  Shares to Sell:', shareCount);
        console.log('  Currently Owned:', ownedShares);
        console.log('  Price:', effectivePrice);
        console.log('  Net Proceeds:', netAmount);
        console.log('  Current Account Balance:', activeAccount.cashBalance);

        Alert.alert(
            'Confirm Sale',
            `Sell ${shareCount} shares of ${stockData.symbol} at ${priceType === 'market' ? 'market price' : `A$${effectivePrice.toFixed(2)}`}?\n\nNet proceeds: A$${netAmount.toFixed(2)}`,
            [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Confirm',
                    onPress: async () => {
                        setLoading(true);
                        try {
                            console.log('💰 SELL: Starting order placement...');
                            console.log('  Stock Symbol:', stockData.symbol);
                            console.log('  Order Type:', priceType);
                            console.log('  Shares to Sell:', shareCount);
                            console.log('  Currently Owned:', ownedShares);
                            console.log('  Limit Price (if limit):', limitPrice);
                            console.log('  Effective Price:', effectivePrice);

                            const stock = await getOrCreateStockBySymbol(stockData.symbol);
                            console.log('✅ SELL: Stock retrieved/created:', stock.stockId, stock.stockCode);

                            if (priceType === 'limit') {
                                console.log('🎯 SELL: Creating LIMIT order...');
                                // Create a limit order
                                const orderRequest: CreateOrderRequest = {
                                    stockId: stock.stockId,
                                    accountId: activeAccount.accountId,
                                    quantity: shareCount,
                                    limitPrice: parseFloat(limitPrice),
                                    orderType: 'SELL_LIMIT' as OrderType,
                                };

                                console.log('📤 SELL: Sending limit order request:', {
                                  stockId: orderRequest.stockId,
                                  accountId: orderRequest.accountId,
                                  quantity: orderRequest.quantity,
                                  limitPrice: orderRequest.limitPrice,
                                  orderType: orderRequest.orderType,
                                });

                                const response = await createOrder(orderRequest);
                                console.log('✅ SELL: Limit order created successfully!', {
                                  orderId: response.orderId,
                                  status: response.status,
                                  createdAt: response.createdAt,
                                });

                                Alert.alert('Success', `Limit order placed! Your order will execute when ${stockData.symbol} reaches A$${limitPrice} or above.`);
                                navigation.goBack();
                            } else {
                                console.log('📊 SELL: Creating MARKET order...');
                                // Create a market order
                                const transactionRequest: CreateTransactionRequest = {
                                    stockId: stock.stockId,
                                    accountId: activeAccount.accountId,
                                    shareQuantity: shareCount,
                                    pricePerShare: effectivePrice,
                                    transactionType: 'SELL' as TransactionType,
                                };

                                console.log('📤 SELL: Sending market transaction request:', {
                                  stockId: transactionRequest.stockId,
                                  accountId: transactionRequest.accountId,
                                  shareQuantity: transactionRequest.shareQuantity,
                                  pricePerShare: transactionRequest.pricePerShare,
                                  transactionType: transactionRequest.transactionType,
                                });

                                const response = await createTransaction(transactionRequest);
                                console.log('✅ SELL: Market transaction completed!', {
                                  transactionId: response.transactionId,
                                  transactionType: response.transactionType,
                                  createdAt: response.createdAt,
                                });

                                Alert.alert('Success', `Successfully sold ${shareCount} shares of ${stockData.symbol}!`);
                                navigation.goBack();
                            }
                        } catch (error: any) {
                            console.error('❌ SELL: Error occurred:', {
                              message: error.message,
                              code: error.code,
                              status: error.status,
                              fullError: error,
                            });
                            Alert.alert('Error', error.message || 'Failed to complete transaction');
                        } finally {
                            setLoading(false);
                        }
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
                <Text style={[styles.headerTitle, { color: Colors.text }]}>Sell Stock</Text>

                {/* Account Banner */}
                {activeAccount && (
                    <View style={[styles.accountBanner, { backgroundColor: Colors.tint + '20', borderColor: Colors.tint }]}>
                        <MaterialCommunityIcons name="wallet-outline" size={16} color={Colors.tint} />
                        <View style={{ flex: 1 }}>
                            <Text style={[styles.accountBannerLabel, { color: Colors.tint }]}>
                                Trading from:
                            </Text>
                            <Text style={[styles.accountBannerValue, { color: Colors.tint }]}>
                                {activeAccount.accountName} • A${activeAccount.cashBalance.toLocaleString('en-AU')}
                            </Text>
                        </View>
                    </View>
                )}

                {stockData ? (
                    <>
                        {/* Stock Info Card */}
                        <View style={[styles.stockCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <View style={styles.stockHeader}>
                                <View style={[styles.sectorBadge, { backgroundColor: sectorColor.bgLight }]}>
                                    <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                                        {displayStockData.sector.charAt(0)}
                                    </Text>
                                </View>
                                <View style={styles.stockInfo}>
                                    <Text style={[styles.stockSymbol, { color: sectorColor.color }]}>
                                        {displayStockData.symbol}
                                    </Text>
                                    <Text style={[styles.stockName, { color: Colors.text, opacity: 0.7 }]}>
                                        {displayStockData.name}
                                    </Text>
                                </View>
                            </View>
                            <View style={styles.priceRow}>
                                <Text style={[styles.priceLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Current Price
                                </Text>
                                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                                    <Text style={[styles.currentPrice, { color: Colors.text }]}>
                                        A${currentPrice.toFixed(2)}
                                    </Text>
                                    {loadingPrice && (
                                        <ActivityIndicator size="small" color={Colors.tint} />
                                    )}
                                </View>
                            </View>

                            {/* Owned Shares - Loading State */}
                            {loadingHolding ? (
                                <View style={[styles.ownedRow, { backgroundColor: Colors.card, borderColor: Colors.border, justifyContent: 'center' }]}>
                                    <ActivityIndicator size="small" color={Colors.tint} />
                                    <Text style={[styles.ownedText, { color: Colors.text, opacity: 0.6, marginLeft: 8 }]}>
                                        Loading holdings...
                                    </Text>
                                </View>
                            ) : holding ? (
                                <View style={[styles.ownedRow, { backgroundColor: '#FFF9E6', borderColor: '#FFD700' }]}>
                                    <MaterialCommunityIcons name="wallet-outline" size={16} color="#B8860B" />
                                    <Text style={[styles.ownedText, { color: '#B8860B' }]}>
                                        You own {ownedShares} shares
                                    </Text>
                                </View>
                            ) : (
                                <View style={[styles.ownedRow, { backgroundColor: '#FFEBEE', borderColor: '#C62828' }]}>
                                    <MaterialCommunityIcons name="alert-circle-outline" size={16} color="#C62828" />
                                    <Text style={[styles.ownedText, { color: '#C62828' }]}>
                                        {holdingError || `You don't own any shares`}
                                    </Text>
                                </View>
                            )}
                        </View>

                        {/* Show form only if user has holdings */}
                        {holding && ownedShares > 0 ? (
                            <>
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
                                    disabled={shareCount <= 0 || shareCount > ownedShares || loading}
                                >
                                    {loading ? (
                                        <ActivityIndicator color="white" />
                                    ) : (
                                        <>
                                            <MaterialCommunityIcons
                                                name="check-circle"
                                                size={20}
                                                color="white"
                                            />
                                            <Text style={styles.sellButtonText}>
                                                Sell {shareCount > 0 ? `${shareCount} Shares` : 'Stock'}
                                            </Text>
                                        </>
                                    )}
                                </TouchableOpacity>
                            </>
                        ) : !loadingHolding ? (
                            <View style={[styles.emptyState, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                <MaterialCommunityIcons
                                    name="alert-circle-outline"
                                    size={48}
                                    color="#C62828"
                                    style={{ opacity: 0.6 }}
                                />
                                <Text style={[styles.emptyText, { color: Colors.text }]}>
                                    No holdings found
                                </Text>
                                <Text style={[styles.emptySubtext, { color: Colors.text, opacity: 0.6 }]}>
                                    You do not own any shares of {stockData.symbol}
                                </Text>
                            </View>
                        ) : null}
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
        marginLeft: 90,
    },
    headerTitleContainer: {
        flex: 1,
    },
    headerTitle: {
        fontSize: 28, fontWeight: "800", fontStyle: "italic", marginLeft: 10,
        marginBottom: 15, marginTop: -20
    },
    accountBanner: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        borderWidth: 1,
        borderRadius: 10,
        padding: 12,
        marginBottom: 16,
    },
    accountBannerLabel: {
        fontSize: 10,
        fontWeight: '600',
        opacity: 0.8,
    },
    accountBannerValue: {
        fontSize: 13,
        fontWeight: '700',
        marginTop: 2,
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