'use strict';

import React, { useRef, useEffect } from 'react';
import { NavigationContainer, NavigationContainerRef, CompositeNavigationProp, NavigatorScreenParams } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator, BottomTabNavigationProp } from '@react-navigation/bottom-tabs';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useColorScheme, View, ActivityIndicator, Text, StyleSheet } from 'react-native';
import { useTheme } from '@/src/context/ThemeContext';
import { useAuth } from '@/src/context/AuthContext';

// Auth Screen
import LoginScreen from '@/src/screens/auth/LoginScreen';

// Placeholder components - will create these as wrappers
// For now, import directly from app folder, will move later
import HomeScreen from '@/app/(tabs)/index';
import PortfolioScreen from '@/app/(tabs)/portfolio';
import SearchScreen from '@/app/(tabs)/search';
import WatchlistScreen from '@/app/(tabs)/watchlist';
import ProfileScreen from '@/app/(tabs)/profile';

// Other screens
import StockTickerPage from '@/app/stock/[ticker]';
import BuyTransactionPage from '@/src/screens/transaction/buy';
import SellTransactionPage from '@/src/screens/transaction/sell';
import HistoryScreen from '@/src/screens/transaction/history';
import SettingsPage from '@/app/settings';
import CreateAccountScreen from '@/src/screens/account/create';

// Navigation types - Define TabParamList first for forward reference
export type TabParamList = {
  Home: undefined;
  Portfolio: undefined;
  Search: undefined;
  Watchlist: undefined;
  Profile: {
    openWallet?: boolean;
  } | undefined;
};

export type RootStackParamList = {
  MainTabs: NavigatorScreenParams<TabParamList>;
  Login: undefined;
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
    };
  };
  BuyTransaction: {
    stock?: {
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
      stockId?: string;
    };
  };
  SellTransaction: {
    stock?: {
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
      stockId?: string;
    };
    stockId?: string;
  };
  TransactionHistory: undefined;
  Settings: undefined;
  CreateAccount: undefined;
};

declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}

const Stack = createNativeStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<TabParamList>();

// Loading screen component
function LoadingScreen() {
  return (
    <View style={styles.loadingContainer}>
      <ActivityIndicator size="large" color="#007AFF" />
      <Text style={styles.loadingText}>Loading...</Text>
    </View>
  );
}

// Tab Navigator
function TabNavigator() {
  const {Colors} = useTheme();

  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: string;

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
          } else {
            iconName = 'home';
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
  const {Colors} = useTheme();
  const { isAuthenticated, isLoading, accounts, isLoadingAccounts } = useAuth();
  const previousNeedsAccountSetup = useRef(false);

  // Check if authenticated user has no accounts (new user)
  // Only consider this true after accounts have finished loading
  const needsAccountSetup = isAuthenticated && !isLoadingAccounts && accounts.length === 0;

  // When needsAccountSetup becomes true (new user after registration), navigate to CreateAccount
  useEffect(() => {
    if (needsAccountSetup && !previousNeedsAccountSetup.current) {
      console.log('🎯 New user detected - navigating to CreateAccount');
      navigationRef.current?.navigate('CreateAccount' as never);
      previousNeedsAccountSetup.current = true;
    } else if (!needsAccountSetup && previousNeedsAccountSetup.current) {
      previousNeedsAccountSetup.current = false;
    }
  }, [needsAccountSetup]);

  if (isLoading) {
    return <LoadingScreen />;
  }

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
        headerBackVisible: true,
        contentStyle: {
          backgroundColor: Colors.background,
        },
      }}
      initialRouteName={
        isLoadingAccounts ? 'MainTabs' :
        needsAccountSetup ? 'CreateAccount' :
        isAuthenticated ? 'MainTabs' :
        'Login'
      }
    >
      {isAuthenticated ? (
        <>
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
                headerShown: false,
              }}
            />
          </Stack.Group>

          {/* Transaction Screens */}
          <Stack.Group
            screenOptions={{
              presentation: 'card',
            }}
          >
            <Stack.Screen
              name="BuyTransaction"
              component={BuyTransactionPage}
              options={{
                headerShown: false,
              }}
            />
            <Stack.Screen
              name="SellTransaction"
              component={SellTransactionPage}
              options={{
                headerShown: false,
              }}
            />
            <Stack.Screen
              name="TransactionHistory"
              component={HistoryScreen}
              options={{
                headerShown: false,
              }}
            />
          </Stack.Group>

          {/* Modal Stack - Settings and Account */}
          <Stack.Group
            screenOptions={{
              presentation: 'card',
            }}
          >
            <Stack.Screen
              name="Settings"
              component={SettingsPage}
              options={{
                headerShown: false,
              }}
            />
            <Stack.Screen
              name="CreateAccount"
              component={CreateAccountScreen}
              options={{
                headerShown: false,
              }}
            />
          </Stack.Group>
        </>
      ) : (
        // Auth Stack
        <Stack.Group
          screenOptions={{
            headerShown: false,
          }}
        >
          <Stack.Screen
            name="Login"
            component={LoginScreen}
          />
        </Stack.Group>
      )}
    </Stack.Navigator>
  );
}

// Navigation Container exported with ref
export const navigationRef = React.createRef<NavigationContainerRef<RootStackParamList>>();

// Main Navigation Component
export function Navigation() {
  return (
    <NavigationContainer ref={navigationRef}>
      <RootStack />
    </NavigationContainer>
  );
}

export default Navigation;

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    fontWeight: '600',
    color: '#666666',
  },
});
