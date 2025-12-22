import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Modal,
    Alert,
    ActivityIndicator,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import { useNavigation, useRoute } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RouteProp } from '@react-navigation/native';
import type { RootStackParamList, TabParamList } from '@/src/navigation';
import { useAuth } from '@/src/context/AuthContext';
import { getUserDashboard } from '@/src/services/dashboardService';
import type { DashboardDTO } from '@/src/types/api';
import { logout as apiLogout } from '@/src/services/authService';
import { deleteAccount as apiDeleteAccount} from '@/src/services/portfolioService';
import WalletModal from '@/src/screens/wallet/WalletModal';
import OrdersSection from '@/src/screens/tabs/profile/OrdersSection';


const ProfileMenuOption = ({
                               icon,
                               label,
                               value,
                               onPress,
                               colors,
                           }: {
    icon: string;
    label: string;
    value?: string;
    onPress: () => void;
    colors: any;
}) => {
    return (
        <TouchableOpacity
            onPress={onPress}
            style={[
                styles.menuOption,
                {
                    backgroundColor: colors.card,
                    borderColor: colors.border,
                }
            ]}
            activeOpacity={0.7}
        >
            <View style={styles.menuLeft}>
                <View style={styles.menuIconContainer}>
                    <MaterialCommunityIcons
                        name={icon as any}
                        size={20}
                        color={colors.tint}
                    />
                </View>
                <Text style={[styles.menuLabel, { color: colors.text }]}>
                    {label}
                </Text>
            </View>
            <View style={styles.menuRight}>
                {value && (
                    <Text style={[styles.menuValue, { color: colors.text, opacity: 0.6 }]}>
                        {value}
                    </Text>
                )}
                <MaterialCommunityIcons
                    name="chevron-right"
                    size={20}
                    color={colors.text}
                    style={{ opacity: 0.4 }}
                />
            </View>
        </TouchableOpacity>
    );
};

