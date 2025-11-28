import { Tabs } from 'expo-router';
import React from 'react';
import {
    useColorScheme,
    Image,
    Text,
    View,
    StyleSheet,
    ViewStyle, TouchableOpacity, GestureResponderEvent, StyleProp,
} from 'react-native';
import { Colors } from '../../src/constants/colors';
import { icons } from '../../src/constants/icons';
import {BottomTabBarButtonProps} from "@react-navigation/bottom-tabs";


interface HapticTabProps {
    children: React.ReactNode;
    onPress?: () => void;
    style?: ViewStyle | ViewStyle[];
}

export const HapticTab: React.FC<BottomTabBarButtonProps> = ({children, onPress, style,}) => {
    return (
        <TouchableOpacity
            onPress={onPress as (event: GestureResponderEvent) => void}
            style={style as StyleProp<ViewStyle>}
            activeOpacity={0.7}
        >
            {children}
        </TouchableOpacity>
    );
};

// Tab Bar Icon
interface TabBarIconProps {
    focused: boolean;
    icon: keyof typeof icons;
    title: string;
    color: string;
}

const TabBarIcon: React.FC<TabBarIconProps> = ({ focused, icon, title, color }) => {
    if (focused) {
        return (
            <View style={[styles.activeBackground, { backgroundColor: color + '20' }]}>
                <Image source={icons[icon]} style={[styles.icon, { tintColor: color }]} />
                <Text style={[styles.activeTitle, { color }]}>{title}</Text>
            </View>
        );
    } else {
        return (
            <View style={styles.inactiveContainer}>
                <Image source={icons[icon]} style={[styles.icon, { tintColor: color }]} />
            </View>
        );
    }
};


// Tab Layout
export default function TabLayout() {
    const colorScheme = useColorScheme();
    const themeColors = Colors[colorScheme ?? 'light'];

    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarActiveTintColor: themeColors.tint,
                tabBarButton: (props) => <HapticTab {...props} />,
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Home',
                    tabBarIcon: ({ focused }) => (
                        <TabBarIcon focused={focused} icon="home" title="Home" color={themeColors.tint} />
                    ),
                }}
            />
            <Tabs.Screen
                name="portfolio"
                options={{
                    title: 'Portfolio',
                    tabBarIcon: ({ focused }) => (
                        <TabBarIcon
                            focused={focused}
                            icon="portfolio"
                            title="Portfolio"
                            color={themeColors.tint}
                        />
                    ),
                }}
            />
            <Tabs.Screen
                name="search"
                options={{
                    title: 'Search',
                    tabBarIcon: ({ focused }) => (
                        <TabBarIcon focused={focused} icon="search" title="Search" color={themeColors.tint} />
                    ),
                }}
            />
            <Tabs.Screen
                name="watchlist"
                options={{
                    title: 'Watchlist',
                    tabBarIcon: ({ focused }) => (
                        <TabBarIcon
                            focused={focused}
                            icon="watchlist"
                            title="Watchlist"
                            color={themeColors.tint}
                        />
                    ),
                }}
            />
            <Tabs.Screen
                name="profile"
                options={{
                    title: 'Profile',
                    tabBarIcon: ({ focused }) => (
                        <TabBarIcon
                            focused={focused}
                            icon="profile"
                            title="Profile"
                            color={themeColors.tint}
                        />
                    ),
                }}
            />
        </Tabs>
    );
}

// Styles
const styles = StyleSheet.create({
    activeBackground: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        minWidth: 112,
        minHeight: 56,
        borderRadius: 28,
        marginTop: 4,
        paddingHorizontal: 12,
    },
    inactiveContainer: {
        marginTop: 4,
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 28,
        padding: 8,
    },
    icon: {
        width: 24,
        height: 24,
        resizeMode: 'contain',
    },
    activeTitle: {
        marginLeft: 8,
        fontSize: 14,
        fontWeight: '600',
    },
});
