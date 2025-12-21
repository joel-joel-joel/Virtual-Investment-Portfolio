import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    Modal,
    TouchableOpacity,
    TextInput,
    useColorScheme,
    Alert,
    ActivityIndicator,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import { updateAccount } from '@/src/services/portfolioService';
import type { AccountDTO } from '@/src/types/api';

interface WalletModalProps {
    visible: boolean;
    onClose: () => void;
    account: AccountDTO | null;
    onBalanceUpdate: (newBalance: number) => void;
}

type TransactionType = 'deposit' | 'withdraw';

export default function WalletModal({
    visible,
    onClose,
    account,
    onBalanceUpdate,
}: WalletModalProps) {
    const {Colors} = useTheme()

    const [transactionType, setTransactionType] = useState<TransactionType>('deposit');
    const [amount, setAmount] = useState('');
    const [loading, setLoading] = useState(false);
    const [currentBalance, setCurrentBalance] = useState(account?.cashBalance || 0);

    useEffect(() => {
        if (account) {
            setCurrentBalance(account.cashBalance);
        }
    }, [account]);

    const handleClose = () => {
        setAmount('');
        setTransactionType('deposit');
        onClose();
    };

    const handleAmountChange = (text: string) => {
        // Remove non-numeric characters except decimal point
        const cleaned = text.replace(/[^0-9.]/g, '');

        // Ensure only one decimal point
        const parts = cleaned.split('.');
        if (parts.length > 2) {
            return;
        }

        // Limit to 2 decimal places
        if (parts[1] && parts[1].length > 2) {
            return;
        }

        setAmount(cleaned);
    };

    const validateAmount = (): { valid: boolean; error?: string } => {
        if (!amount || amount.trim() === '') {
            return { valid: false, error: 'Please enter an amount' };
        }

        const numAmount = parseFloat(amount);

        if (isNaN(numAmount) || numAmount <= 0) {
            return { valid: false, error: 'Amount must be greater than zero' };
        }

        if (transactionType === 'withdraw' && numAmount > currentBalance) {
            return {
                valid: false,
                error: `Insufficient funds. Available balance: A$${currentBalance.toFixed(2)}`,
            };
        }

        // Max transaction limit (optional)
        const MAX_TRANSACTION = 1000000; // 1 million
        if (numAmount > MAX_TRANSACTION) {
            return {
                valid: false,
                error: `Maximum transaction amount is A$${MAX_TRANSACTION.toLocaleString()}`,
            };
        }

        return { valid: true };
    };

    const handleTransaction = async () => {
        if (!account) {
            Alert.alert('Error', 'No account selected');
            return;
        }

        const validation = validateAmount();
        if (!validation.valid) {
            Alert.alert('Validation Error', validation.error);
            return;
        }

        const numAmount = parseFloat(amount);

        try {
            setLoading(true);

            const newBalance =
                transactionType === 'deposit'
                    ? currentBalance + numAmount
                    : currentBalance - numAmount;

            console.log('='.repeat(60));
            console.log('💰 WalletModal: Starting transaction');
            console.log('  Type:', transactionType);
            console.log('  Account ID:', account.accountId);
            console.log('  Account Name:', account.accountName);
            console.log('  Current Balance:', currentBalance);
            console.log('  Amount:', numAmount);
            console.log('  New Balance:', newBalance);
            console.log('-'.repeat(60));

            console.log('📤 Calling updateAccount API...');

            // Update account balance via API
            const response = await updateAccount(account.accountId, {
                accountName: account.accountName,
                cashBalance: newBalance,
            });

            console.log('✅ API Response received!');
            console.log('  Response:', JSON.stringify(response, null, 2));
            console.log('  Response cashBalance:', response.cashBalance);
            console.log('-'.repeat(60));

            // Update local state
            setCurrentBalance(newBalance);
            console.log('✅ Local state updated');

            // Notify parent component
            console.log('📢 Calling onBalanceUpdate with:', newBalance);
            onBalanceUpdate(newBalance);

            // Show success message
            Alert.alert(
                'Success',
                `A$${numAmount.toFixed(2)} ${transactionType === 'deposit' ? 'deposited' : 'withdrawn'} successfully!`,
                [
                    {
                        text: 'OK',
                        onPress: handleClose,
                    }
                ]
            );

            // Clear amount after successful transaction
            setAmount('');
            console.log('='.repeat(60));
        } catch (error: any) {
            console.error('='.repeat(60));
            console.error('❌ WalletModal: Transaction FAILED');
            console.error('  Error Message:', error.message);
            console.error('  Error:', error);
            console.error('='.repeat(60));

            Alert.alert(
                'Transaction Failed',
                error.message || 'Failed to process transaction. Please try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    const quickAmounts = [100, 500, 1000, 5000];

    if (!account) {
        return null;
    }

    return (
        <Modal
            visible={visible}
            transparent={true}
            animationType="slide"
            onRequestClose={handleClose}
        >
            <KeyboardAvoidingView
                style={styles.modalOverlay}
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
            >
                <TouchableOpacity
                    style={styles.backdrop}
                    activeOpacity={1}
                    onPress={handleClose}
                />
                <View style={[styles.modalContent, { backgroundColor: Colors.background }]}>
                    {/* Header */}
                    <View style={[styles.modalHeader, { borderBottomColor: Colors.border }]}>
                        <View style={[styles.walletIconContainer, { backgroundColor: Colors.tint }]}>
                            <MaterialCommunityIcons name="wallet" size={24} color="white" />
                        </View>
                        <View style={styles.headerTextContainer}>
                            <Text style={[styles.modalTitle, { color: Colors.text }]}>
                                Manage Wallet
                            </Text>
                            <Text style={[styles.accountName, { color: Colors.text, opacity: 0.7 }]}>
                                {account.accountName}
                            </Text>
                        </View>
                        <TouchableOpacity onPress={handleClose} style={styles.closeButton}>
                            <MaterialCommunityIcons name="close" size={24} color={Colors.text} />
                        </TouchableOpacity>
                    </View>

                    {/* Current Balance */}
                    <View style={[styles.balanceCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <Text style={[styles.balanceLabel, { color: Colors.text, opacity: 0.6 }]}>
                            Available Balance
                        </Text>
                        <Text style={[styles.balanceAmount, { color: Colors.text }]}>
                            A${currentBalance.toLocaleString('en-AU', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2,
                            })}
                        </Text>
                    </View>

                    {/* Transaction Type Selector */}
                    <View style={styles.typeSelector}>
                        <TouchableOpacity
                            onPress={() => setTransactionType('deposit')}
                            style={[
                                styles.typeButton,
                                {
                                    backgroundColor:
                                        transactionType === 'deposit' ? Colors.tint : Colors.card,
                                    borderColor: transactionType === 'deposit' ? Colors.tint : Colors.border,
                                },
                            ]}
                            disabled={loading}
                        >
                            <MaterialCommunityIcons
                                name="plus-circle"
                                size={20}
                                color={transactionType === 'deposit' ? 'white' : Colors.tint}
                            />
                            <Text
                                style={[
                                    styles.typeButtonText,
                                    { color: transactionType === 'deposit' ? 'white' : Colors.text },
                                ]}
                            >
                                Deposit
                            </Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            onPress={() => setTransactionType('withdraw')}
                            style={[
                                styles.typeButton,
                                {
                                    backgroundColor:
                                        transactionType === 'withdraw' ? Colors.tint : Colors.card,
                                    borderColor: transactionType === 'withdraw' ? Colors.tint : Colors.border,
                                },
                            ]}
                            disabled={loading}
                        >
                            <MaterialCommunityIcons
                                name="minus-circle"
                                size={20}
                                color={transactionType === 'withdraw' ? 'white' : Colors.tint}
                            />
                            <Text
                                style={[
                                    styles.typeButtonText,
                                    { color: transactionType === 'withdraw' ? 'white' : Colors.text },
                                ]}
                            >
                                Withdraw
                            </Text>
                        </TouchableOpacity>
                    </View>

                    {/* Amount Input */}
                    <View style={styles.amountSection}>
                        <Text style={[styles.inputLabel, { color: Colors.text }]}>
                            Amount
                        </Text>
                        <View style={[styles.amountInputWrapper, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <Text style={[styles.currencySymbol, { color: Colors.text }]}>
                                A$
                            </Text>
                            <TextInput
                                style={[styles.amountInput, { color: Colors.text }]}
                                placeholder="0.00"
                                placeholderTextColor={Colors.text + '80'}
                                value={amount}
                                onChangeText={handleAmountChange}
                                keyboardType="decimal-pad"
                                editable={!loading}
                            />
                        </View>

                        {/* Quick Amount Buttons */}
                        <View style={styles.quickAmounts}>
                            {quickAmounts.map((quickAmount) => (
                                <TouchableOpacity
                                    key={quickAmount}
                                    onPress={() => setAmount(quickAmount.toString())}
                                    style={[
                                        styles.quickAmountButton,
                                        {
                                            backgroundColor: Colors.card,
                                            borderColor: Colors.border,
                                        },
                                    ]}
                                    disabled={loading}
                                >
                                    <Text style={[styles.quickAmountText, { color: Colors.tint }]}>
                                        A${quickAmount}
                                    </Text>
                                </TouchableOpacity>
                            ))}
                        </View>
                    </View>

                    {/* Transaction Button */}
                    <TouchableOpacity
                        onPress={handleTransaction}
                        style={[
                            styles.transactionButton,
                            {
                                backgroundColor: Colors.tint,
                                opacity: loading || !amount ? 0.5 : 1,
                            },
                        ]}
                        disabled={loading || !amount}
                    >
                        {loading ? (
                            <ActivityIndicator color="white" />
                        ) : (
                            <>
                                <MaterialCommunityIcons
                                    name={transactionType === 'deposit' ? 'plus-circle' : 'minus-circle'}
                                    size={20}
                                    color="white"
                                />
                                <Text style={styles.transactionButtonText}>
                                    {transactionType === 'deposit' ? 'Deposit' : 'Withdraw'} Funds
                                </Text>
                            </>
                        )}
                    </TouchableOpacity>

                    {/* Info Text */}
                    <View style={[styles.infoCard, { backgroundColor: Colors.tint + '15', borderColor: Colors.tint + '30' }]}>
                        <MaterialCommunityIcons name="information-outline" size={16} color={Colors.tint} />
                        <Text style={[styles.infoText, { color: Colors.tint }]}>
                            {transactionType === 'deposit'
                                ? 'Add funds to your account to start investing'
                                : 'Withdraw available cash from your account'}
                        </Text>
                    </View>
                </View>
            </KeyboardAvoidingView>
        </Modal>
    );
}

const styles = StyleSheet.create({
    modalOverlay: {
        flex: 1,
        justifyContent: 'flex-end',
    },
    backdrop: {
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
    },
    modalContent: {
        borderTopLeftRadius: 24,
        borderTopRightRadius: 24,
        paddingTop: 24,
        paddingHorizontal: 24,
        paddingBottom: 40,
        maxHeight: '90%',
    },
    modalHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        paddingBottom: 20,
        borderBottomWidth: 1,
        marginBottom: 20,
    },
    walletIconContainer: {
        width: 48,
        height: 48,
        borderRadius: 24,
        alignItems: 'center',
        justifyContent: 'center',
    },
    headerTextContainer: {
        flex: 1,
    },
    modalTitle: {
        fontSize: 18,
        fontWeight: '800',
    },
    accountName: {
        fontSize: 12,
        fontWeight: '500',
        marginTop: 2,
    },
    closeButton: {
        padding: 8,
    },
    balanceCard: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 20,
        alignItems: 'center',
        marginBottom: 24,
    },
    balanceLabel: {
        fontSize: 12,
        fontWeight: '600',
        marginBottom: 8,
    },
    balanceAmount: {
        fontSize: 32,
        fontWeight: '800',
    },
    typeSelector: {
        flexDirection: 'row',
        gap: 12,
        marginBottom: 24,
    },
    typeButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 14,
        borderRadius: 12,
        borderWidth: 1,
    },
    typeButtonText: {
        fontSize: 14,
        fontWeight: '700',
    },
    amountSection: {
        marginBottom: 24,
    },
    inputLabel: {
        fontSize: 13,
        fontWeight: '600',
        marginBottom: 8,
        paddingLeft: 4,
    },
    amountInputWrapper: {
        flexDirection: 'row',
        alignItems: 'center',
        borderWidth: 1,
        borderRadius: 12,
        paddingHorizontal: 16,
        height: 56,
        gap: 8,
    },
    currencySymbol: {
        fontSize: 18,
        fontWeight: '700',
    },
    amountInput: {
        flex: 1,
        fontSize: 18,
        fontWeight: '600',
    },
    quickAmounts: {
        flexDirection: 'row',
        gap: 10,
        marginTop: 12,
    },
    quickAmountButton: {
        flex: 1,
        borderWidth: 1,
        borderRadius: 8,
        paddingVertical: 10,
        alignItems: 'center',
        justifyContent: 'center',
    },
    quickAmountText: {
        fontSize: 12,
        fontWeight: '700',
    },
    transactionButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 16,
        borderRadius: 12,
        marginBottom: 16,
    },
    transactionButtonText: {
        color: 'white',
        fontSize: 15,
        fontWeight: '700',
    },
    infoCard: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        gap: 10,
        padding: 12,
        borderRadius: 10,
        borderWidth: 1,
    },
    infoText: {
        flex: 1,
        fontSize: 11,
        fontWeight: '600',
        lineHeight: 16,
    },
});
