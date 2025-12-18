package com.joelcode.personalinvestmentportfoliotracker.services.news;

import com.joelcode.personalinvestmentportfoliotracker.dto.news.NewsArticleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooNewsItem;
import com.joelcode.personalinvestmentportfoliotracker.services.finnhub.FinnhubApiClient;
import com.joelcode.personalinvestmentportfoliotracker.services.yahoofinance.YahooFinanceApiClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class YahooFinanceNewsService implements NewsApiClient {

    private final YahooFinanceApiClient yahooFinanceApiClient;
    private final FinnhubApiClient finnhubApiClient;

    public YahooFinanceNewsService(YahooFinanceApiClient yahooFinanceApiClient,
                                   FinnhubApiClient finnhubApiClient) {
        this.yahooFinanceApiClient = yahooFinanceApiClient;
        this.finnhubApiClient = finnhubApiClient;
    }

    @Override
    public List<NewsArticleDTO> getAllNews(int limit) {
        try {
            List<YahooNewsItem> newsItems = yahooFinanceApiClient.getGeneralNews(limit);
            return convertToNewsArticleDTOs(newsItems);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all news", e);
        }
    }

    @Override
    public List<NewsArticleDTO> getNewsBySymbol(String symbol, int limit) {
        try {
            List<YahooNewsItem> newsItems = yahooFinanceApiClient.getNewsForSymbol(symbol, limit);
            return convertToNewsArticleDTOs(newsItems, symbol);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news for symbol: " + symbol, e);
        }
    }

    @Override
    public List<NewsArticleDTO> getNewsBySector(String sector, int limit) {
        try {
            // For sector filtering, we aggregate news from multiple symbols in that sector
            // This is a workaround since Yahoo Finance doesn't support direct sector filtering
            List<NewsArticleDTO> allNews = new ArrayList<>();

            // Try to get some common symbols for the sector and aggregate their news
            List<String> sectorSymbols = getSectorSymbols(sector);

            if (sectorSymbols.isEmpty()) {
                // Fallback: search for the sector directly
                return convertToNewsArticleDTOs(
                    yahooFinanceApiClient.search(sector).getNews() != null ?
                    yahooFinanceApiClient.search(sector).getNews() : new ArrayList<>()
                );
            }

            // Aggregate news from sector symbols
            for (String symbol : sectorSymbols) {
                try {
                    List<NewsArticleDTO> symbolNews = getNewsBySymbol(symbol, 5);
                    allNews.addAll(symbolNews);
                } catch (Exception e) {
                    // Continue with next symbol if one fails
                }
            }

            // Remove duplicates and limit results
            return allNews.stream()
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news for sector: " + sector, e);
        }
    }

    @Override
    public List<NewsArticleDTO> getNewsByIndustries(String[] industries, int limit) {
        try {
            List<NewsArticleDTO> allNews = new ArrayList<>();
            int newsPerIndustry = limit / Math.max(industries.length, 1);

            for (String industry : industries) {
                try {
                    List<NewsArticleDTO> industryNews = getNewsBySector(industry, newsPerIndustry);
                    allNews.addAll(industryNews);
                } catch (Exception e) {
                    // Continue with next industry if one fails
                }
            }

            return allNews.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news for industries", e);
        }
    }

    private List<NewsArticleDTO> convertToNewsArticleDTOs(List<YahooNewsItem> newsItems) {
        return convertToNewsArticleDTOs(newsItems, null);
    }

    private List<NewsArticleDTO> convertToNewsArticleDTOs(List<YahooNewsItem> newsItems, String symbol) {
        List<NewsArticleDTO> result = new ArrayList<>();

        if (newsItems == null) {
            return result;
        }

        for (YahooNewsItem item : newsItems) {
            try {
                String sector = "General";
                if (symbol != null) {
                    sector = lookupSectorForSymbol(symbol);
                }

                String imageUrl = extractThumbnailUrl(item.getThumbnail());
                String publishedAt = convertTimestamp(item.getProviderPublishTime());

                NewsArticleDTO dto = new NewsArticleDTO(
                        sector,
                        item.getTitle(),
                        "",  // Yahoo search doesn't provide description/summary
                        item.getLink(),
                        publishedAt,
                        imageUrl
                );
                result.add(dto);
            } catch (Exception e) {
                // Skip articles that fail to convert
            }
        }

        return result;
    }

    private String lookupSectorForSymbol(String symbol) {
        try {
            var profile = finnhubApiClient.getCompanyProfile(symbol);
            return profile.getIndustry() != null ?
                   profile.getIndustry() : "Other";
        } catch (Exception e) {
            return "Other";
        }
    }

    private List<String> getSectorSymbols(String sector) {
        // Hardcoded mapping of sectors to popular symbols
        // In production, this could be fetched from a database or cached from Finnhub
        return switch (sector.toLowerCase()) {
            case "technology" -> List.of("AAPL", "MSFT", "GOOGL", "NVDA", "META");
            case "healthcare" -> List.of("JNJ", "UNH", "PFE", "ABBV", "TMO");
            case "financials" -> List.of("JPM", "BAC", "WFC", "GS", "MS");
            case "industrials" -> List.of("BA", "CAT", "GE", "MMM", "HON");
            case "consumer discretionary" -> List.of("AMZN", "TSLA", "HD", "MCD", "NKE");
            case "consumer staples" -> List.of("PG", "KO", "WMT", "PEP", "MO");
            case "energy" -> List.of("XOM", "CVX", "COP", "SLB", "MPC");
            case "utilities" -> List.of("NEE", "DUK", "SO", "EXC", "AWK");
            case "real estate" -> List.of("AMT", "PLD", "CCI", "EQIX", "WELL");
            case "materials" -> List.of("LIN", "APD", "ROK", "SHW", "NEM");
            case "communication services" -> List.of("DIS", "DISH", "VZ", "T", "CMCSA");
            default -> List.of();
        };
    }

    private String extractThumbnailUrl(Object thumbnail) {
        if (thumbnail == null) {
            return null;
        }

        try {
            var thumbObj = (com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooNewsThumbnail) thumbnail;
            if (thumbObj.getResolutions() == null || thumbObj.getResolutions().isEmpty()) {
                return null;
            }

            // Get largest resolution by width
            return thumbObj.getResolutions().stream()
                    .max(Comparator.comparing(r -> r.getWidth() != null ? r.getWidth() : 0))
                    .map(com.joelcode.personalinvestmentportfoliotracker.dto.yahoofinance.YahooThumbnailResolution::getUrl)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String convertTimestamp(Long timestamp) {
        if (timestamp == null) {
            return null;
        }

        try {
            Instant instant = Instant.ofEpochSecond(timestamp);
            return instant.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
