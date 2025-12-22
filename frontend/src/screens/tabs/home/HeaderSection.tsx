import React from "react";
import { View, Image, StyleSheet } from "react-native";
import { icons } from "../../../constants/icons";
import { useTheme } from '@/src/context/ThemeContext';


export const HeaderSection = () => {
    const {Colors} = useTheme();

    return (
        <View style={[styles.topBar, { backgroundColor: Colors.background }]}>
            <View style={styles.centerGroup}>
                <Image
                    source={icons.pegasus}
                    style={[styles.icon, { tintColor: Colors.tint }]}
                />
            </View>
        </View>
    );
};

const styles = StyleSheet.create({
    topBar: {
        flexDirection: "row",
        alignItems: "center",
        justifyContent: "center",
        marginTop: -20,
        position: "relative",
    },
    centerGroup: {
        flexDirection: "row",
        alignItems: "center",
        maxWidth: 80,
        justifyContent: "center",
    },
    icon: {
        width: 125,
        height: 125,
        resizeMode: "contain",
    },
    rightIcon: {
        position: 'absolute',
        right: 0,
        marginTop: 5,
    },

});
