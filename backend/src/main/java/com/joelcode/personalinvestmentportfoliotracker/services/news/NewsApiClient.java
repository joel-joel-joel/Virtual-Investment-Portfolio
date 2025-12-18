package com.joelcode.personalinvestmentportfoliotracker.services.news;

import com.joelcode.personalinvestmentportfoliotracker.dto.news.NewsArticleDTO;

import java.util.List;

public interface NewsApiClient {

    /**
     * Get news by multiple industries/sectors
     * @param industries Array of industry/sector names
     * @param limit Maximum number of articles
     * @return List of news articles
     */
    List<NewsArticleDTO> getNewsByIndustries(String[] industries, int limit);

    /**
     * Get all news with a limit
     * @param limit Maximum number of articles
     * @return List of all news articles
     */
    List<NewsArticleDTO> getAllNews(int limit);

    /**
     * Get news filtered by sector/industry
     * @param sector Sector/industry name
     * @param limit Maximum number of articles
     * @return List of news articles for the sector
     */
    List<NewsArticleDTO> getNewsBySector(String sector, int limit);

    /**
     * Get news for a specific stock symbol
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param limit Maximum number of articles
     * @return List of news articles for the symbol
     */
    List<NewsArticleDTO> getNewsBySymbol(String symbol, int limit);
}
