package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.stock.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stocks")
@CrossOrigin(origins = "*")
public class StockController {

    @Autowired
    public StockService stockService;

    // Get all stocks
    @GetMapping
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    // Get stock by ID
    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(@PathVariable UUID id) {
        StockDTO stock = stockService.getStockById(id);
        if (stock != null) {
            return ResponseEntity.ok(stock);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create new stock
    @PostMapping
    public ResponseEntity<StockDTO> createStock(@Valid @RequestBody StockCreateRequest request) {
        StockDTO created = stockService.createStock(request);
        return ResponseEntity.ok(created);
    }

    // Update stock
    @PutMapping("/{id}")
    public ResponseEntity<StockDTO> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateRequest request
    ) {
        StockDTO updated = stockService.updateStock(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete stock
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable UUID id) {
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }

    // Get current price for a stock
    @GetMapping("/{id}/price")
    public ResponseEntity<?> getCurrentPrice(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.getCurrentPrice(id));
    }
}