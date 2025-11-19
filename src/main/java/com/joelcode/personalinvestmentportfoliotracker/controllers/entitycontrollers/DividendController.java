package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.dividend.DividendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dividends")
public class DividendController {

    @Autowired
    private DividendService dividendService;

    // Get all dividends
    @GetMapping
    public ResponseEntity<List<DividendDTO>> getAllDividends() {
        return ResponseEntity.ok(dividendService.getAllDividends());
    }

    // Get a dividend by ID
    @GetMapping("/{id}")
    public ResponseEntity<DividendDTO> getDividendById(@PathVariable UUID id) {
        DividendDTO dto = dividendService.getDividendById(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new dividend
    @PostMapping
    public ResponseEntity<DividendDTO> createDividend(@RequestBody DividendCreateRequest request) {
        DividendDTO created = dividendService.createDividend(request);
        return ResponseEntity.ok(created);
    }

    // Delete a dividend
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDividend(@PathVariable UUID id) {
        dividendService.deleteDividend(id);
        return ResponseEntity.noContent().build();

    }

    // Get all dividends for a specific stock
    @GetMapping("/stock/{stockId}")
    public ResponseEntity<List<DividendDTO>> getDividendsForStock(@PathVariable UUID stockId) {
        return ResponseEntity.ok(dividendService.getDividendsByStock(stockId));
    }
}