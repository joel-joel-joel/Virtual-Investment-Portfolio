import React, { useState } from 'react';
import {
    View,
    Text,
    StyleSheet,
    Image,
    TouchableOpacity,
    Modal,
    ScrollView,
    Animated,
    useColorScheme,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '../../constants/colors';

import { NewsArticleDTO } from '../../types/api';

interface NewsItem {
    id: number;
    title: string;
    description: string;
    image: NodeRequire | string;
    content: string;
    sector: string;
}

interface ExpandableNewsCardProps {
    news: NewsItem[];
}

// Sector color mapping
const sectorColors = {
    "Technology": { color: "#0369A1" },
    "Semiconductors": { color: "#B45309" },
    "FinTech": { color: "#15803D" },
    "Consumer/Tech": { color: "#6D28D9" },
    "Healthcare": { color: "#BE123C"},
    "Markets": { color: "#7C3AED" },
};

const SectorBadge = ({ sector }: { sector: string }) => {
    const sectorColor = sectorColors[sector as keyof typeof sectorColors] || sectorColors["Technology"];

    return (
        <View style={[styles.sectorBadge, { backgroundColor: "white" }]}>
            <MaterialCommunityIcons
                name="tag"
                size={12}
                color={sectorColor.color}
                style={{ marginRight: 4 }}
            />
            <Text style={[styles.sectorBadgeText, { color: sectorColor.color }]}>
                {sector}
            </Text>
        </View>
    );
};

export const ExpandableNewsCard: React.FC<ExpandableNewsCardProps> = ({ news }) => {
    const [activeNews, setActiveNews] = useState<NewsItem | null>(null);
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const scaleAnim = React.useRef(new Animated.Value(0)).current;

    const openNewsDetail = (item: NewsItem) => {
        setActiveNews(item);
        Animated.spring(scaleAnim, {
            toValue: 1,
            useNativeDriver: true,
        }).start();
    };

    const closeNewsDetail = () => {
        Animated.timing(scaleAnim, {
            toValue: 0,
            duration: 200,
            useNativeDriver: true,
        }).start(() => setActiveNews(null));
    };

    return (
        <View style={[styles.container, {backgroundColor: Colors.card}]}>
            <View style={styles.newsListWrapper}>
                <View style={[styles.header, { borderBottomColor: Colors.card }]}>
                    <View>
                        <Text style={[styles.newsHeader, { color: Colors.text }]}>
                            Market News
                        </Text>
                        <Text style={[styles.subtitle, { color: Colors.text, opacity: 0.6 }]}>
                            Based on your watchlist & holdings
                        </Text>
                    </View>
                    <MaterialCommunityIcons
                        name="newspaper-variant-multiple-outline"
                        size={28}
                        color={Colors.tint}
                        style={{ opacity: 0.7 }}
                    />
                </View>
                {/* Flat list of news with colored sector badges */}
                {news.map((item) => {
                    // Get sector colors, fallback to Technology if undefined
                    const sectorTheme = sectorColors[item.sector as keyof typeof sectorColors] || sectorColors["Technology"];

                    return (
                        <TouchableOpacity
                            key={item.id}
                            onPress={() => openNewsDetail(item)}
                            style={[
                                styles.newsCard,
                                {
                                    backgroundColor: "white", // Card background based on sector
                                    borderColor: sectorTheme.color, // Optional: border accent
                                },
                            ]}
                            activeOpacity={0.7}
                        >
                            <Image
                                source={typeof item.image === 'string' ? { uri: item.image } : item.image}
                                style={styles.newsImage}
                                resizeMode="cover"
                            />
                            <View style={styles.newsCardContent}>
                                <View style={styles.newsCardHeader}>
                                    <Text
                                        style={[styles.newsTitle, { color: "black" }]} // Title uses sector color
                                        numberOfLines={2}
                                    >
                                        {item.title}
                                    </Text>
                                </View>

                                {/* Sector Badge (color-coded) */}
                                <SectorBadge sector={item.sector} />

                                <Text
                                    style={[styles.newsDescription, { color: Colors.text, opacity: 0.7 }]}
                                    numberOfLines={1}
                                >
                                    {item.description}
                                </Text>
                            </View>
                            <MaterialCommunityIcons
                                name="chevron-right"
                                size={20}
                                color={sectorTheme.color} // chevron matches sector
                            />
                        </TouchableOpacity>
                    );
                })}
            </View>

            {/* Expanded Detail Modal */}
            <Modal
                visible={activeNews !== null}
                transparent={true}
                animationType="fade"
                onRequestClose={closeNewsDetail}
            >
                <View style={styles.modalOverlay}>
                    <Animated.View
                        style={[
                            styles.expandedCard,
                            {
                                backgroundColor: Colors.background,
                                transform: [{ scale: scaleAnim }],
                            },
                        ]}
                    >
                        {/* Close Button */}
                        <TouchableOpacity
                            style={styles.closeButton}
                            onPress={closeNewsDetail}
                        >
                            <MaterialCommunityIcons
                                name="close"
                                size={24}
                                color={Colors.text}
                            />
                        </TouchableOpacity>

                        {/* Image */}
                        {activeNews && (
                            <Image
                                source={typeof activeNews.image === 'string' ? { uri: activeNews.image } : activeNews.image}
                                style={styles.expandedImage}
                                resizeMode="cover"
                            />
                        )}

                        {/* Content */}
                        <ScrollView style={styles.expandedContent}>
                            {activeNews && (
                                <>
                                    <Text style={[styles.expandedTitle, { color: "#266EF1" }]}>
                                        {activeNews.title}
                                    </Text>

                                    <SectorBadge sector={activeNews.sector} />

                                    <View style={styles.contentDivider} />

                                    <Text style={[styles.expandedContentText, { color: Colors.text }]}>
                                        {activeNews.content}
                                    </Text>
                                </>
                            )}
                        </ScrollView>

                        {/* Action Button */}
                        <TouchableOpacity
                            style={[styles.actionButton, { backgroundColor: Colors.tint }]}
                            onPress={closeNewsDetail}
                        >
                            <Text style={styles.actionButtonText}>Read More</Text>
                        </TouchableOpacity>
                    </Animated.View>
                </View>
            </Modal>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        width: '100%',
        paddingHorizontal: 16,
        paddingBottom: 24,
        borderRadius: 20,
        marginTop: 10,
    },
    newsListWrapper: {
        marginTop: 15,
    },
    newsHeader: {
        fontSize: 20,
        fontWeight: '700',
        marginBottom: 12,
        marginLeft: -12,
        fontStyle: "italic",
    },
    subtitle: {
        fontSize: 12,
        marginTop: -10,
        marginRight: -8,
        marginLeft: -12,
    },
    sectorGroupHeader: {
        fontSize: 12,
        fontWeight: '700',
        textTransform: 'uppercase',
        letterSpacing: 0.5,
        marginLeft: 22,
        marginTop: 16,
        marginBottom: 8,
        opacity: 0.8,
    },
    newsCard: {
        flexDirection: 'row',
        borderRadius: 12,
        padding: 12,
        marginBottom: 12,
        alignItems: 'center',
        gap: 12,
        width: '100%',          // wider card
        alignSelf: 'center',   // center horizontally
    },
    header: {
        flexDirection: "row",
        justifyContent: "space-between",
        alignItems: "flex-start",
        paddingHorizontal: 24,
        paddingBottom: 12,
        borderBottomWidth: 1,
        marginBottom: 16,
    },
    newsImage: {
        width: 60,
        height: 60,
        borderRadius: 8,
    },
    newsCardContent: {
        flex: 1,
        gap: 6,
    },
    newsCardHeader: {
        marginBottom: 2,
    },
    newsTitle: {
        fontSize: 12,
        fontWeight: '600',
        marginBottom: -2,
    },
    sectorBadge: {
        flexDirection: 'row',
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
        alignItems: 'center',
        alignSelf: 'flex-start',
    },
    sectorBadgeText: {
        fontSize: 10,
        fontWeight: '600',
    },
    newsDescription: {
        fontSize: 10,
    },
    modalOverlay: {
        flex: 1,
        backgroundColor: 'rgba(0, 0, 0, 0.5)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    expandedCard: {
        width: '85%',
        maxHeight: '80%',
        borderRadius: 16,
        overflow: 'hidden',
    },
    closeButton: {
        position: 'absolute',
        top: 16,
        right: 16,
        zIndex: 10,
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        borderRadius: 20,
        padding: 8,
    },
    expandedImage: {
        width: '100%',
        height: 250,
    },
    expandedContent: {
        padding: 16,
        maxHeight: 300,
    },
    expandedHeaderRow: {
        flexDirection: 'row',
        alignItems: 'flex-start',
        marginBottom: 8,
    },
    expandedTitle: {
        fontSize: 18,
        fontWeight: '800',
        marginBottom: 8,
    },
    expandedBadgeRow: {
        marginBottom: 12,
    },
    expandedDescription: {
        fontSize: 14,
        marginBottom: 12,
    },
    contentDivider: {
        height: 1,
        backgroundColor: 'rgba(0, 0, 0, 0.1)',
        marginVertical: 12,
    },
    expandedContentText: {
        fontSize: 13,
        lineHeight: 20,
        marginBottom: 16,
    },
    actionButton: {
        margin: 16,
        paddingVertical: 12,
        borderRadius: 12,
        alignItems: 'center',
    },
    actionButtonText: {
        color: 'white',
        fontSize: 16,
        fontWeight: '600',
    },
});