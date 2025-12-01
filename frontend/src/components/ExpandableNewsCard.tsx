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
    Dimensions,
} from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { getThemeColors } from '../constants/colors';
import {Colors} from "@/constants/theme";

interface NewsItem {
    id: number;
    title: string;
    description: string;
    image: NodeRequire;
    content: string;
}

interface ExpandableNewsCardProps {
    news: NewsItem[];
}

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
        <View style={styles.container}>
            {/* News List */}
            <View style={styles.newsListWrapper}>
                <Text style={[styles.newsHeader, { color: Colors.text }]}>
                    Market News
                </Text>

                {news.map((item) => (
                    <TouchableOpacity
                        key={item.id}
                        onPress={() => openNewsDetail(item)}
                        style={[
                            styles.newsCard,
                            {
                                backgroundColor: Colors.card,
                                borderColor: Colors.border,
                            },
                        ]}
                        activeOpacity={0.7}
                    >
                        <Image
                            source={item.image as any}
                            style={styles.newsImage}
                            resizeMode="cover"
                        />
                        <View style={styles.newsCardContent}>
                            <Text
                                style={[styles.newsTitle, { color: "#266EF1" }]}
                                numberOfLines={2}
                            >
                                {item.title}
                            </Text>
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
                            color={Colors.tint}
                        />
                    </TouchableOpacity>
                ))}
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
                                source={activeNews.image as any}
                                style={styles.expandedImage}
                                resizeMode="cover"
                            />
                        )}

                        {/* Content */}
                        <ScrollView style={styles.expandedContent}>
                            {activeNews && (
                                <>
                                    <Text style={[styles.expandedTitle, { color: Colors.text }]}>
                                        {activeNews.title}
                                    </Text>

                                    <Text
                                        style={[
                                            styles.expandedDescription,
                                            { color: Colors.text, opacity: 0.7 },
                                        ]}
                                    >
                                        {activeNews.description}
                                    </Text>

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
    },
    newsListWrapper: {
        marginTop: 20,
    },
    newsHeader: {
        fontSize: 16,
        fontWeight: '700',
        marginBottom: 12,
        marginLeft: 20,
    },
    newsCard: {
        flexDirection: 'row',
        borderRadius: 12,
        borderWidth: 1,
        padding: 12,
        marginBottom: 12,
        alignItems: 'center',
        gap: 12,
    },
    newsImage: {
        width: 60,
        height: 60,
        borderRadius: 8,
    },
    newsCardContent: {
        flex: 1,
    },
    newsTitle: {
        fontSize: 14,
        fontWeight: '600',
        marginBottom: 4,
    },
    newsDescription: {
        fontSize: 12,
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
    expandedTitle: {
        fontSize: 18,
        fontWeight: '800',
        marginBottom: 8,
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