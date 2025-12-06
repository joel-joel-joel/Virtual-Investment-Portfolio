import React from 'react';
import {
    View,
    Text,
    StyleSheet,
    ScrollView,
    TouchableOpacity,
    useColorScheme,
    Animated,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '../../constants/colors';
import { useRouter } from 'expo-router';

interface QuickAction {
    id: string;
    label: string;
    icon: string;
    onPress: () => void;
}

interface QuickActionsRowProps {
    actions?: QuickAction[];
}

// We'll create this inside the component to access router
const createDefaultActions = (router: any): QuickAction[] => [
    {
        id: 'add-transaction',
        label: 'Add Transaction',
        icon: 'plus-circle',
        onPress: () => router.push('/transaction/buy'),
    },
    {
        id: 'watchlist',
        label: 'Add to Watchlist',
        icon: 'heart-outline',
        onPress: () => router.push('/(tabs)/watchlist'),
    },
    {
        id: 'search',
        label: 'Search Stock',
        icon: 'magnify',
        onPress: () => router.push('/(tabs)/search'),
    },
    {
        id: 'analytics',
        label: 'Analytics',
        icon: 'chart-line',
        onPress: () => router.push('/(tabs)/portfolio'),
    },
];

const QuickActionButton = ({
                               action,
                               colors,
                               scaleAnim,
                           }: {
    action: QuickAction;
    colors: any;
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
                        backgroundColor: colors.card,
                        borderColor: colors.tint,
                        shadowColor: colors.tint,
                    },
                ]}
                activeOpacity={0.8}
            >
                {/* LEFT ICON */}
                <View style={styles.leftIconWrapper}>
                    <MaterialCommunityIcons
                        name={action.icon as any}
                        size={16}
                        color={colors.tint}
                    />
                </View>

                {/* CENTERED TEXT */}
                <View style={styles.centerTextWrapper}>
                    <Text style={[styles.actionLabel, { color: colors.text }]}>
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
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const router = useRouter();
    const scaleAnim = React.useRef(new Animated.Value(1)).current;

    const defaultActions = createDefaultActions(router);
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
                        colors={Colors}
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