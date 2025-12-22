import React from 'react';
import { View, Text, StyleSheet, useColorScheme, ScrollView, TouchableOpacity } from 'react-native';
import { useTheme } from '@/src/context/ThemeContext';
import { HeaderSection } from '@/src/screens/tabs/home/HeaderSection';
import TransactionHistory from '@/src/screens/transaction/TransactionHistory';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import type { RootStackParamList } from '@/src/navigation';

export default function TransactionHistoryPage() {
    const {Colors} = useTheme();
    const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

    const handleGoBack = () => {
        navigation.goBack();
    };

    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                {/* Header with Back Button */}
                <View style={[styles.topBar, { backgroundColor: Colors.background }]}>
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
                    <View style={styles.headerSpacer}>
                        <HeaderSection />
                    </View>
                </View>
                <Text style={styles.headerTitle}>
                    Transaction History
                </Text>


                {/* Transaction History Component */}
                <TransactionHistory showHeader={true} />
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 20,
        gap: 12,
    },
    topBar: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 0,
        paddingBottom: 0,
    },
    backButton: {
        width: 44,
        height: 44,
        borderRadius: 10,
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
        marginTop: -10,
    },
    headerSpacer: {
        marginLeft: 90,
    },
    headerTitleContainer: {
        flex: 1,
    },
    headerTitle: {
        fontSize: 28, fontWeight: "800", fontStyle: "italic", marginLeft: 10,
        marginBottom: 15, marginTop: -20
    },
});
