import React from 'react';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';

// Screen Imports
import HomeScreen from '@/app/(tabs)/index';
import PortfolioScreen from '@/app/(tabs)/portfolio';
import SearchScreen from '@/app/(tabs)/search';
import WatchlistScreen from '@/app/(tabs)/watchlist';
import ProfileScreen from '@/app/(tabs)/profile';

// Modal/Stack Imports
import SettingsScreen from '../src/components/settings/SettingsScreen';
import StockTickerPage from './stock/[ticker]';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

// Define Navigation Params
export type RootStackParamList = {
    MainTabs: undefined;
    Settings: undefined;
    StockTicker: {
        stock: {
            symbol: string;
            name: string;
            price: number;
            change: number;
            changePercent: number;
            sector: string;
            marketCap: string;
            peRatio: string;
            dividend: string;
            dayHigh: number;
            dayLow: number;
            yearHigh: number;
            yearLow: number;
            description: string;
            employees: string;
            founded: string;
            website: string;
            nextEarningsDate: string;
            nextDividendDate: string;
            earningsPerShare: string;
        }
    };
};

declare global {
    namespace ReactNavigation {
        interface RootParamList extends RootStackParamList {}
    }
}

// Tab Navigator
function TabNavigator() {
    const { Colors } = useTheme();

    return (
        <Tab.Navigator
            screenOptions={({ route }) => ({
                headerShown: false,
                tabBarIcon: ({ focused, color, size }) => {
                    let iconName;

                    if (route.name === 'Home') {
                        iconName = focused ? 'home' : 'home-outline';
                    } else if (route.name === 'Portfolio') {
                        iconName = focused ? 'briefcase' : 'briefcase-outline';
                    } else if (route.name === 'Search') {
                        iconName = focused ? 'magnify' : 'magnify';
                    } else if (route.name === 'Watchlist') {
                        iconName = focused ? 'heart' : 'heart-outline';
                    } else if (route.name === 'Profile') {
                        iconName = focused ? 'account' : 'account-outline';
                    }

                    return (
                        <MaterialCommunityIcons
                            name={iconName as any}
                            size={size}
                            color={color}
                        />
                    );
                },
                tabBarActiveTintColor: Colors.tint,
                tabBarInactiveTintColor: Colors.text + '99',
                tabBarStyle: {
                    backgroundColor: Colors.background,
                    borderTopColor: Colors.border,
                    borderTopWidth: 1,
                    paddingBottom: 8,
                    paddingTop: 8,
                },
                tabBarLabelStyle: {
                    fontSize: 11,
                    fontWeight: '600',
                    marginTop: 4,
                    color: Colors.text,
                },
            })}
        >
            <Tab.Screen
                name="Home"
                component={HomeScreen}
                options={{
                    tabBarLabel: 'Home',
                }}
            />
            <Tab.Screen
                name="Portfolio"
                component={PortfolioScreen}
                options={{
                    tabBarLabel: 'Portfolio',
                }}
            />
            <Tab.Screen
                name="Search"
                component={SearchScreen}
                options={{
                    tabBarLabel: 'Search',
                }}
            />
            <Tab.Screen
                name="Watchlist"
                component={WatchlistScreen}
                options={{
                    tabBarLabel: 'Watchlist',
                }}
            />
            <Tab.Screen
                name="Profile"
                component={ProfileScreen}
                options={{
                    tabBarLabel: 'Profile',
                }}
            />
        </Tab.Navigator>
    );
}

// Root Stack Navigator
function RootStack() {
    const { Colors } = useTheme();

    return (
        <Stack.Navigator
            screenOptions={{
                headerStyle: {
                    backgroundColor: Colors.background,
                },
                headerTintColor: Colors.text,
                headerTitleStyle: {
                    fontWeight: '800',
                    fontSize: 18,
                },
                headerBackVisible: false,
                contentStyle: {
                    backgroundColor: Colors.background,
                },
            }}
        >
            {/* Main Tab Navigator */}
            <Stack.Group
                screenOptions={{
                    headerShown: false,
                }}
            >
                <Stack.Screen
                    name="MainTabs"
                    component={TabNavigator}
                />
            </Stack.Group>

            {/* Stock Ticker - Can be accessed from anywhere */}
            <Stack.Group
                screenOptions={{
                    presentation: 'card',
                }}
            >
                <Stack.Screen
                    name="StockTicker"
                    component={StockTickerPage}
                    options={{
                        title: 'Stock Details',
                        headerShown: true,
                    }}
                />
            </Stack.Group>

            {/* Modal Stack - Settings */}
            <Stack.Group
                screenOptions={{
                    presentation: 'card',
                }}
            >
                <Stack.Screen
                    name="Settings"
                    component={SettingsScreen}
                    options={{
                        title: 'Settings',
                    }}
                />
            </Stack.Group>
        </Stack.Navigator>
    );
}

// Navigation Container
function Navigation() {
    const { Colors, effectiveTheme } = useTheme();

    console.log('🎨 Navigation Colors:', Colors);
    console.log('🎨 effectiveTheme:', effectiveTheme);
    console.log('🎨 Colors.background:', Colors.background);

    const navigationTheme = effectiveTheme === 'dark'
        ? {
            ...DarkTheme,
            colors: {
                ...DarkTheme.colors,
                background: Colors.background,
                card: Colors.background, // ✅ Set card to background too
                text: Colors.text,
                border: Colors.border,
                primary: Colors.tint,
                notification: Colors.tint,
            },
        }
        : {
            ...DefaultTheme,
            colors: {
                ...DefaultTheme.colors,
                background: Colors.background,
                card: Colors.background, // ✅ Set card to background too
                text: Colors.text,
                border: Colors.border,
                primary: Colors.tint,
                notification: Colors.tint,
            },
        };

    return (
        <NavigationContainer theme={navigationTheme}>
            <RootStack />
        </NavigationContainer>
    );
}

export default Navigation;