export default function ProfileScreen() {
    const {Colors} = useTheme();
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const route = useRoute<RouteProp<TabParamList, 'Profile'>>();
    const { user, accounts, activeAccount, switchAccount, logout, refreshAccounts, setActiveAccount } = useAuth();

    const [showAccountSwitcher, setShowAccountSwitcher] = useState(false);
    const [showWalletModal, setShowWalletModal] = useState(false);
    const [dashboardData, setDashboardData] = useState<DashboardDTO | null>(null);
    const [loading, setLoading] = useState(false);
    const [deleting, setDeleting] = useState(false);

    useEffect(() => {
        if (user && activeAccount) {
            loadDashboardData();
        }
    }, [user, activeAccount]);

    // Check if we should open wallet modal from navigation params
    useEffect(() => {
        if (route.params?.openWallet === true && activeAccount) {
            setShowWalletModal(true);
        }
    }, [route.params?.openWallet, activeAccount]);

    const loadDashboardData = async () => {
        if (!user) return;

        try {
            setLoading(true);
            const data = await getUserDashboard(user.userId);
            setDashboardData(data);
        } catch (error) {
            console.error('Failed to load dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAccountSwitch = (accountId: string) => {
        switchAccount(accountId);
        setShowAccountSwitcher(false);
    };

    const handleCreateAccount = () => {
        navigation.navigate('CreateAccount');
    };

    const handleDeleteAccount = () => {
        Alert.alert(
            'Delete Account',
            `Are you sure you want to delete "${activeAccount?.accountName}"? This action cannot be undone.`,
            [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Delete',
                    onPress: async () => {
                        if (!activeAccount) return;

                        try {
                            setDeleting(true);
                            await apiDeleteAccount(activeAccount.accountId);

                            // Refresh accounts after deletion
                            await refreshAccounts();

                            Alert.alert('Success', 'Account deleted successfully');
                        } catch (error) {
                            console.error('Failed to delete account:', error);
                            Alert.alert('Error', 'Failed to delete account. Please try again.');
                        } finally {
                            setDeleting(false);
                        }
                    },
                    style: 'destructive',
                },
            ]
        );
    };

    const handleSettingsPress = () => {
        navigation.navigate('Settings');
    };

    const handleTransactionHistoryPress = () => {
        navigation.navigate('TransactionHistory');
    };

    const handleLogoutPress = () => {
        Alert.alert(
            'Logout',
            'Are you sure you want to logout?',
            [
                { text: 'Cancel', style: 'cancel' },
                {
                    text: 'Logout',
                    onPress: async () => {
                        try {
                            await apiLogout();
                            await logout();
                            // Navigation is handled by auth state change in AuthProvider
                        } catch (error) {
                            Alert.alert('Error', 'Failed to logout. Please try again.');
                        }
                    },
                    style: 'destructive',
                },
            ]
        );
    };

    const handleWalletPress = () => {
        if (!activeAccount) {
            Alert.alert('No Account', 'Please select or create an account first');
            return;
        }
        setShowWalletModal(true);
    };

    const handleBalanceUpdate = async (newBalance: number) => {
        console.log('📱 ProfileScreen: handleBalanceUpdate called');
        console.log('  New Balance:', newBalance);

        if (activeAccount) {
            const updatedAccount = {
                ...activeAccount,
                cashBalance: newBalance,
            };

            console.log('📝 Setting local activeAccount:', updatedAccount);
            setActiveAccount(updatedAccount);

            try {
                console.log('🔄 Refreshing accounts from API...');
                await refreshAccounts();
                console.log('✅ refreshAccounts completed');

                // Small delay to ensure backend has processed the change
                await new Promise(resolve => setTimeout(resolve, 500));

                console.log('🔄 Loading dashboard data...');
                await loadDashboardData();
                console.log('✅ loadDashboardData completed');
            } catch (error) {
                console.error('❌ Refresh error:', error);
                Alert.alert('Warning', 'Balance updated but refresh failed');
            }
        }
    };

    // Calculate portfolio stats from dashboard data
    const portfolioStats = dashboardData ? {
        totalValue: dashboardData.portfolioOverview.totalPortfolioValue, // Already includes cash and holdings
        totalInvested: dashboardData.portfolioOverview.totalCostBasis,
        totalReturn: dashboardData.portfolioOverview.totalUnrealizedGain + dashboardData.portfolioOverview.totalRealizedGain,
        returnPercent: dashboardData.portfolioPerformance.roiPercentage,
        bestPerformer: dashboardData.portfolioOverview.holdings.length > 0
            ? [...dashboardData.portfolioOverview.holdings].sort((a, b) => b.unrealizedGainPercent - a.unrealizedGainPercent)[0]?.stockSymbol || 'N/A'
            : 'N/A',
        worstPerformer: dashboardData.portfolioOverview.holdings.length > 0
            ? [...dashboardData.portfolioOverview.holdings].sort((a, b) => a.unrealizedGainPercent - b.unrealizedGainPercent)[0]?.stockSymbol || 'N/A'
            : 'N/A',
    } : null;

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header with Settings Icon */}
            <View style={[styles.header, { borderBottomColor: Colors.border }]}>
                <View>
                    <Text style={[styles.title, { color: Colors.text }]}>
                        Profile
                    </Text>
                </View>
                <TouchableOpacity
                    onPress={handleSettingsPress}
                    style={[styles.settingsButton, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                >
                    <MaterialCommunityIcons
                        name="cog"
                        size={20}
                        color={Colors.tint}
                    />
                </TouchableOpacity>
            </View>

            <ScrollView
                showsVerticalScrollIndicator={false}
                contentContainerStyle={styles.scrollContent}
            >
                {/* User Profile Card */}
                <View style={[styles.profileCard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <View style={styles.profileHeader}>
                        {/* Profile Picture */}
                        <View style={[styles.profileImageContainer, { backgroundColor: Colors.tint }]}>
                            <MaterialCommunityIcons
                                name="account-circle"
                                size={80}
                                color="white"
                            />
                        </View>

                        {/* User Info */}
                        <View style={styles.userInfo}>
                            <Text style={[styles.userName, { color: Colors.text }]}>
                                {user?.fullName || 'User'}
                            </Text>
                            <Text style={[styles.userEmail, { color: Colors.text, opacity: 0.7 }]}>
                                {user?.email || 'No email'}
                            </Text>
                            {activeAccount && (
                                <View style={[styles.brokerBadge, { backgroundColor: Colors.tint + '20' }]}>
                                    <MaterialCommunityIcons
                                        name="wallet-outline"
                                        size={12}
                                        color={Colors.tint}
                                    />
                                    <Text style={[styles.brokerText, { color: Colors.tint }]}>
                                        {activeAccount.accountName}
                                    </Text>
                                </View>
                            )}
                        </View>
                    </View>

                    {/* Account Switcher, Create Account, and Delete Account Buttons */}
                    <View style={styles.accountButtonsContainer}>
                        <View style={styles.accountButtons}>
                            {accounts.length > 1 && (
                                <TouchableOpacity
                                    onPress={() => setShowAccountSwitcher(true)}
                                    style={[styles.accountButton, { backgroundColor: Colors.background, borderColor: Colors.border, flex: 1 }]}
                                >
                                    <MaterialCommunityIcons
                                        name="account-switch-outline"
                                        size={16}
                                        color={Colors.tint}
                                    />
                                    <Text style={[styles.accountButtonText, { color: Colors.tint }]}>
                                        Switch Account
                                    </Text>
                                </TouchableOpacity>
                            )}
                            <TouchableOpacity
                                onPress={handleCreateAccount}
                                style={[styles.accountButton, { backgroundColor: Colors.tint, flex: accounts.length > 1 ? 1 : undefined }]}
                            >
                                <MaterialCommunityIcons
                                    name="plus-circle"
                                    size={16}
                                    color="white"
                                />
                                <Text style={[styles.accountButtonText, { color: 'white' }]}>
                                    Create Account
                                </Text>
                            </TouchableOpacity>
                        </View>

                        {/* Delete Account Button - Only show if user has more than 1 account */}
                        {accounts.length > 1 && (
                            <TouchableOpacity
                                onPress={handleDeleteAccount}
                                disabled={deleting}
                                style={[styles.deleteAccountButton, { opacity: deleting ? 0.6 : 1 }]}
                            >
                                {deleting ? (
                                    <ActivityIndicator size="small" color="#C62828" />
                                ) : (
                                    <>
                                        <MaterialCommunityIcons
                                            name="trash-can-outline"
                                            size={16}
                                            color="#C62828"
                                        />
                                        <Text style={[styles.deleteAccountButtonText, { color: '#C62828' }]}>
                                            Delete Account
                                        </Text>
                                    </>
                                )}
                            </TouchableOpacity>
                        )}
                    </View>
                </View>

                {/* Portfolio Analytics Summary */}
                {loading ? (
                    <View style={[styles.loadingContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <ActivityIndicator size="large" color={Colors.tint} />
                        <Text style={[styles.loadingText, { color: Colors.text, opacity: 0.6 }]}>
                            Loading portfolio data...
                        </Text>
                    </View>
                ) : portfolioStats ? (
                    <View style={styles.analyticsSection}>
                        <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                            Account Summary
                        </Text>

                        {/* Wallet and Holdings - Stacked */}
                        <View style={[styles.walletHoldingsContainer, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <View style={styles.walletHoldingRow}>
                                <View style={styles.walletHoldingItem}>
                                    <Text style={[styles.walletHoldingLabel, { color: Colors.text, opacity: 0.6 }]}>
                                        Total
                                    </Text>
                                    <Text style={[styles.totalReturnValue, { color: Colors.text }]}>
                                        A${((activeAccount?.cashBalance || 0) + (dashboardData?.portfolioOverview.holdingsValue || 0)).toLocaleString('en-AU', { maximumFractionDigits: 0 })}
                                    </Text>
                                </View>
                                <View style={[styles.walletHoldingDivider, { backgroundColor: Colors.border }]} />
                                <View style={styles.walletHoldingItem}>
                                    <Text style={[styles.walletHoldingLabel, { color: Colors.text, opacity: 0.6 }]}>
                                        % Return
                                    </Text>
                                    <Text style={[styles.totalReturnValue, { color: portfolioStats.returnPercent >= 0 ? '#2E7D32' : '#C62828' }]}>
                                        {portfolioStats.returnPercent >= 0 ? '+' : ''}{portfolioStats.returnPercent.toFixed(2)}%
                                    </Text>
                                </View>
                            </View>
                        </View>

                        {/* Detailed Stats */}
                        <View style={[styles.detailedStats, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                            <View style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Wallet Value
                                </Text>
                                <Text style={[styles.detailValue, { color: Colors.text }]}>
                                    A${(activeAccount?.cashBalance || 0).toLocaleString('en-AU', { maximumFractionDigits: 0 })}
                                </Text>
                            </View>
                            <View style={[styles.detailDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.detailRow}>
                                <Text style={[styles.walletHoldingLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Holdings Value
                                </Text>
                                <Text style={[styles.walletHoldingValue, { color: Colors.text }]}>
                                    A${(dashboardData?.portfolioOverview.holdingsValue || 0).toLocaleString('en-AU', { maximumFractionDigits: 0 })}
                                </Text>
                            </View>
                            <View style={[styles.detailDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Amount Invested
                                </Text>
                                <Text style={[styles.detailValue, { color: Colors.text }]}>
                                    A${portfolioStats.totalInvested.toLocaleString('en-AU')}
                                </Text>
                            </View>
                            <View style={[styles.detailDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Gain/Loss
                                </Text>
                                <Text style={[styles.detailValue, { color: portfolioStats.totalReturn >= 0 ? '#2E7D32' : '#C62828' }]}>
                                    {portfolioStats.totalReturn >= 0 ? '+' : ''}A${portfolioStats.totalReturn.toLocaleString('en-AU')}
                                </Text>
                            </View>
                            <View style={[styles.detailDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Best Performer
                                </Text>
                                <Text style={[styles.detailValue, { color: '#2E7D32' }]}>
                                    {portfolioStats.bestPerformer}
                                </Text>
                            </View>
                            <View style={[styles.detailDivider, { backgroundColor: Colors.border }]} />
                            <View style={styles.detailRow}>
                                <Text style={[styles.detailLabel, { color: Colors.text, opacity: 0.6 }]}>
                                    Worst Performer
                                </Text>
                                <Text style={[styles.detailValue, { color: '#C62828' }]}>
                                    {portfolioStats.worstPerformer}
                                </Text>
                            </View>
                        </View>
                    </View>
                ) : null}

                {/* Menu Options */}
                <View style={styles.menuSection}>
                    <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                        Account
                    </Text>

                    <ProfileMenuOption
                        icon="wallet"
                        label="Manage Wallet"
                        value={activeAccount ? `A$${activeAccount.cashBalance.toLocaleString('en-AU')}` : undefined}
                        onPress={handleWalletPress}
                        colors={Colors}
                    />

                    <ProfileMenuOption
                        icon="history"
                        label="Transaction History"
                        onPress={handleTransactionHistoryPress}
                        colors={Colors}
                    />

                    <ProfileMenuOption
                        icon="download"
                        label="Download Statements"
                        onPress={() => Alert.alert('Download Statements', 'Coming soon!')}
                        colors={Colors}
                    />

                    <ProfileMenuOption
                        icon="bell-outline"
                        label="Notifications"
                        value="On"
                        onPress={() => Alert.alert('Notifications', 'Coming soon!')}
                        colors={Colors}
                    />

                    <ProfileMenuOption
                        icon="lock-outline"
                        label="Security"
                        onPress={() => Alert.alert('Security', 'Coming soon!')}
                        colors={Colors}
                    />
                </View>

                {/* Limit Orders Section */}
                {activeAccount && (
                    <View style={styles.ordersSection}>
                        <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                            Limit Orders
                        </Text>
                        <OrdersSection />
                    </View>
                )}

                {/* Danger Zone */}
                <View style={styles.menuSection}>
                    <TouchableOpacity
                        onPress={handleLogoutPress}
                        style={[styles.logoutButton, { backgroundColor: '#FCE4E4', borderColor: '#FFB3B3' }]}
                    >
                        <MaterialCommunityIcons
                            name="logout"
                            size={18}
                            color="#C62828"
                        />
                        <Text style={[styles.logoutButtonText, { color: '#C62828' }]}>
                            Logout
                        </Text>
                    </TouchableOpacity>
                </View>

                <View style={{ height: 24 }} />
            </ScrollView>

            {/* Account Switcher Modal */}
            <Modal
                visible={showAccountSwitcher}
                transparent={true}
                animationType="slide"
                onRequestClose={() => setShowAccountSwitcher(false)}
            >
                <View style={[styles.modalOverlay, { backgroundColor: Colors.background }]}>
                    <View style={styles.modalHeader}>
                        <Text style={[styles.modalTitle, { color: Colors.text }]}>
                            Switch Account
                        </Text>
                        <TouchableOpacity
                            onPress={() => setShowAccountSwitcher(false)}
                            style={styles.closeButton}
                        >
                            <MaterialCommunityIcons
                                name="close"
                                size={24}
                                color={Colors.text}
                            />
                        </TouchableOpacity>
                    </View>

                    <View style={styles.modalContent}>
                        {accounts.map(account => (
                            <TouchableOpacity
                                key={account.accountId}
                                onPress={() => handleAccountSwitch(account.accountId)}
                                style={[
                                    styles.accountOption,
                                    {
                                        backgroundColor: Colors.card,
                                        borderColor: activeAccount?.accountId === account.accountId ? Colors.tint : Colors.border,
                                        borderWidth: activeAccount?.accountId === account.accountId ? 2 : 1,
                                    }
                                ]}
                            >
                                <View style={[styles.accountOptionImage, { backgroundColor: Colors.tint }]}>
                                    <MaterialCommunityIcons
                                        name="wallet-outline"
                                        size={24}
                                        color="white"
                                    />
                                </View>
                                <View style={styles.accountOptionInfo}>
                                    <Text style={[styles.accountOptionName, { color: Colors.text }]}>
                                        {account.accountName}
                                    </Text>
                                    <Text style={[styles.accountOptionEmail, { color: Colors.text, opacity: 0.6 }]}>
                                        Balance: A${account.cashBalance.toLocaleString('en-AU')}
                                    </Text>
                                </View>
                                {activeAccount?.accountId === account.accountId && (
                                    <MaterialCommunityIcons
                                        name="check-circle"
                                        size={24}
                                        color={Colors.tint}
                                    />
                                )}
                            </TouchableOpacity>
                        ))}
                    </View>
                </View>
            </Modal>

            {/* Wallet Modal */}
            <WalletModal
                visible={showWalletModal}
                onClose={() => setShowWalletModal(false)}
                account={activeAccount}
                onBalanceUpdate={handleBalanceUpdate}  // Now properly awaits
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginTop: -40,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 12,
        paddingVertical: 16,
    },
    title: {
        fontSize: 28,
        fontWeight: '800',
        fontStyle: 'italic',
    },
    settingsButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        marginRight: -12,
        borderWidth: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    scrollContent: {
        paddingVertical: 0,
        gap: 24,
    },
    profileCard: {
        borderWidth: 1,
        borderRadius: 16,
        padding: 20,
        gap: 16,
    },
    profileHeader: {
        flexDirection: 'row',
        gap: 16,
        alignItems: 'flex-start',
    },
    profileImageContainer: {
        width: 80,
        height: 80,
        borderRadius: 40,
        alignItems: 'center',
        justifyContent: 'center',
    },
    userInfo: {
        flex: 1,
        gap: 8,
    },
    userName: {
        fontSize: 18,
        fontWeight: '800',
    },
    userEmail: {
        fontSize: 13,
        fontWeight: '500',
    },
    brokerBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        paddingHorizontal: 10,
        paddingVertical: 4,
        borderRadius: 6,
        alignSelf: 'flex-start',
        marginTop: 4,
    },
    brokerText: {
        fontSize: 11,
        fontWeight: '600',
    },
    accountButtonsContainer: {
        gap: 10,
    },
    accountButtons: {
        flexDirection: 'row',
        gap: 10,
    },
    accountButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 10,
        paddingHorizontal: 16,
        borderRadius: 10,
    },
    accountButtonText: {
        fontSize: 12,
        fontWeight: '700',
    },
    deleteAccountButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        paddingVertical: 10,
        paddingHorizontal: 16,
        borderRadius: 10,
        backgroundColor: '#FCE4E4',
        borderWidth: 1,
        borderColor: '#FFB3B3',
    },
    deleteAccountButtonText: {
        fontSize: 12,
        fontWeight: '700',
    },
    loadingContainer: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 40,
        alignItems: 'center',
        justifyContent: 'center',
        gap: 12,
    },
    loadingText: {
        fontSize: 12,
        fontWeight: '600',
    },
    analyticsSection: {
        gap: 12,
    },
    sectionTitle: {
        fontSize: 16,
        fontWeight: '700',
        fontStyle: 'italic',
        paddingHorizontal: 12,
    },
    walletHoldingsContainer: {
        borderWidth: 1,
        borderRadius: 12,
        overflow: 'hidden',
    },
    walletHoldingRow: {
        flexDirection: 'row',
    },
    walletHoldingItem: {
        flex: 1,
        paddingVertical: 16,
        paddingHorizontal: 12,
        alignItems: 'center',
        justifyContent: 'center',
    },
    walletHoldingLabel: {
        fontSize: 11,
        fontWeight: '600',
        marginBottom: 6,
    },
    walletHoldingValue: {
        fontSize: 14,
        fontWeight: '700',
    },
    walletHoldingDivider: {
        width: 1,
    },
    totalReturnContainer: {
        flexDirection: 'row',
        gap: 12,
    },
    totalCard: {
        flex: 1,
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 20,
        paddingHorizontal: 16,
        alignItems: 'center',
        justifyContent: 'center',
    },
    returnCard: {
        flex: 1,
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 20,
        paddingHorizontal: 16,
        alignItems: 'center',
        justifyContent: 'center',
    },
    totalReturnLabel: {
        fontSize: 11,
        fontWeight: '600',
        marginBottom: 8,
    },
    totalReturnValue: {
        fontSize: 18,
        fontWeight: '800',
    },
    statsGrid: {
        borderWidth: 1,
        borderRadius: 12,
        flexDirection: 'row',
        overflow: 'hidden',
    },
    statBlock: {
        flex: 1,
        paddingVertical: 14,
        paddingHorizontal: 12,
        alignItems: 'center',
        justifyContent: 'center',
    },
    statLabel: {
        fontSize: 10,
        fontWeight: '600',
        marginBottom: 6,
    },
    statValueLarge: {
        fontSize: 18,
        fontWeight: '800',
    },
    statDivider: {
        width: 1,
    },
    detailedStats: {
        borderWidth: 1,
        borderRadius: 12,
        overflow: 'hidden',
    },
    detailRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingVertical: 12,
        paddingHorizontal: 16,
    },
    detailLabel: {
        fontSize: 12,
        fontWeight: '600',
    },
    detailValue: {
        fontSize: 13,
        fontWeight: '700',
    },
    detailDivider: {
        height: 1,
    },
    menuSection: {
        gap: 10,
    },
    menuOption: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    menuLeft: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        flex: 1,
    },
    menuIconContainer: {
        width: 40,
        height: 40,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'transparent',
    },
    menuLabel: {
        fontSize: 13,
        fontWeight: '600',
    },
    menuRight: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    menuValue: {
        fontSize: 12,
        fontWeight: '600',
    },
    logoutButton: {
        borderWidth: 1,
        borderRadius: 12,
        paddingVertical: 12,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
    },
    logoutButtonText: {
        fontSize: 13,
        fontWeight: '700',
    },
    modalOverlay: {
        flex: 1,
        paddingTop: 60,
    },
    modalHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 24,
        paddingVertical: 16,
        borderBottomWidth: 1,
    },
    modalTitle: {
        fontSize: 20,
        fontWeight: '800',
    },
    closeButton: {
        padding: 8,
    },
    modalContent: {
        paddingHorizontal: 24,
        paddingVertical: 20,
        gap: 12,
    },
    accountOption: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
    },
    accountOptionImage: {
        width: 50,
        height: 50,
        borderRadius: 25,
        alignItems: 'center',
        justifyContent: 'center',
    },
    accountOptionInfo: {
        flex: 1,
        gap: 4,
    },
    accountOptionName: {
        fontSize: 13,
        fontWeight: '700',
    },
    accountOptionEmail: {
        fontSize: 11,
        fontWeight: '500',
    },
    ordersSection: {
        gap: 12,
        paddingBottom: 24,
    },
});