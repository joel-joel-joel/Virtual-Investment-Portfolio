import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Switch,
    Alert,
    Modal,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '@/src/constants/colors';
import {HeaderSection} from "@/src/components/home/HeaderSection";
import {router} from "expo-router";

interface NotificationSettings {
    priceAlerts: boolean;
    portfolioUpdates: boolean;
    marketNews: boolean;
    dividendNotifications: boolean;
    earningSeason: boolean;
}

interface AppSettings {
    theme: 'light' | 'dark' | 'system';
    currency: 'AUD' | 'USD' | 'EUR';
    language: 'English' | 'Spanish' | 'French';
    dateFormat: 'DD/MM/YYYY' | 'MM/DD/YYYY' | 'YYYY-MM-DD';
}

const SettingItemToggle = ({
                               icon,
                               label,
                               description,
                               value,
                               onToggle,
                               colors,
                           }: {
    icon: string;
    label: string;
    description: string;
    value: boolean;
    onToggle: (value: boolean) => void;
    colors: any;
}) => {
    return (
        <View style={[styles.settingItem, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <View style={styles.settingLeft}>
                <View style={[styles.iconContainer, { backgroundColor: colors.tint + '15' }]}>
                    <MaterialCommunityIcons
                        name={icon as any}
                        size={20}
                        color={colors.tint}
                    />
                </View>
                <View style={styles.settingText}>
                    <Text style={[styles.settingLabel, { color: colors.text }]}>
                        {label}
                    </Text>
                    <Text style={[styles.settingDescription, { color: colors.text, opacity: 0.6 }]}>
                        {description}
                    </Text>
                </View>
            </View>
            <Switch
                value={value}
                onValueChange={onToggle}
                trackColor={{ false: '#E0E0E0', true: colors.tint + '50' }}
                thumbColor={value ? colors.tint : '#F0F0F0'}
            />
        </View>
    );
};

const SettingItemSelect = ({
                               icon,
                               label,
                               description,
                               value,
                               onPress,
                               colors,
                           }: {
    icon: string;
    label: string;
    description: string;
    value: string;
    onPress: () => void;
    colors: any;
}) => {
    return (
        <TouchableOpacity
            onPress={onPress}
            style={[styles.settingItem, { backgroundColor: colors.card, borderColor: colors.border }]}
            activeOpacity={0.7}
        >
            <View style={styles.settingLeft}>
                <View style={[styles.iconContainer, { backgroundColor: colors.tint + '15' }]}>
                    <MaterialCommunityIcons
                        name={icon as any}
                        size={20}
                        color={colors.tint}
                    />
                </View>
                <View style={styles.settingText}>
                    <Text style={[styles.settingLabel, { color: colors.text }]}>
                        {label}
                    </Text>
                    <Text style={[styles.settingDescription, { color: colors.text, opacity: 0.6 }]}>
                        {description}
                    </Text>
                </View>
            </View>
            <View style={styles.settingRight}>
                <Text style={[styles.settingValue, { color: colors.tint }]}>
                    {value}
                </Text>
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

const SelectionModal = ({
                            visible,
                            title,
                            options,
                            selectedValue,
                            onSelect,
                            onClose,
                            colors,
                        }: {
    visible: boolean;
    title: string;
    options: string[];
    selectedValue: string;
    onSelect: (value: string) => void;
    onClose: () => void;
    colors: any;
}) => {
    return (
        <Modal
            visible={visible}
            transparent={true}
            animationType="slide"
            onRequestClose={onClose}
        >
            <View style={[styles.modalOverlay, { backgroundColor: colors.background }]}>
                <View style={[styles.modalHeader, { borderBottomColor: colors.border }]}>
                    <Text style={[styles.modalTitle, { color: colors.text }]}>
                        {title}
                    </Text>
                    <TouchableOpacity
                        onPress={onClose}
                        style={styles.closeButton}
                    >
                        <MaterialCommunityIcons
                            name="close"
                            size={24}
                            color={colors.text}
                        />
                    </TouchableOpacity>
                </View>

                <View style={styles.modalContent}>
                    {options.map((option) => (
                        <TouchableOpacity
                            key={option}
                            onPress={() => {
                                onSelect(option);
                                onClose();
                            }}
                            style={[
                                styles.optionItem,
                                {
                                    backgroundColor: colors.card,
                                    borderColor: selectedValue === option ? colors.tint : colors.border,
                                    borderWidth: selectedValue === option ? 2 : 1,
                                }
                            ]}
                        >
                            <Text style={[styles.optionText, { color: colors.text }]}>
                                {option}
                            </Text>
                            {selectedValue === option && (
                                <MaterialCommunityIcons
                                    name="check-circle"
                                    size={24}
                                    color={colors.tint}
                                />
                            )}
                        </TouchableOpacity>
                    ))}
                </View>
            </View>
        </Modal>
    );
};

export default function SettingsScreen({ navigation }: { navigation?: any }) {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    // Notification Settings
    const [notifications, setNotifications] = useState<NotificationSettings>({
        priceAlerts: true,
        portfolioUpdates: true,
        marketNews: false,
        dividendNotifications: true,
        earningSeason: false,
    });

    // App Settings
    const [appSettings, setAppSettings] = useState<AppSettings>({
        theme: 'system',
        currency: 'AUD',
        language: 'English',
        dateFormat: 'DD/MM/YYYY',
    });

    // Modal States
    const [activeModal, setActiveModal] = useState<string | null>(null);

    const handleNotificationToggle = (key: keyof NotificationSettings) => {
        setNotifications(prev => ({
            ...prev,
            [key]: !prev[key],
        }));
    };

    const handleAppSettingChange = (key: keyof AppSettings, value: any) => {
        setAppSettings(prev => ({
            ...prev,
            [key]: value,
        }));
    };

    const handleGoBack = () => {
        router.back();
    };

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>
            {/* Header */}
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
            <Text style={styles.title}>
                Settings
            </Text>

            <ScrollView
                showsVerticalScrollIndicator={false}
                contentContainerStyle={styles.scrollContent}
            >
                {/* APPEARANCE SECTION */}
                <View style={styles.section}>
                    <Text style={[styles.sectionHeader, { color: Colors.text }]}>
                        Appearance
                    </Text>

                    <SettingItemSelect
                        icon="palette"
                        label="Theme"
                        description="Choose your preferred appearance"
                        value={appSettings.theme.charAt(0).toUpperCase() + appSettings.theme.slice(1)}
                        onPress={() => setActiveModal('theme')}
                        colors={Colors}
                    />
                </View>

                {/* GENERAL SECTION */}
                <View style={styles.section}>
                    <Text style={[styles.sectionHeader, { color: Colors.text }]}>
                        General
                    </Text>

                    <SettingItemSelect
                        icon="currency-usd"
                        label="Default Currency"
                        description="Select your preferred currency"
                        value={appSettings.currency}
                        onPress={() => setActiveModal('currency')}
                        colors={Colors}
                    />

                    <SettingItemSelect
                        icon="translate"
                        label="Language"
                        description="Choose your preferred language"
                        value={appSettings.language}
                        onPress={() => setActiveModal('language')}
                        colors={Colors}
                    />

                    <SettingItemSelect
                        icon="calendar-outline"
                        label="Date Format"
                        description="Select date display format"
                        value={appSettings.dateFormat}
                        onPress={() => setActiveModal('dateFormat')}
                        colors={Colors}
                    />
                </View>

                {/* NOTIFICATIONS SECTION */}
                <View style={styles.section}>
                    <Text style={[styles.sectionHeader, { color: Colors.text }]}>
                        Notifications
                    </Text>

                    <SettingItemToggle
                        icon="bell-alert-outline"
                        label="Price Alerts"
                        description="Get notified when prices change significantly"
                        value={notifications.priceAlerts}
                        onToggle={() => handleNotificationToggle('priceAlerts')}
                        colors={Colors}
                    />

                    <SettingItemToggle
                        icon="chart-line-variant"
                        label="Portfolio Updates"
                        description="Receive daily portfolio performance updates"
                        value={notifications.portfolioUpdates}
                        onToggle={() => handleNotificationToggle('portfolioUpdates')}
                        colors={Colors}
                    />

                    <SettingItemToggle
                        icon="newspaper"
                        label="Market News"
                        description="Get the latest financial news and insights"
                        value={notifications.marketNews}
                        onToggle={() => handleNotificationToggle('marketNews')}
                        colors={Colors}
                    />

                    <SettingItemToggle
                        icon="cash-multiple"
                        label="Dividend Notifications"
                        description="Be notified about dividend payments"
                        value={notifications.dividendNotifications}
                        onToggle={() => handleNotificationToggle('dividendNotifications')}
                        colors={Colors}
                    />

                    <SettingItemToggle
                        icon="calendar-check"
                        label="Earnings Season"
                        description="Get alerts during company earnings season"
                        value={notifications.earningSeason}
                        onToggle={() => handleNotificationToggle('earningSeason')}
                        colors={Colors}
                    />
                </View>

                {/* PORTFOLIO SECTION */}
                <View style={styles.section}>
                    <Text style={[styles.sectionHeader, { color: Colors.text }]}>
                        Portfolio
                    </Text>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('Recurring Investments', 'Coming soon!')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="repeat"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    Recurring Investments
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    Set up automatic investments
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('Rebalancing', 'Coming soon!')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="scale-balance"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    Rebalancing Alerts
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    Get notified when portfolio needs rebalancing
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>
                </View>

                {/* SECURITY & PRIVACY SECTION */}
                <View style={styles.section}>
                    <Text style={[styles.sectionHeader, { color: Colors.text }]}>
                        Security & Privacy
                    </Text>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('Two-Factor Authentication', 'Coming soon!')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="shield-account-outline"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    Two-Factor Authentication
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    Enhance account security
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('Privacy Settings', 'Coming soon!')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="lock-outline"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    Privacy Policy
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    View our privacy policy
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>
                </View>

                {/* ABOUT SECTION */}
                <View style={styles.section}>
                    <Text style={[styles.sectionHeader, { color: Colors.text }]}>
                        About
                    </Text>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('About Pegasus', 'Pegasus Investment Portfolio v1.0.0')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="information-outline"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    About Pegasus
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    v1.0.0
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('Terms of Service', 'Coming soon!')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="file-document-outline"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    Terms of Service
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    Read our terms
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>

                    <TouchableOpacity
                        style={[styles.settingItem, { backgroundColor: Colors.card, borderColor: Colors.border }]}
                        activeOpacity={0.7}
                        onPress={() => Alert.alert('Support', 'support@pegasusinvest.com')}
                    >
                        <View style={styles.settingLeft}>
                            <View style={[styles.iconContainer, { backgroundColor: Colors.tint + '15' }]}>
                                <MaterialCommunityIcons
                                    name="help-circle-outline"
                                    size={20}
                                    color={Colors.tint}
                                />
                            </View>
                            <View style={styles.settingText}>
                                <Text style={[styles.settingLabel, { color: Colors.text }]}>
                                    Contact Support
                                </Text>
                                <Text style={[styles.settingDescription, { color: Colors.text, opacity: 0.6 }]}>
                                    Get help and support
                                </Text>
                            </View>
                        </View>
                        <MaterialCommunityIcons
                            name="chevron-right"
                            size={20}
                            color={Colors.text}
                            style={{ opacity: 0.4 }}
                        />
                    </TouchableOpacity>
                </View>

                <View style={{ height: 24 }} />
            </ScrollView>

            {/* Selection Modals */}
            <SelectionModal
                visible={activeModal === 'theme'}
                title="Choose Theme"
                options={['Light', 'Dark', 'System']}
                selectedValue={appSettings.theme.charAt(0).toUpperCase() + appSettings.theme.slice(1)}
                onSelect={(value) => handleAppSettingChange('theme', value.toLowerCase())}
                onClose={() => setActiveModal(null)}
                colors={Colors}
            />

            <SelectionModal
                visible={activeModal === 'currency'}
                title="Choose Currency"
                options={['AUD', 'USD', 'EUR']}
                selectedValue={appSettings.currency}
                onSelect={(value) => handleAppSettingChange('currency', value)}
                onClose={() => setActiveModal(null)}
                colors={Colors}
            />

            <SelectionModal
                visible={activeModal === 'language'}
                title="Choose Language"
                options={['English', 'Spanish', 'French']}
                selectedValue={appSettings.language}
                onSelect={(value) => handleAppSettingChange('language', value)}
                onClose={() => setActiveModal(null)}
                colors={Colors}
            />

            <SelectionModal
                visible={activeModal === 'dateFormat'}
                title="Choose Date Format"
                options={['DD/MM/YYYY', 'MM/DD/YYYY', 'YYYY-MM-DD']}
                selectedValue={appSettings.dateFormat}
                onSelect={(value) => handleAppSettingChange('dateFormat', value)}
                onClose={() => setActiveModal(null)}
                colors={Colors}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        marginTop: -5,
    },
    header: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        paddingHorizontal: 24,
        paddingVertical: 8,
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
        marginLeft: -40,
        marginTop: 0,
    },
    title: {
        fontSize: 28, fontWeight: "800", fontStyle: "italic", marginLeft: 10,
        marginBottom: 0, marginTop: -10
    },
    scrollContent: {
        paddingVertical: 20,
        gap: 20,
    },
    section: {
        gap: 10,
    },
    sectionHeader: {
        fontSize: 12,
        fontWeight: '700',
        textTransform: 'uppercase',
        letterSpacing: 0.5,
        opacity: 0.7,
        marginBottom: 4,
    },
    settingItem: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
    },
    settingLeft: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 12,
        flex: 1,
    },
    iconContainer: {
        width: 40,
        height: 40,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
    },
    settingText: {
        flex: 1,
        gap: 2,
    },
    settingLabel: {
        fontSize: 13,
        fontWeight: '700',
    },
    settingDescription: {
        fontSize: 11,
        fontWeight: '500',
    },
    settingRight: {
        flexDirection: 'row',
        alignItems: 'center',
        gap: 8,
    },
    settingValue: {
        fontSize: 12,
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
        fontSize: 18,
        fontWeight: '800',
    },
    closeButton: {
        padding: 8,
    },
    modalContent: {
        paddingHorizontal: 24,
        paddingVertical: 20,
        gap: 10,
    },
    optionItem: {
        borderWidth: 1,
        borderRadius: 12,
        padding: 14,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
    },
    optionText: {
        fontSize: 13,
        fontWeight: '600',
    },
});