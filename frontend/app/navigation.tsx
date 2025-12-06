import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useColorScheme } from 'react-native';
import { getThemeColors } from '../src/constants/colors';

// Screen Imports
import HomeScreen from './(tabs)/index';
import PortfolioScreen from './(tabs)/portfolio';
import SearchScreen from './(tabs)/search';
import WatchlistScreen from './(tabs)/watchlist';
import ProfileScreen from './(tabs)/profile';

// Modal/Stack Imports
import Settings from './(tabs)/settings';

const Stack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();

// Define Navigation Params
export type RootStackParamList = {
    MainTabs: undefined;
    Settings: undefined;
};

// Tab Navigator
function TabNavigator() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

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
                    backgroundColor: Colors.card,
                    borderTopColor: Colors.border,
                    borderTopWidth: 1,
                    paddingBottom: 8,
                    paddingTop: 8,
                },
                tabBarLabelStyle: {
                    fontSize: 11,
                    fontWeight: '600',
                    marginTop: 4,
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
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    return (
        <Stack.Navigator
            screenOptions={{
                headerStyle: {
                    backgroundColor: Colors.card,
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
            <Stack.Group>
                <Stack.Screen
                    name="MainTabs"
                    component={TabNavigator}
                    options={{
                        headerShown: false,
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
                    component={Settings}
                    options={{
                        title: 'Settings',
                        headerBackVisible: false,
                    }}
                />
            </Stack.Group>
        </Stack.Navigator>
    );
}

// Navigation Container
export function Navigation() {
    return (
        <NavigationContainer>
            <RootStack />
        </NavigationContainer>
    );
}

export default Navigation;