package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.pricealert.PriceAlertCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.pricealert.PriceAlertDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.PriceAlert;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.entities.User;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PriceAlertRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/price-alerts")
@CrossOrigin(origins = "*")
@Profile("!test")
public class PriceAlertController {

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private StockRepository stockRepository;

    // GET /api/price-alerts - Get all price alerts for authenticated user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PriceAlertDTO>> getUserAlerts(
            @RequestParam(required = false) Boolean active,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<PriceAlert> alerts;
        if (active != null) {
            alerts = priceAlertRepository.findByUserIdAndIsActive(user.getUserId(), active);
        } else {
            alerts = priceAlertRepository.findByUserId(user.getUserId());
        }

        List<PriceAlertDTO> alertDTOs = alerts.stream()
                .map(alert -> new PriceAlertDTO(
                        alert.getAlertId(),
                        user.getUserId(),
                        alert.getStock().getStockId(),
                        alert.getStock().getStockCode(),
                        alert.getStock().getCompanyName(),
                        alert.getType(),
                        alert.getTargetPrice(),
                        alert.getIsActive(),
                        alert.getCreatedAt(),
                        alert.getTriggeredAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(alertDTOs);
    }

    // POST /api/price-alerts - Create a new price alert
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPriceAlert(
            @Valid @RequestBody PriceAlertCreateRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        // Validate stock exists
        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        // Validate alert type is ABOVE or BELOW
        if (!request.getType().equals("ABOVE") && !request.getType().equals("BELOW")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Alert type must be ABOVE or BELOW"));
        }

        // Create new price alert
        PriceAlert alert = new PriceAlert(user, stock, request.getType(), request.getTargetPrice());
        priceAlertRepository.save(alert);

        PriceAlertDTO dto = new PriceAlertDTO(
                alert.getAlertId(),
                user.getUserId(),
                stock.getStockId(),
                stock.getStockCode(),
                stock.getCompanyName(),
                alert.getType(),
                alert.getTargetPrice(),
                alert.getIsActive(),
                alert.getCreatedAt(),
                alert.getTriggeredAt()
        );

        return ResponseEntity.ok(dto);
    }

    // DELETE /api/price-alerts/{alertId} - Delete a price alert
    @DeleteMapping("/{alertId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePriceAlert(
            @PathVariable UUID alertId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        PriceAlert alert = priceAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        // Verify the alert belongs to the authenticated user
        if (!alert.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).build();
        }

        priceAlertRepository.delete(alert);
        return ResponseEntity.noContent().build();
    }
}
