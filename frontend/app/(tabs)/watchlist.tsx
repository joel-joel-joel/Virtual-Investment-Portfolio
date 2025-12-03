import {View, Text, StyleSheet, useColorScheme, ScrollView} from 'react-native';
import { getThemeColors } from '@/src/constants/colors';
import { HeaderSection } from '@/src/components/home/HeaderSection';
import { HoldingsList } from '@/src/components/portfolio/HoldingsList';
import { AllocationOverview } from '@/src/components/portfolio/AllocationOverview';


export default function PortfolioScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />

            </ScrollView>
        </View>
    );
}