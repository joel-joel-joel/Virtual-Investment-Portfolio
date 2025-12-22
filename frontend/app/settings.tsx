import React from 'react';
import { View, ScrollView, useColorScheme, TouchableOpacity, StyleSheet } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useTheme } from '@/src/context/ThemeContext';
import SettingsScreen from "@/src/screens/settings/SettingsScreen";
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';
import {HeaderSection} from "@/src/screens/tabs/home/HeaderSection";


export default function SettingsPage() {
    const {Colors} = useTheme();
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    const handleGoBack = () => {
        navigation.goBack();
    };

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background }}>

            <ScrollView
                showsVerticalScrollIndicator={false}
                contentContainerStyle={{ paddingHorizontal: 24, paddingVertical: 16 }}
            >
                <View style={[styles.headerContainer, { backgroundColor: Colors.background }]}>
                    {/* Back button (left) */}
                    <TouchableOpacity
                        onPress={handleGoBack}
                        style={[styles.backButton, { backgroundColor: Colors.card }]}
                    >
                        <MaterialCommunityIcons
                            name="chevron-left"
                            size={28}
                            color={Colors.text}
                        />
                    </TouchableOpacity>

                    {/* Centered header */}
                    <View style={styles.centerHeader}>
                        <HeaderSection />
                    </View>
                </View>

                <SettingsScreen/>
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
    headerContainer: {
        position: 'relative',      // 🔑 allows absolute children
        height: 56,
        justifyContent: 'center',
        marginBottom: 20,
        marginTop: 30,
    },
    backButton: {
        position: 'absolute',      // 🔑 pin to left
        left: 24,
        width: 44,
        height: 44,
        borderRadius: 10,
        marginLeft: -25,
        alignItems: 'center',
        justifyContent: 'center',
    },
    centerHeader: {
        alignItems: 'center',
        marginTop: 10,// 🔑 true center
    },
});

