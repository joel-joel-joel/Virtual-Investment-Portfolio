package com.joelcode.personalinvestmentportfoliotracker.services.news;

import com.joelcode.personalinvestmentportfoliotracker.dto.marketaux.MarketAuxArticleDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.marketaux.MarketAuxNewsResponseDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.news.NewsArticleDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketAuxApiClientImpl implements MarketAuxApiClient {

    @Value("${marketaux.api.key}")
    private String apiKey;

    @Value("${marketaux.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public MarketAuxApiClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<NewsArticleDTO> getNewsByIndustries(String[] industries, int limit) {
        try {
            String industriesParam = String.join(",", industries);
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/news/all")
                    .queryParam("industries", industriesParam)
                    .queryParam("limit", limit)
                    .queryParam("api_token", apiKey)
                    .toUriString();

            MarketAuxNewsResponseDTO response = restTemplate.getForObject(url, MarketAuxNewsResponseDTO.class);
            return mapArticlesToNewsDTO(response);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch news from MarketAux API", e);
        }
    }

    @Override
    public List<NewsArticleDTO> getAllNews(int limit) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/news/all")
                    .queryParam("limit", limit)
                    .queryParam("api_token", apiKey)
                    .toUriString();

            MarketAuxNewsResponseDTO response = restTemplate.getForObject(url, MarketAuxNewsResponseDTO.class);
            return mapArticlesToNewsDTO(response);
        } catch (RestClientException e) {
            throw new RuntimeException("Failed to fetch news from MarketAux API", e);
        }
    }

    @Override
    public List<NewsArticleDTO> getNewsBySector(String sector, int limit) {
        try {
            // Search for news by raw Finnhub sector/industry name
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/news/all")
                    .queryParam("industries", sector)
                    .queryParam("limit", limit)
                    .queryParam("api_token", apiKey)
                    .toUriString();

            MarketAuxNewsResponseDTO response = restTemplate.getForObject(url, MarketAuxNewsResponseDTO.class);
            return mapArticlesToNewsDTO(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch news by sector: " + sector, e);
        }
    }

    private List<NewsArticleDTO> mapArticlesToNewsDTO(MarketAuxNewsResponseDTO response) {
        List<NewsArticleDTO> newsArticles = new ArrayList<>();

        if (response == null || response.getData() == null) {
            return newsArticles;
        }

        for (MarketAuxArticleDTO article : response.getData()) {
            String sector = getPrimarySectorFromArticle(article);
            NewsArticleDTO newsArticle = new NewsArticleDTO(
                    sector,
                    article.getTitle(),
                    article.getDescription(),
                    article.getUrl(),
                    article.getPublishedOn(),
                    article.getImageUrl()
            );
            newsArticles.add(newsArticle);
        }

        return newsArticles;
    }

    /**
     * Get primary industry/sector from article
     * Returns raw Finnhub industry name (not mapped to limited categories)
     */
    private String getPrimarySectorFromArticle(MarketAuxArticleDTO article) {
        if (article.getIndustries() != null && article.getIndustries().length > 0) {
            // Return the first industry as-is from Finnhub
            String industry = article.getIndustries()[0];
            return (industry != null && !industry.isBlank()) ? industry : "Other";
        }
        return "Other";
    }
}
