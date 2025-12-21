import { View, useColorScheme, ScrollView } from 'react-native';
import { HeaderSection } from '@/src/components/home/HeaderSection';
import { HoldingsList } from '@/src/components/portfolio/HoldingsList';
import { AllocationOverview } from '@/src/components/portfolio/AllocationOverview';
import { useAuth } from '@/src/context/AuthContext';
import { useCallback, useState } from 'react';
import { useTheme } from '@/src/context/ThemeContext';

export default function PortfolioScreen() {
    const {Colors} = useTheme();
    const { activeAccount } = useAuth();
    const [refreshTrigger, setRefreshTrigger] = useState(0);

    // Trigger refresh for both HoldingsList and AllocationOverview
    const handleRefresh = useCallback(() => {
        setRefreshTrigger(prev => prev + 1);
    }, []);

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                {/* HoldingsList now manages its own data fetching */}
                <HoldingsList onRefresh={handleRefresh} />
                {/* AllocationOverview also manages its own data */}
                <AllocationOverview accountId={activeAccount?.accountId || ''} />
            </ScrollView>
        </View>
    );
}