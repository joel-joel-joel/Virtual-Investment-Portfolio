package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricehistory.PriceHistoryCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.pricehistory.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricehistory")
public class PriceHistoryController {

    @Autowired
    private PriceHistoryService priceHistoryService;

    // Get all price history records
    @GetMapping
    public ResponseEntity<List<PriceHistoryDTO>> getAllPriceHistory() {
        return ResponseEntity.ok(priceHistoryService.getAllPriceHistories());
    }

    // Get a specific price history record by ID
    @GetMapping("/{id}")
    public ResponseEntity<PriceHistoryDTO> getPriceHistoryById(@PathVariable UUID id) {
        PriceHistoryDTO dto = priceHistoryService.getPriceHistoryById(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Add a new price history record
    @PostMapping
    public ResponseEntity<PriceHistoryDTO> createPriceHistory(@RequestBody PriceHistoryCreateRequest request) {
        PriceHistoryDTO created = priceHistoryService.createPriceHistory(request);
        return ResponseEntity.ok(created);
    }

    // Delete a price history record
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePriceHistory(@PathVariable UUID id) {
        priceHistoryService.deletePriceHistory(id);
        return ResponseEntity.noContent().build();
    }

    // Get all price history for a specific stock
    @GetMapping("/stock/{stockId}")
    public ResponseEntity<List<PriceHistoryDTO>> getPriceHistoryForStock(@PathVariable UUID stockId) {
        return ResponseEntity.ok(priceHistoryService.getPriceHistoryForStock(stockId));
    }

    // Get latest price for a specific stock
    @GetMapping("/stock/{stockId}/latest")
    public ResponseEntity<PriceHistoryDTO> getLatestPriceForStock(@PathVariable UUID stockId) {
        PriceHistoryDTO dto = priceHistoryService.getLatestPriceForStock(stockId);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
