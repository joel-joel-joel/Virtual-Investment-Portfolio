package com.joelcode.personalinvestmentportfoliotracker.controllers.utilitycontrollers;

import com.joelcode.personalinvestmentportfoliotracker.dto.earnings.EarningsDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Earnings;
import com.joelcode.personalinvestmentportfoliotracker.repositories.EarningsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/earnings")
@CrossOrigin(origins = "*")
@Profile("!test")
public class EarningsController {

    @Autowired
    private EarningsRepository earningsRepository;

    // GET /api/earnings/upcoming - Get upcoming earnings for the next 90 days
    @GetMapping("/upcoming")
    public ResponseEntity<List<EarningsDTO>> getUpcomingEarnings() {
        LocalDate today = LocalDate.now();
        LocalDate in90Days = today.plusDays(90);

        List<Earnings> earnings = earningsRepository.findUpcomingEarnings(today, in90Days);

        List<EarningsDTO> earningsDTOs = earnings.stream()
                .map(e -> new EarningsDTO(
                        e.getEarningId(),
                        e.getStock().getStockId(),
                        e.getStock().getStockCode(),
                        e.getStock().getCompanyName(),
                        e.getEarningsDate(),
                        e.getEstimatedEPS(),
                        e.getActualEPS(),
                        e.getReportTime()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(earningsDTOs);
    }

    // GET /api/earnings/by-stock/{stockId} - Get earnings for a specific stock
    @GetMapping("/by-stock/{stockId}")
    public ResponseEntity<List<EarningsDTO>> getEarningsByStock(@PathVariable UUID stockId) {
        List<Earnings> earnings = earningsRepository.findByStockId(stockId);

        List<EarningsDTO> earningsDTOs = earnings.stream()
                .map(e -> new EarningsDTO(
                        e.getEarningId(),
                        e.getStock().getStockId(),
                        e.getStock().getStockCode(),
                        e.getStock().getCompanyName(),
                        e.getEarningsDate(),
                        e.getEstimatedEPS(),
                        e.getActualEPS(),
                        e.getReportTime()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(earningsDTOs);
    }
}
