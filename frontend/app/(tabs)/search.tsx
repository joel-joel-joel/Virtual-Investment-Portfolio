import React from 'react';
import { View, ScrollView, useColorScheme } from 'react-native';
import { getThemeColors } from '../../src/constants/colors';
import { HeaderSection } from "@/src/components/home/HeaderSection";
import SearchScreenComponent from "@/src/components/search/SearchScreen";


export default function SearchScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                <SearchScreenComponent />
            </ScrollView>
        </View>
    );
}