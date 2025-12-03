import React from "react";
import {View, ScrollView, useColorScheme, StyleSheet} from "react-native";
import { getThemeColors } from "@/src/constants/colors";
import { HeaderSection } from "@/src/components/home/HeaderSection";
import { Dashboard } from "@/src/components/home/Dashboard";
import { WatchlistHighlights } from "@/src/components/home/WatchlistHighlights";
import { TopMovers } from "@/src/components/home/TopMovers";
import { ExpandableNewsCard } from '@/src/components/home/ExpandableNewsCard';
import { SuggestedForYou } from '@/src/components/home/SuggestedForYou';
import {StockTicker} from "@/src/components/home/StockTicker";
import { EarningsCalendar } from "@/src/components/home/EarningsCalender";
import {QuickActionsRow} from "@/src/components/home/QuickActions";


export default function HomeScreen() {
    const colorScheme = useColorScheme();
    const Colors = getThemeColors(colorScheme);

    const newsItems = [
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
    ];

    const watchlistStocks = [
        { id: 1, symbol: "AAPL", price: "A$150.25", change: "+2.5%", sector: "Technology" },
        { id: 2, symbol: "MSFT", price: "A$380.50", change: "+1.8%", sector: "Technology" },
        { id: 3, symbol: "GOOGL", price: "A$140.75", change: "+3.2%", sector: "Technology" },
        { id: 4, symbol: "TSLA", price: "A$245.30", change: "-1.5%", sector: "Consumer/Tech" },
        { id: 5, symbol: "AMZN", price: "A$170.90", change: "+2.1%", sector: "Consumer/Tech" },
    ];


    return (
        <View style={{ flex: 1, backgroundColor: Colors.background, padding: 24 }}>
            <ScrollView showsVerticalScrollIndicator={false}>
                <HeaderSection />
                <Dashboard />
                <QuickActionsRow />
                <StockTicker stocks={watchlistStocks} />
                <WatchlistHighlights />
                <TopMovers />
                <EarningsCalendar />
                <ExpandableNewsCard news={newsItems} />
                <SuggestedForYou />
            </ScrollView>
        </View>
    );
}


