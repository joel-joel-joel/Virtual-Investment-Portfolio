import 'react-native-gesture-handler';
import React, { useEffect } from 'react';
import { StatusBar, View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { AuthProvider } from '@/src/context/AuthContext';
import { initializeSectorColors } from '@/src/services/sectorColorService';
import Navigation from '@/src/navigation';
import { ThemeProvider } from '@/src/context/ThemeContext';
import { useTheme } from '@/src/context/ThemeContext';

// Inner component that uses theme (must be inside ThemeProvider)
function AppContent() {
    const { Colors, effectiveTheme } = useTheme();

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background }}>
            <StatusBar
                barStyle={effectiveTheme === 'dark' ? 'light-content' : 'dark-content'}
                backgroundColor={Colors.background}
            />
            <Navigation />
        </View>
    );
}

export default function App() {
    useEffect(() => {
        initializeSectorColors().catch(error =>
            console.error('Failed to initialize sector Colors:', error)
        );
    }, []);

    return (
        <GestureHandlerRootView style={{ flex: 1 }}>
            <SafeAreaProvider>
                <AuthProvider>
                    <ThemeProvider>
                        <AppContent />
                    </ThemeProvider>
                </AuthProvider>
            </SafeAreaProvider>
        </GestureHandlerRootView>
    );
}