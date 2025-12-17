import React, { useEffect, useState } from "react";
import { View, ScrollView, useColorScheme } from "react-native";
import { getThemeColors } from "@/src/constants/colors";
import { HeaderSection } from "@/src/components/home/HeaderSection";
import { Dashboard } from "@/src/components/home/Dashboard";
import { WatchlistHighlights } from "@/src/components/home/WatchlistHighlights";
import { TopMovers } from "@/src/components/home/TopMovers";
import { ExpandableNewsCard } from '@/src/components/home/ExpandableNewsCard';
import { SuggestedForYou } from '@/src/components/home/SuggestedForYou';
import { StockTicker } from "@/src/components/home/StockTicker";
import { EarningsCalendar } from "@/src/components/home/EarningsCalender";
import { QuickActionsRow } from "@/src/components/home/QuickActions";
import { getAllNewsSafe } from '@/src/services/newsService';
import { getAccountHoldings } from '@/src/services/portfolioService';
import { useAuth } from '@/src/context/AuthContext';
import type { NewsArticleDTO, HoldingDTO } from '@/src/types/api';

export default function HomeScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);
    const { activeAccount } = useAuth();

    const [newsItems, setNewsItems] = useState<any[]>([]);
    const [loadingNews, setLoadingNews] = useState(true);
    const [holdings, setHoldings] = useState<HoldingDTO[]>([]);
    const [loadingHoldings, setLoadingHoldings] = useState(true);

    // Fetch holdings for TopMovers
    useEffect(() => {
        const fetchHoldings = async () => {
            if (!activeAccount?.accountId) {
                setLoadingHoldings(false);
                return;
            }

            try {
                setLoadingHoldings(true);
                const data = await getAccountHoldings(activeAccount.accountId);
                setHoldings(data || []);
            } catch (error) {
                console.error('Failed to fetch holdings:', error);
                setHoldings([]);
            } finally {
                setLoadingHoldings(false);
            }
        };

        fetchHoldings();
    }, [activeAccount?.accountId]);

    // Fetch news from API
    useEffect(() => {
        const fetchNews = async () => {
            try {
                const newsData = await getAllNewsSafe(10);
                const formattedNews = newsData.map((article: NewsArticleDTO, index: number) => ({
                    id: index + 1,
                    title: article.title,
                    description: article.summary,
                    image: article.imageUrl || require('../../assets/images/apple.png'),
                    content: article.summary,
                    sector: article.sector
                }));
                setNewsItems(formattedNews);
            } catch (error) {
                console.error('Failed to fetch news:', error);
                // Fallback to mock data if API fails
                setNewsItems([
                    {
                        id: 1,
                        title: "Fed Signals Rate Cuts Ahead",
                        description: "Federal Reserve indicates potential interest rate reductions...",
                        image: require('../../assets/images/apple.png'),
                        content: "The Federal Reserve has signaled that interest rate cuts may be coming...",
                        sector: "Markets"
                    },
                    {
                        id: 2,
                        title: "Tech Stocks Rally on AI News",
                        description: "Technology sector leads market as AI developments continue...",
                        image: require('../../assets/images/apple.png'),
                        content: "Major technology companies announced breakthrough developments...",
                        sector: "Technology"
                    },
                    {
                        id: 3,
                        title: "Earnings Season Exceeds Expectations",
                        description: "Companies report stronger than expected Q3 earnings...",
                        image: require('../../assets/images/apple.png'),
                        content: "Corporate earnings for the third quarter have exceeded analyst expectations...",
                        sector: "Healthcare"
                    },
                ]);
            } finally {
                setLoadingNews(false);
            }
        };

        fetchNews();
    }, []);

    const [refreshTicker, setRefreshTicker] = useState(0);

    const handleRefreshTicker = () => {
        setRefreshTicker(prev => prev + 1); // This will reload new randomized stocks
    };


    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                <Dashboard />
                <QuickActionsRow />
                <StockTicker refreshTrigger={refreshTicker} />
                <WatchlistHighlights />
                <TopMovers holdings={holdings} />
                <EarningsCalendar holdings={holdings} />
                <ExpandableNewsCard news={newsItems} />
                <SuggestedForYou />
            </ScrollView>
        </View>
    );
}