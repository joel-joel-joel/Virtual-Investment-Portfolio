package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.news.NewsArticleDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.news.NewsApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
@Profile("!test")
public class NewsController {


    @Autowired
    private NewsApiClient newsApiClient;

    /**
     * Get all news with optional limit
     * @param limit Maximum number of articles to return
     * @return List of news articles
     */
    @GetMapping
    public ResponseEntity<List<NewsArticleDTO>> getAllNews(
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        try {
            List<NewsArticleDTO> news = newsApiClient.getAllNews(limit);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get news by sector/industry from Finnhub
     * Supports any raw Finnhub sector name (e.g., "Technology", "Financial Services", etc.)
     * @param sector Finnhub sector/industry name
     * @param limit Maximum number of articles to return
     * @return List of news articles for the specified sector
     */
    @GetMapping("/sector/{sector}")
    public ResponseEntity<List<NewsArticleDTO>> getNewsBySector(
            @PathVariable String sector,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        try {
            List<NewsArticleDTO> news = newsApiClient.getNewsBySector(sector, limit);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    /**
     * Get news by multiple sectors/industries
     * Supports any raw Finnhub sector names
     * @param sectors Comma-separated list of Finnhub sector/industry names
     * @param limit Maximum number of articles to return
     * @return List of news articles from all specified sectors
     */
    @GetMapping("/sectors")
    public ResponseEntity<List<NewsArticleDTO>> getNewsByMultipleSectors(
            @RequestParam(value = "sectors") String[] sectors,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        try {
            // Pass raw sectors directly to API (no mapping needed anymore)
            List<NewsArticleDTO> news = newsApiClient.getNewsByIndustries(
                    sectors,
                    limit
            );
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    /**
     * Get news by stock symbol
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param limit Maximum number of articles to return
     * @return List of news articles for the specified symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<NewsArticleDTO>> getNewsBySymbol(
            @PathVariable String symbol,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        try {
            List<NewsArticleDTO> news = newsApiClient.getNewsBySymbol(symbol, limit);
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    /**
     * Get all unique sectors/industries from recent news
     * Returns the set of all sectors currently being used in news articles
     * Useful for dynamically discovering available sectors
     * @param limit Number of news articles to scan for sectors
     * @return List of unique sector names
     */
    @GetMapping("/sectors/unique")
    public ResponseEntity<List<String>> getUniqueSectors(
            @RequestParam(value = "limit", defaultValue = "500") int limit
    ) {
        try {
            List<NewsArticleDTO> allNews = newsApiClient.getAllNews(limit);

            // Extract unique sectors
            List<String> uniqueSectors = new ArrayList<>();
            for (NewsArticleDTO article : allNews) {
                String sector = article.getSector();
                if (sector != null && !sector.isBlank() && !uniqueSectors.contains(sector)) {
                    uniqueSectors.add(sector);
                }
            }

            // Sort for consistent output
            uniqueSectors.sort(String::compareTo);
            return ResponseEntity.ok(uniqueSectors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }
}
