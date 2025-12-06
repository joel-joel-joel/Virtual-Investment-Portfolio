import React from 'react';
import { View, Text, StyleSheet, useColorScheme, ScrollView, TouchableOpacity } from 'react-native';
import { getThemeColors } from '@/src/constants/colors';
import { HeaderSection } from '@/src/components/home/HeaderSection';
import TransactionHistory from '@/src/components/transaction/TransactionHistory';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

export default function TransactionHistoryPage() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const router = useRouter();

    const handleGoBack = () => {
        router.back();
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
        gap: 12,
    },
    topBar: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingTop: 12,
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
        flex: 1,
        marginLeft: -32,
        marginTop: 0,
    },
    headerTitleContainer: {
        flex: 1,
    },
    headerTitle: {
        fontSize: 28, fontWeight: "800", fontStyle: "italic", marginLeft: 10,
        marginBottom: 15, marginTop: -20
    },
});
