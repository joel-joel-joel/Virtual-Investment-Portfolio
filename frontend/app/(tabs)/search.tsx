import React from 'react';
import { View, ScrollView, useColorScheme } from 'react-native';
import { useTheme } from '@/src/context/ThemeContext';
import { HeaderSection } from "@/src/screens/tabs/home/HeaderSection";
import SearchScreenComponent from "@/src/screens/tabs/search/SearchScreen";


export default function SearchScreen() {
    const {Colors} = useTheme();

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                <SearchScreenComponent />
            </ScrollView>
        </View>
    );
}