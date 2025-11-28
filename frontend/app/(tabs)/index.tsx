import React from "react";
import { View, Text, StyleSheet, Image } from "react-native";
import { useColorScheme } from "react-native";
import { getThemeColors } from "../../src/constants/colors";
import { MaterialCommunityIcons } from "@expo/vector-icons";
import { icons } from "@/src/constants/icons";

export default function HomeScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    return (
        <View style={[styles.container, { backgroundColor: Colors.background }]}>

            {/* Top bar */}
            <View style={styles.topBar}>
                {/* Center: logo + title */}
                <View style={styles.centerGroup}>
                    <Image
                        source={icons.pegasus}
                        style={[styles.icon, { tintColor: Colors.tint }]}
                    />
                    <Text style={[styles.title, { color: Colors.tint }]}>
                        Pegasus
                    </Text>
                </View>

                {/* Right settings icon */}
                <MaterialCommunityIcons
                    name="dots-vertical"
                    size={28}
                    color={Colors.tint}
                    style={styles.rightIcon}
                />
            </View>

            {/* Dashboard */}
            <View style={styles.DashboardWrapper}>
                <View style={[styles.dashboard, { backgroundColor: Colors.card, borderColor: Colors.border }]}>
                    <Text style={[styles.dashboardtitle, { color: Colors.text }]}>
                        Welcome to Pegasus!
                    </Text>
                    <Text style={[styles.subtitle, { color: Colors.text }]}>
                        Cash and Holdings
                    </Text>
                </View>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 24,
    },
    topBar: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center", // center the logo + title
        marginTop: 0,
        position: "relative",     // allow absolute positioning for right icon
    },
    centerGroup: {
        flexDirection: "row",
        alignItems: "center",
    },
    icon: {
        width: 95,
        height: 95,
        resizeMode: "contain",
    },
    title: {
        fontSize: 30,
        fontWeight: "800",
        marginLeft: -30, // no spacing, attached to logo
        marginRight: 40,
    },
    rightIcon: {
        position: "absolute",
        right: 0,
    },
    DashboardWrapper: {
        marginTop: -10,
        justifyContent: "center",
        alignItems: "center",
    },
    subtitle: {
        fontSize: 12,
        marginTop: 5,
        opacity: 0.7,
    },
    dashboard: {
        width: "100%",
        padding: 20,
        borderRadius: 12,
        borderWidth: 1,
        height: 300,
    },
    dashboardtitle: {
        fontSize: 16,
        fontWeight: "bold",
    },
});
