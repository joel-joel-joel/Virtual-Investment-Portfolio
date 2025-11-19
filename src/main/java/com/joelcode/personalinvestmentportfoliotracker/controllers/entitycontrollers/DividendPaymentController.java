package com.joelcode.personalinvestmentportfoliotracker.controllers.entitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dividendpayments")
public class DividendPaymentController {

    @Autowired
    private DividendPaymentService dividendPaymentService;

    // Get all dividend payments
    @GetMapping
    public ResponseEntity<List<DividendPaymentDTO>> getAllDividendPayments() {
        return ResponseEntity.ok(dividendPaymentService.getAllDividendPayments());
    }

    // Get a dividend payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<DividendPaymentDTO> getDividendPaymentById(@PathVariable UUID id) {
        DividendPaymentDTO dto = dividendPaymentService.getDividendPaymentById(id);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Create a new dividend payment
    @PostMapping
    public ResponseEntity<DividendPaymentDTO> createDividendPayment(@RequestBody DividendPaymentCreateRequest request) {
        DividendPaymentDTO created = dividendPaymentService.createDividendPayment(request);
        return ResponseEntity.ok(created);
    }


    // Delete a dividend payment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDividendPayment(@PathVariable UUID id) {
        dividendPaymentService.deleteDividendPayment(id);
        return ResponseEntity.noContent().build();
    }

    // Get all dividend payments for a specific stock
    @GetMapping("/stock/{stockId}")
    public ResponseEntity<List<DividendPaymentDTO>> getDividendPaymentsForStock(@PathVariable UUID stockId) {
        return ResponseEntity.ok(dividendPaymentService.getDividendPaymentsForStock(stockId));
    }

    // Get all dividend payments for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<DividendPaymentDTO>> getDividendPaymentsForAccount(@PathVariable UUID accountId) {
        return ResponseEntity.ok(dividendPaymentService.getDividendPaymentsForAccount(accountId));
    }
}
