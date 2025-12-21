import {View, useColorScheme, ScrollView} from 'react-native';
import { useTheme } from '@/src/context/ThemeContext';
import { HeaderSection } from '@/src/components/home/HeaderSection';
import ProfileScreenComponent from '@/src/components/profile/ProfileScreen';



export default function WatchList() {
    const {Colors} = useTheme();

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                <ProfileScreenComponent/>
            </ScrollView>
        </View>
    );
}