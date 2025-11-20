package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.SearchDTO;
import com.joelcode.personalinvestmentportfoliotracker.services.utility.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    SearchService searchService;

    /**
     * Search across stocks, accounts, and holdings
     */
    @GetMapping
    public ResponseEntity<SearchDTO> search(
            @RequestParam("query") String query,
            @RequestParam(value = "userId", required = false) UUID userId
    ) {
        SearchDTO results = searchService.search(query, userId);
        return ResponseEntity.ok(results);
    }
}
