import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    useColorScheme,
    Alert,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    ActivityIndicator,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import { useAuth } from '@/src/context/AuthContext';
import { createAccount } from '@/src/services/portfolioService';

// Type for your account DTO
interface AccountDTO {
    accountId: string;
    accountName: string;
    accountBalance: number;
    createdAt: string;
}

export default function CreateAccountScreen() {
    const {Colors} = useTheme();
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const { refreshAccounts } = useAuth();

    const [accountName, setAccountName] = useState('');
    const [initialBalance, setInitialBalance] = useState('');
    const [loading, setLoading] = useState(false);

    // Local state for created account
    const [createdAccount, setCreatedAccount] = useState<AccountDTO | null>(null);

    const handleSubmit = async () => {
        if (!accountName.trim()) {
            Alert.alert('Error', 'Please enter an account name');
            return;
        }

        const balance = initialBalance ? parseFloat(initialBalance) : 0;

        if (initialBalance && (isNaN(balance) || balance < 0)) {
            Alert.alert('Error', 'Please enter a valid initial balance');
            return;
        }

        setLoading(true);

        try {
            const newAccount = await createAccount({
                accountName: accountName.trim(),
                cashBalance: balance,
            });

            // Refresh accounts in AuthContext to update state
            await refreshAccounts();

            // Check if this is the user's first account (onboarding)
            const { accounts } = useAuth();

            if (accounts.length === 1) {
                // This is their first account - navigate directly to MainTabs without showing success UI
                console.log('📱 First account created - navigating to MainTabs');
                (navigation as any).reset({
                    index: 0,
                    routes: [{ name: 'MainTabs' }],
                });
            } else {
                // Additional account - show success UI
                console.log('📱 Additional account created - showing success UI');
                // @ts-ignore
                setCreatedAccount(newAccount);
                Alert.alert('Success', `Account "${accountName}" created successfully!`);
            }
        } catch (error: any) {
            Alert.alert('Error', error.message || 'Failed to create account. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const formatCurrency = (value: string) => {
        const cleaned = value.replace(/[^0-9.]/g, '');
        const parts = cleaned.split('.');
        if (parts.length > 2) return parts[0] + '.' + parts.slice(1).join('');
        return cleaned;
    };

    const handleBalanceChange = (text: string) => {
        setInitialBalance(formatCurrency(text));
    };

    return (
        <KeyboardAvoidingView
            style={[styles.container, { backgroundColor: Colors.background }]}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
            <ScrollView
                contentContainerStyle={styles.scrollContent}
                keyboardShouldPersistTaps="handled"
            >
                {/* Back Button */}
                <TouchableOpacity
                    onPress={() => navigation.goBack()}
                    style={[styles.backButton, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                >
                    <MaterialCommunityIcons name="chevron-left" size={24} color={Colors.text} />
                </TouchableOpacity>

                {/* Header */}
                <Text style={[styles.header, { color: Colors.text }]}>
                    Create Account
                </Text>

                {/* Info */}
                <View style={[styles.infoCard, { backgroundColor: Colors.tint + '15', borderColor: Colors.tint + '30' }]}>
                    <MaterialCommunityIcons name="information-outline" size={20} color={Colors.tint} />
                    <Text style={[styles.infoText, { color: Colors.tint }]}>
                        Create an account to start tracking your portfolio
                    </Text>
                </View>

                {/* Form */}
                {!createdAccount ? (
                    <View style={styles.formContainer}>
                        <View style={styles.inputContainer}>
                            <Text style={[styles.label, { color: Colors.text }]}>
                                Account Name <Text style={{ color: '#C62828' }}>*</Text>
                            </Text>
                            <View style={[styles.inputWrapper, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                <MaterialCommunityIcons name="account-outline" size={20} color={Colors.text} style={{ opacity: 0.5 }} />
                                <TextInput
                                    style={[styles.input, { color: Colors.text }]}
                                    placeholder="e.g., Main Portfolio"
                                    placeholderTextColor={Colors.text + '80'}
                                    value={accountName}
                                    onChangeText={setAccountName}
                                    editable={!loading}
                                    maxLength={50}
                                />
                            </View>
                        </View>

                        <View style={styles.inputContainer}>
                            <Text style={[styles.label, { color: Colors.text }]}>Initial Cash Balance (Optional)</Text>
                            <View style={[styles.inputWrapper, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                                <Text style={[styles.currencySymbol, { color: Colors.text }]}>A$</Text>
                                <TextInput
                                    style={[styles.input, { color: Colors.text }]}
                                    placeholder="0.00"
                                    placeholderTextColor={Colors.text + '80'}
                                    value={initialBalance}
                                    onChangeText={handleBalanceChange}
                                    keyboardType="decimal-pad"
                                    editable={!loading}
                                />
                            </View>
                        </View>

                        <TouchableOpacity
                            style={[styles.createButton, { backgroundColor: Colors.tint }, loading && { opacity: 0.7 }]}
                            onPress={handleSubmit}
                            disabled={loading}
                        >
                            {loading ? (
                                <ActivityIndicator color="white" />
                            ) : (
                                <>
                                    <MaterialCommunityIcons name="plus-circle" size={20} color="white" />
                                    <Text style={styles.createButtonText}>Create Account</Text>
                                </>
                            )}
                        </TouchableOpacity>
                    </View>
                ) : (
                    // Success UI
                    <View style={styles.successContainer}>
                        <View style={[styles.successIcon, { backgroundColor: '#E7F5E7' }]}>
                            <MaterialCommunityIcons name="check-circle" size={56} color="#2E7D32" />
                        </View>
                        <Text style={[styles.successTitle, { color: Colors.text }]}>
                            Account Created!
                        </Text>
                        <Text style={[styles.successText, { color: Colors.text }]}>
                            &#34;{createdAccount.accountName}&#34; is ready to use
                        </Text>
                        <View style={styles.buttonRow}>
                            <TouchableOpacity
                                style={[styles.secondaryButton, { borderColor: Colors.tint }]}
                                onPress={() => navigation.goBack()}
                            >
                                <MaterialCommunityIcons name="arrow-left" size={18} color={Colors.tint} />
                                <Text style={[styles.secondaryButtonText, { color: Colors.tint }]}>
                                    Go Back
                                </Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[styles.primaryButton, { backgroundColor: Colors.tint }]}
                                onPress={() => {
                                    // Replace the navigation stack to go to MainTabs
                                    // The back button is removed and the stack is replaced, so new users
                                    // can't go back to account creation
                                    (navigation as any).reset({
                                        index: 0,
                                        routes: [{ name: 'MainTabs' }],
                                    });
                                }}
                            >
                                <Text style={styles.primaryButtonText}>View Portfolio</Text>
                                <MaterialCommunityIcons name="arrow-right" size={18} color="white" />
                            </TouchableOpacity>
                        </View>
                    </View>
                )}

                <View style={{ height: 40 }} />
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

// Styles
const styles = StyleSheet.create({
    container: { flex: 1 },
    scrollContent: { paddingHorizontal: 24, paddingTop: 24 },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
        borderWidth: 1,
        marginTop: 45,
        marginBottom: 16
    },
    header: {
        fontSize: 28,
        fontWeight: '800',
        fontStyle: 'italic',
        marginBottom: 8
    },
    infoCard: { flexDirection: 'row', alignItems: 'flex-start', gap: 12, padding: 16, borderRadius: 12, borderWidth: 1, marginBottom: 24 },
    infoText: { flex: 1, fontSize: 12, fontWeight: '600', lineHeight: 18 },
    formContainer: { gap: 24 },
    inputContainer: { gap: 8 },
    label: { fontSize: 13, fontWeight: '600', paddingLeft: 4 },
    inputWrapper: { flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderRadius: 12, paddingHorizontal: 16, gap: 12, height: 52 },
    input: { flex: 1, fontSize: 14, fontWeight: '500' },
    currencySymbol: { fontSize: 14, fontWeight: '700', opacity: 0.7 },
    createButton: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, paddingVertical: 16, borderRadius: 12, marginTop: 32 },
    createButtonText: { color: 'white', fontSize: 15, fontWeight: '700' },
    successContainer: {
        marginTop: 32,
        alignItems: 'center',
        gap: 16
    },
    successIcon: {
        width: 100,
        height: 100,
        borderRadius: 50,
        alignItems: 'center',
        justifyContent: 'center',
        marginBottom: 8
    },
    successTitle: {
        fontSize: 24,
        fontWeight: '800',
        marginBottom: 4
    },
    successText: {
        fontSize: 15,
        fontWeight: '500',
        opacity: 0.7,
        marginBottom: 16
    },
    buttonRow: {
        flexDirection: 'row',
        gap: 12,
        width: '100%',
        marginTop: 16
    },
    secondaryButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 6,
        paddingVertical: 14,
        borderRadius: 12,
        borderWidth: 2
    },
    secondaryButtonText: {
        fontSize: 14,
        fontWeight: '700'
    },
    primaryButton: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 6,
        paddingVertical: 14,
        borderRadius: 12
    },
    primaryButtonText: {
        color: 'white',
        fontSize: 14,
        fontWeight: '700'
    },
});
