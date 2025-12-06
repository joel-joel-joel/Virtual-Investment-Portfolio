import React from 'react';
import { View, StyleSheet, useColorScheme } from 'react-native';
import { Stack, useRouter, useSegments } from 'expo-router';
import { Colors } from '../../src/constants/colors';
import { HapticTab, TabBarIcon } from '../../src/components/ui/hapticTab';

export default function TabLayout() {
    const router = useRouter();
    const segments = useSegments();
    const colorScheme = useColorScheme();
    const themeColors = Colors[colorScheme ?? 'light'];

    const tabs = [
        { name: 'index', icon: 'home', title: 'Home' },
        { name: 'portfolio', icon: 'portfolio', title: 'Portfolio' },
        { name: 'search', icon: 'search', title: 'Search' },
        { name: 'watchlist', icon: 'watchlist', title: 'Watchlist' },
        { name: 'profile', icon: 'profile', title: 'Profile' },
    ];

    const currentTab = segments[1] || 'index';

    return (
        <View style={{ flex: 1 }}>
            <Stack
                screenOptions={{
                    headerShown: false,
                }}
            >
                <Stack.Screen name="index" />
                <Stack.Screen name="portfolio" />
                <Stack.Screen name="search" />
                <Stack.Screen name="watchlist" />
                <Stack.Screen name="profile" />
            </Stack>

            <View
                style={[
                    styles.tabBar,
                    {
                        backgroundColor: themeColors.background,
                        borderTopColor: themeColors.border,
                    },
                ]}
            >
                {tabs.map((tab) => (
                    <HapticTab
                        key={tab.name}
                        onPress={() => router.push(`/(tabs)/${tab.name}` as any)}
                        style={styles.tabButton}
                    >
                        <TabBarIcon
                            focused={currentTab === tab.name}
                            icon={tab.icon as any}
                            title={tab.title}
                            color={themeColors.tint}
                            inactiveColor={themeColors.tabIconDefault}
                        />
                    </HapticTab>
                ))}
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    tabBar: {
        flexDirection: 'row',
        borderTopWidth: 1,
        height: 60,
        justifyContent: 'space-around',
        alignItems: 'center',
        paddingHorizontal: 8,
    },
    tabButton: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
    },
});
