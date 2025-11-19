package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/holdings")
public class HoldingController {

    @Autowired
    private HoldingService holdingService;

    // Get all holdings
    @GetMapping
    public ResponseEntity<List<HoldingDTO>> getAllHoldings() {
        return ResponseEntity.ok(holdingService.getAllHoldings());
    }

    // Get a holding by ID
    @GetMapping("/{id}")
    public ResponseEntity<HoldingDTO> getHoldingById(@PathVariable UUID id) {
        HoldingDTO holding = holdingService.getHoldingById(id);
        if (holding != null) {
            return ResponseEntity.ok(holding);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new holding
    @PostMapping
    public ResponseEntity<HoldingDTO> createHolding(@RequestBody HoldingCreateRequest request) {
        HoldingDTO created = holdingService.createHolding(request);
        return ResponseEntity.ok(created);
    }

    // Update a holding
    @PutMapping("/{id}")
    public ResponseEntity<HoldingDTO> updateHolding(
            @PathVariable UUID id,
            @RequestBody HoldingUpdateRequest request
    ) {
        HoldingDTO updated = holdingService.updateHolding(id, request);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a holding
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHolding(@PathVariable UUID id) {
        holdingService.deleteHolding(id);
        return ResponseEntity.noContent().build();
    }

    // Get all holdings for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<HoldingDTO>> getHoldingsForAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(holdingService.getHoldingsForAccount(accountId));
    }
}
