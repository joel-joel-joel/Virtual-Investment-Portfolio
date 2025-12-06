import React, { useEffect } from "react";
import { Slot, useRouter, useSegments } from "expo-router";
import { StatusBar, View, ActivityIndicator, Text, StyleSheet } from "react-native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { GestureHandlerRootView } from "react-native-gesture-handler";
import { AuthProvider, useAuth } from "../src/context/AuthContext";

// Loading screen component
function LoadingScreen() {
    return (
        <View style={styles.loadingContainer}>
            <ActivityIndicator size="large" color="#007AFF" />
            <Text style={styles.loadingText}>Loading...</Text>
        </View>
    );
}

// Protected routes component - handles conditional rendering
function ProtectedRoutes() {
    const { isAuthenticated, isLoading } = useAuth();
    const segments = useSegments();
    const router = useRouter();

    useEffect(() => {
        if (isLoading) return; // Don't navigate while checking auth state

        const inAuthGroup = segments[0] === 'auth';
        const inTabsGroup = segments[0] === '(tabs)';

        if (isAuthenticated) {
            // User is authenticated
            if (inAuthGroup) {
                // If on auth screen (login), redirect to main app
                router.replace('/(tabs)');
            }
        } else {
            // User is not authenticated
            if (!inAuthGroup) {
                // If not on auth screen, redirect to login
                router.replace('/auth/login');
            }
        }
    }, [isAuthenticated, isLoading, segments]);

    // Show loading screen while checking authentication
    if (isLoading) {
        return <LoadingScreen />;
    }

    return <Slot />;
}

export default function RootLayout() {
    return (
        <GestureHandlerRootView style={{ flex: 1 }}>
            <SafeAreaProvider>
                <AuthProvider>
                    <StatusBar barStyle="dark-content" backgroundColor="white" />
                    <ProtectedRoutes />
                </AuthProvider>
            </SafeAreaProvider>
        </GestureHandlerRootView>
    );
}

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
