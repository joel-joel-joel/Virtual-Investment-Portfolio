package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.SearchDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.utility.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    private SearchController searchController;

    @BeforeEach
    void setUp() {
        searchController = new SearchController();
        searchController.searchService = searchService;
    }

    @Test
    void testSearch_WithQuery_Success() {
        // Arrange
        String query = "AAPL";
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, null)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchService, times(1)).search(query, null);
    }

    @Test
    void testSearch_WithQueryAndUserId_Success() {
        // Arrange
        String query = "MSFT";
        UUID userId = UUID.randomUUID();
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, userId)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchService, times(1)).search(query, userId);
    }

    @Test
    void testSearch_EmptyQuery() {
        // Arrange
        String query = "";
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, null)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchService, times(1)).search(query, null);
    }

    @Test
    void testSearch_SpecialCharactersInQuery() {
        // Arrange
        String query = "TEST-123";
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, null)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchService, times(1)).search(query, null);
    }

    @Test
    void testSearch_LongQuery() {
        // Arrange
        String query = "This is a very long search query that spans multiple words";
        UUID userId = UUID.randomUUID();
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, userId)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchService, times(1)).search(query, userId);
    }

    @Test
    void testSearch_MultipleSearches_DifferentQueries() {
        // Arrange
        String query1 = "AAPL";
        String query2 = "GOOGL";
        UUID userId = UUID.randomUUID();
        SearchDTO results1 = new SearchDTO();
        SearchDTO results2 = new SearchDTO();
        when(searchService.search(query1, userId)).thenReturn(results1);
        when(searchService.search(query2, userId)).thenReturn(results2);

        // Act
        ResponseEntity<SearchDTO> response1 = searchController.search(query1, userId);
        ResponseEntity<SearchDTO> response2 = searchController.search(query2, userId);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(searchService, times(1)).search(query1, userId);
        verify(searchService, times(1)).search(query2, userId);
    }

    @Test
    void testSearch_WithoutUserId_Optional() {
        // Arrange
        String query = "Checking";
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, null)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchService, times(1)).search(query, null);
    }

    @Test
    void testSearch_CaseInsensitiveQuery() {
        // Arrange
        String query = "aapl";
        SearchDTO results = new SearchDTO();
        when(searchService.search(query, null)).thenReturn(results);

        // Act
        ResponseEntity<SearchDTO> response = searchController.search(query, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchService, times(1)).search(query, null);
    }
}