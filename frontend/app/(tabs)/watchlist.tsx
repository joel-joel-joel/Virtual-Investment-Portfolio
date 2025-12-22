import {View, useColorScheme, ScrollView} from 'react-native';
import { useTheme } from '@/src/context/ThemeContext';
import { HeaderSection } from '@/src/screens/tabs/home/HeaderSection';
import WatchListScreen from '@/src/screens/tabs/watchlist/WatchlistScreen';


export default function WatchList() {
    const {Colors} = useTheme();

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                <WatchListScreen/>
            </ScrollView>
        </View>
    );
}