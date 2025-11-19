package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot.PortfolioSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/snapshots")
public class PortfolioSnapshotController {

    @Autowired
    private PortfolioSnapshotService snapshotService;

    // Get all snapshots
    @GetMapping
    public ResponseEntity<List<PortfolioSnapshotDTO>> getAllSnapshots() {
        return ResponseEntity.ok(snapshotService.getAllSnapshots());
    }

    // Get snapshot by ID
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioSnapshotDTO> getSnapshotById(@PathVariable UUID id) {
        PortfolioSnapshotDTO snapshot = snapshotService.getSnapshotById(id);
        if (snapshot != null) {
            return ResponseEntity.ok(snapshot);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new snapshot
    @PostMapping
    public ResponseEntity<PortfolioSnapshotDTO> createSnapshot(@RequestBody PortfolioSnapshotCreateRequest request) {
        PortfolioSnapshotDTO created = snapshotService.createSnapshot(request);
        return ResponseEntity.ok(created);
    }

    // Delete a snapshot
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnapshot(@PathVariable UUID id) {
       snapshotService.deleteSnapshot(id);
       return ResponseEntity.noContent().build();
    }

    // Get all snapshots for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PortfolioSnapshotDTO>> getSnapshotsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(snapshotService.getSnapshotsForUser(userId));
    }

    // Get all snapshots for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<PortfolioSnapshotDTO>> getSnapshotsForAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(snapshotService.getSnapshotsForAccount(accountId));
    }
}
