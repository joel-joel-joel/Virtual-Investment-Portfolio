import React, { createContext, useContext, useState, useEffect } from 'react';
import { useColorScheme } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { getThemeColors } from '@/src/constants/colors';

type ThemeType = 'light' | 'dark' | 'system';

interface ThemeContextType {
    theme: ThemeType;
    setTheme: (theme: ThemeType) => Promise<void>;
    effectiveTheme: 'light' | 'dark'; // Actual theme after resolving 'system'
    Colors: ReturnType<typeof getThemeColors>;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const deviceColorScheme = useColorScheme();
    const [theme, setThemeState] = useState<ThemeType>('system');
    const [isLoading, setIsLoading] = useState(true);

    // Load theme preference from AsyncStorage on mount
    useEffect(() => {
        const loadTheme = async () => {
            try {
                const savedTheme = await AsyncStorage.getItem('appTheme');
                if (savedTheme && ['light', 'dark', 'system'].includes(savedTheme)) {
                    setThemeState(savedTheme as ThemeType);
                }
            } catch (error) {
                console.error('Failed to load theme preference:', error);
            } finally {
                setIsLoading(false);
            }
        };
        loadTheme();
    }, []);

    // Determine effective theme (resolve 'system' to actual device theme)
    const effectiveTheme: 'light' | 'dark' =
        theme === 'system'
            ? (deviceColorScheme ?? 'light')
            : theme;

    // Get colors based on effective theme
    const Colors = getThemeColors(effectiveTheme);

    // Update theme and persist to AsyncStorage
    const setTheme = async (newTheme: ThemeType) => {
        try {
            setThemeState(newTheme);
            await AsyncStorage.setItem('appTheme', newTheme);
            console.log('✅ Theme changed to:', newTheme);
        } catch (error) {
            console.error('Failed to save theme preference:', error);
        }
    };

    // Show loading state while theme is being loaded
    if (isLoading) {
        return null; // Or a splash screen
    }

    return (
        <ThemeContext.Provider
            value={{
                theme,
                setTheme,
                effectiveTheme,
                Colors,
            }}
        >
            {children}
        </ThemeContext.Provider>
    );
};

// Custom hook to use theme context
export const useTheme = (): ThemeContextType => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within ThemeProvider');
    }
    return context;
};