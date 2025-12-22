import React from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    Animated,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
import { useTheme } from '@/src/context/ThemeContext';

interface QuickAction {
    id: string;
    label: string;
    icon: string;
    onPress: () => void;
}

interface QuickActionsRowProps {
    actions?: QuickAction[];
}

// We'll create this inside the component to access navigation
const createDefaultActions = (navigation: NativeStackNavigationProp<RootStackParamList>): QuickAction[] => [
    {
        id: 'add-to-wallet',
        label: 'Add to Wallet',
        icon: 'plus-circle',
        onPress: () => {
            // Navigate to profile tab and open wallet modal
            navigation.navigate('MainTabs', {
                screen: 'Profile',
                params: { openWallet: true }
            });
        },
    },
    {
        id: 'watchlist',
        label: 'Add to Watchlist',
        icon: 'heart-outline',
        onPress: () => navigation.navigate('MainTabs', { screen: 'Watchlist' }),
    },
    {
        id: 'search',
        label: 'Search Stock',
        icon: 'magnify',
        onPress: () => navigation.navigate('MainTabs', { screen: 'Search' }),
    },
    {
        id: 'analytics',
        label: 'Analytics',
        icon: 'chart-line',
        onPress: () => navigation.navigate('MainTabs', { screen: 'Portfolio' }),
    },
];

const QuickActionButton = ({
                               action,
                               Colors,
                               scaleAnim,
                           }: {
    action: QuickAction;
    Colors: any;
    scaleAnim: Animated.Value;
}) => {
    const animScale = React.useRef(new Animated.Value(1)).current;

    const handlePressIn = () => {
        Animated.spring(animScale, {
            toValue: 0.92,
            useNativeDriver: true,
        }).start();
    };

    const handlePressOut = () => {
        Animated.spring(animScale, {
            toValue: 1,
            useNativeDriver: true,
        }).start();
        action.onPress();
    };

    return (
        <Animated.View style={{ transform: [{ scale: animScale }] }}>
            <TouchableOpacity
                onPressIn={handlePressIn}
                onPressOut={handlePressOut}
                style={[
                    styles.actionButton,
                    {
                        backgroundColor: Colors.card,
                        borderColor: Colors.tint,
                        shadowColor: Colors.tint,
                    },
                ]}
                activeOpacity={0.8}
            >
                {/* LEFT ICON */}
                <View style={styles.leftIconWrapper}>
                    <MaterialCommunityIcons
                        name={action.icon as any}
                        size={16}
                        color={Colors.tint}
                    />
                </View>

                {/* CENTERED TEXT */}
                <View style={styles.centerTextWrapper}>
                    <Text style={[styles.actionLabel, { color: Colors.text }]}>
                        {action.label}
                    </Text>
                </View>

                {/* RIGHT SPACER TO KEEP TEXT CENTERED */}
                <View style={styles.rightSpacer} />
            </TouchableOpacity>


        </Animated.View>
    );
};

export const QuickActionsRow: React.FC<QuickActionsRowProps> = ({
                                                                    actions,
                                                                }) => {
    const { Colors } = useTheme();
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();
    const scaleAnim = React.useRef(new Animated.Value(1)).current;

    const defaultActions = createDefaultActions(navigation);
    const finalActions = actions || defaultActions;

    return (
        <View style={styles.wrapper}>
            <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                scrollEventThrottle={16}
                style={styles.container}
                contentContainerStyle={[
                    styles.contentContainer,
                    { paddingHorizontal: 24 }
                ]}
            >
                {finalActions.map((action) => (
                    <QuickActionButton
                        key={action.id}
                        action={action}
                        Colors = {Colors}
                        scaleAnim={scaleAnim}
                    />
                ))}
            </ScrollView>
        </View>
    );
};

const styles = StyleSheet.create({
    wrapper: {
        marginVertical: 16,
    },
    container: {
        flexGrow: 0,
    },
    contentContainer: {
        gap: 10,
    },
    actionButton: {
        width: 150,
        height: 44,
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "space-between",
        borderWidth: 1,
        borderRadius: 10,
        paddingHorizontal: 10,
        shadowOffset: { width: 0, height: 1 },
        shadowOpacity: 0.08,
        shadowRadius: 2,
        elevation: 2,
    },

    leftIconWrapper: {
        width: 20,
        alignItems: "flex-start",
    },

    centerTextWrapper: {
        flex: 1,
        alignItems: "center",
    },

    rightSpacer: {
        width: 5,      // same width as the icon wrapper to keep text centered
    },

    actionLabel: {
        fontSize: 12,
        fontWeight: "600",
    },

});