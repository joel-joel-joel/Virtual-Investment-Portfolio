
import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Image,
    Modal,
    Alert,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '@/src/constants/colors';
import { useRouter } from 'expo-router';


interface UserAccount {
    id: string;
    name: string;
    email: string;
    image?: any;
    broker?: string;
}

interface PortfolioStats {
    totalValue: number;
    totalInvested: number;
    totalReturn: number;
    returnPercent: number;
    bestPerformer: string;
    worstPerformer: string;
}

const userAccounts: UserAccount[] = [
    {
        id: '1',
        name: 'Alex Morgan',
        email: 'alex.morgan@email.com',
        broker: 'Interactive Brokers',
    },
    {
        id: '2',
        name: 'Alex Morgan',
        email: 'alex.investor@email.com',
        broker: 'Commsec',
    },
];

const portfolioStats: PortfolioStats = {
    totalValue: 1027680,
    totalInvested: 1000000,
    totalReturn: 27680,
    returnPercent: 2.768,
    bestPerformer: 'NVDA',
    worstPerformer: 'AMD',
};

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

export default function ProfileScreen({ navigation }: { navigation?: any }) {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const nav = useNavigation();
    const [activeAccount, setActiveAccount] = useState(userAccounts[0]);
    const [showAccountSwitcher, setShowAccountSwitcher] = useState(false);

    const handleAccountSwitch = (account: UserAccount) => {
        setActiveAccount(account);
        setShowAccountSwitcher(false);
    };

    const router = useRouter();

    const handleSettingsPress = () => {
        router.push('/(tabs)/settings');
    };


    const handleLogoutPress = () => {
        Alert.alert(
            'Logout',
            'Are you sure you want to logout?',
            [
                { text: 'Cancel', onPress: () => {}, style: 'cancel' },
                {
                    text: 'Logout',
                    onPress: () => {
                        Alert.alert('Logged out', 'You have been logged out successfully');
                    },
                    style: 'destructive',
                },
            ]
        );
    };

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
                                {activeAccount.name}
                            </Text>
                            <Text style={[styles.userEmail, { color: Colors.text, opacity: 0.7 }]}>
                                {activeAccount.email}
                            </Text>
                            {activeAccount.broker && (
                                <View style={[styles.brokerBadge, { backgroundColor: Colors.tint + '20' }]}>
                                    <MaterialCommunityIcons
                                        name="office-building-outline"
                                        size={12}
                                        color={Colors.tint}
                                    />
                                    <Text style={[styles.brokerText, { color: Colors.tint }]}>
                                        {activeAccount.broker}
                                    </Text>
                                </View>
                            )}
                        </View>
                    </View>

                    {/* Account Switcher Button */}
                    {userAccounts.length > 1 && (
                        <TouchableOpacity
                            onPress={() => setShowAccountSwitcher(true)}
                            style={[styles.switchAccountButton, { backgroundColor: Colors.background, borderColor: Colors.border }]}
                        >
                            <MaterialCommunityIcons
                                name="account-switch-outline"
                                size={16}
                                color={Colors.tint}
                            />
                            <Text style={[styles.switchAccountText, { color: Colors.tint }]}>
                                Switch Account
                            </Text>
                        </TouchableOpacity>
                    )}
                </View>

                {/* Portfolio Analytics Summary */}
                <View style={styles.analyticsSection}>
                    <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                        Portfolio Summary
                    </Text>

                    {/* Main Stats */}
                    <View style={[styles.statsGrid, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                        <View style={styles.statBlock}>
                            <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                                Total Value
                            </Text>
                            <Text style={[styles.statValueLarge, { color: Colors.text }]}>
                                A${(portfolioStats.totalValue / 1000).toFixed(0)}K
                            </Text>
                        </View>
                        <View style={[styles.statDivider, { backgroundColor: Colors.border }]} />
                        <View style={styles.statBlock}>
                            <Text style={[styles.statLabel, { color: Colors.text, opacity: 0.6 }]}>
                                Total Return
                            </Text>
                            <Text style={[styles.statValueLarge, { color: '#2E7D32' }]}>
                                +{portfolioStats.returnPercent.toFixed(2)}%
                            </Text>
                        </View>
                    </View>

                    {/* Detailed Stats */}
                    <View style={[styles.detailedStats, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
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
                            <Text style={[styles.detailValue, { color: '#2E7D32' }]}>
                                +A${portfolioStats.totalReturn.toLocaleString('en-AU')}
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

                {/* Menu Options */}
                <View style={styles.menuSection}>
                    <Text style={[styles.sectionTitle, { color: Colors.text }]}>
                        Account
                    </Text>

                    <ProfileMenuOption
                        icon="history"
                        label="Transaction History"
                        onPress={() => Alert.alert('Transaction History', 'Coming soon!')}
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
                        {userAccounts.map(account => (
                            <TouchableOpacity
                                key={account.id}
                                onPress={() => handleAccountSwitch(account)}
                                style={[
                                    styles.accountOption,
                                    {
                                        backgroundColor: Colors.card,
                                        borderColor: activeAccount.id === account.id ? Colors.tint : Colors.border,
                                        borderWidth: activeAccount.id === account.id ? 2 : 1,
                                    }
                                ]}
                            >
                                <View style={[styles.accountOptionImage, { backgroundColor: Colors.tint }]}>
                                    <MaterialCommunityIcons
                                        name="account-circle"
                                        size={40}
                                        color="white"
                                    />
                                </View>
                                <View style={styles.accountOptionInfo}>
                                    <Text style={[styles.accountOptionName, { color: Colors.text }]}>
                                        {account.name}
                                    </Text>
                                    <Text style={[styles.accountOptionEmail, { color: Colors.text, opacity: 0.6 }]}>
                                        {account.email}
                                    </Text>
                                    {account.broker && (
                                        <Text style={[styles.accountOptionBroker, { color: Colors.text, opacity: 0.5 }]}>
                                            {account.broker}
                                        </Text>
                                    )}
                                </View>
                                {activeAccount.id === account.id && (
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
        switchAccountButton: {
            flexDirection: 'row',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 8,
            paddingVertical: 10,
            borderRadius: 10,
            borderWidth: 1,
        },
        switchAccountText: {
            fontSize: 12,
            fontWeight: '700',
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
        accountOptionBroker: {
            fontSize: 10,
            fontWeight: '500',
        },
    });