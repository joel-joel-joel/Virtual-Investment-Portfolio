package com.joelcode.personalinvestmentportfoliotracker.services.dividend;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.repositories.DividendRepository;
import com.joelcode.personalinvestmentportfoliotracker.repositories.StockRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment.DividendPaymentService;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.DividendMapper;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class DividendServiceImpl implements DividendService {

    // Define key fields
    private final DividendRepository dividendRepository;
    private final StockRepository stockRepository;
    private final DividendValidationService dividendValidationService;
    private final DividendPaymentService dividendPaymentService;
    private final WebSocketController webSocketController;
    private final SimpMessagingTemplate messagingTemplate;


    // Constructors
    public DividendServiceImpl(DividendRepository dividendRepository,
                               StockRepository stockRepository,
                               DividendValidationService dividendValidationService,
                               DividendPaymentService dividendPaymentService,
                               WebSocketController webSocketController,
                               SimpMessagingTemplate messagingTemplate) {
        this.dividendRepository = dividendRepository;
        this.stockRepository = stockRepository;
        this.dividendValidationService = dividendValidationService;
        this.dividendPaymentService = dividendPaymentService;
        this.webSocketController = webSocketController;
        this.messagingTemplate = messagingTemplate;
    }


    // Interface functions

    // Create dividend DTO from request dto
    @Override
    @Transactional
    public DividendDTO createDividend(DividendCreateRequest request) {

        // Validate fields
        dividendValidationService.validateCreateRequest(
                request.getDividendPerShare(),
                request.getPayDate()
        );

        // Fetch stock
        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + request.getStockId()));

        // Check for duplicate dividend
        if (dividendRepository.existsByStockAndPayDate(stock, request.getPayDate())) {
            throw new RuntimeException("Dividend already exists for this stock on this pay date");
        }

        // Create dividend entity
        Dividend dividend = new Dividend(
                request.getDividendPerShare(),
                request.getPayDate(),
                stock
        );

        // Save dividend
        dividend = dividendRepository.save(dividend);

        // Automatically create payment records for all accounts holding this stock
        dividendPaymentService.processPaymentsForDividend(dividend.getDividendId());

        // WebSocket notification
         messagingTemplate.convertAndSend(
                "/topic/dividends",  // topic for all subscribers
                new WebSocketController.UserNotification(
                        "New dividend announced for stock " + stock.getStockCode() +
                                " on " + dividend.getPayDate() +
                                " at " + dividend.getDividendAmountPerShare() + " per share",
                        LocalDateTime.now()
                )
        );

        return DividendMapper.toDTO(dividend);
    }

    // Create response DTO from id
    @Override
    public DividendDTO getDividendById(UUID dividendId) {
        Dividend dividend = dividendValidationService.validateDividendExists(dividendId);
        return DividendMapper.toDTO(dividend);
    }

    // Retrieve all dividends
    @Override
    public List<DividendDTO> getAllDividends() {
        return dividendRepository.findAll()
                .stream()
                .map(DividendMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Retrieve dividends by stockiD
    @Override
    public List<DividendDTO> getDividendsByStock(UUID stockId) {
        return dividendRepository.findByStock_StockId(stockId)
                .stream()
                .map(DividendMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Delete dividend
    @Override
    @Transactional
    public void deleteDividend(UUID dividendId) {
        Dividend dividend = dividendValidationService.validateDividendExists(dividendId);

        dividendRepository.delete(dividend);
    }
}