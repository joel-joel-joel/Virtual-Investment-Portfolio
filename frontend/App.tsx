import 'react-native-gesture-handler';
import React, { useEffect } from 'react';
import { StatusBar } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { AuthProvider } from '@/src/context/AuthContext';
import { initializeSectorColors } from '@/src/services/sectorColorService';
import Navigation from '@/src/navigation';

export default function App() {
  useEffect(() => {
    initializeSectorColors().catch(error =>
      console.error('Failed to initialize sector colors:', error)
    );
  }, []);

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <AuthProvider>
          <StatusBar barStyle="dark-content" backgroundColor="white" />
          <Navigation />
        </AuthProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
