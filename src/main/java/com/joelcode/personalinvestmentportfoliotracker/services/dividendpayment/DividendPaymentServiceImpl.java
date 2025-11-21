package com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment;

import com.joelcode.personalinvestmentportfoliotracker.controllers.WebSocketController;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.*;
import com.joelcode.personalinvestmentportfoliotracker.services.holding.HoldingCalculationService;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.DividendPaymentMapper;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class DividendPaymentServiceImpl implements DividendPaymentService {

    // Define key fields
    private final DividendPaymentRepository paymentRepository;
    private final DividendRepository dividendRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final DividendPaymentValidationService validationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final HoldingCalculationService holdingCalculationService;


    // Constructor
    public DividendPaymentServiceImpl(DividendPaymentRepository paymentRepository,
                                      DividendRepository dividendRepository,
                                      AccountRepository accountRepository,
                                      StockRepository stockRepository,
                                      HoldingRepository holdingRepository,
                                      DividendPaymentValidationService validationService,
                                      SimpMessagingTemplate messagingTemplate,
                                      HoldingCalculationService holdingCalculationService) {
        this.paymentRepository = paymentRepository;
        this.dividendRepository = dividendRepository;
        this.accountRepository = accountRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.validationService = validationService;
        this.messagingTemplate = messagingTemplate;
        this.holdingCalculationService = holdingCalculationService;
    }


    // Interface function

    // Create response dividend payment dto from request dto
    @Override
    @Transactional
    public DividendPaymentDTO createDividendPayment(DividendPaymentCreateRequest request) {

        // Validate request
        validationService.validateCreateRequest(request);

        // Fetch entities
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + request.getAccountId()));

        Dividend dividend = dividendRepository.findById(request.getDividendId())
                .orElseThrow(() -> new RuntimeException("Dividend not found with ID: " + request.getDividendId()));

        Stock stock = dividend.getStock();

        // Check for duplicate payment
        if (paymentRepository.existsByAccountAndDividend(account, dividend)) {
            throw new RuntimeException("Payment already exists for this account and dividend");
        }

        // Create payment entity
        DividendPayment payment = DividendPaymentMapper.toEntity(request, account, dividend, stock);

        // Save to database
        payment = paymentRepository.save(payment);

        // Calculate updated portfolio value
        BigDecimal previousPortfolioValue = account.getAccountBalance().add(
                holdingCalculationService.calculateTotalUnrealizedGain(account.getAccountId())
        );
        BigDecimal updatedPortfolioValue = previousPortfolioValue.add(
                request.getShareQuantity().multiply(dividend.getDividendAmountPerShare())
        );
        BigDecimal change = updatedPortfolioValue.subtract(previousPortfolioValue);

        // Update account balance in memory (optional, DB update might happen elsewhere)
        account.setAccountBalance(account.getAccountBalance().add(
                request.getShareQuantity().multiply(dividend.getDividendAmountPerShare())
        ));

        // WebSocket notification: user alert
        messagingTemplate.convertAndSend(
                "/topic/dividends",
                new WebSocketController.UserNotification(
                        "New dividend payment for stock " + stock.getStockCode() +
                                " on " + dividend.getPayDate() +
                                " at " + dividend.getDividendAmountPerShare() + " per share, total payment is "
                                + (request.getShareQuantity().multiply(dividend.getDividendAmountPerShare())),
                        LocalDateTime.now()
                )
        );

        // WebSocket notification: portfolio update
        messagingTemplate.convertAndSend(
                "/topic/portfolio/" + account.getAccountId(),
                new WebSocketController.PortfolioUpdateMessage(
                        account.getAccountId(),
                        updatedPortfolioValue,
                        change,
                        LocalDateTime.now()
                )
        );

        return DividendPaymentMapper.toDTO(payment);
    }

    // Get paymnent by iD
    @Override
    public DividendPaymentDTO getDividendPaymentById(UUID paymentId) {
        DividendPayment payment = validationService.validatePaymentExists(paymentId);
        return DividendPaymentMapper.toDTO(payment);
    }

    // Get payments by account
    @Override
    public List<DividendPaymentDTO> getDividendPaymentsByAccount(UUID accountId) {
        validationService.validateAccountExists(accountId);

        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountOrderByDate(accountId);

        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get by account and stock
    @Override
    public List<DividendPaymentDTO> getDividendPaymentsByAccountAndStock(UUID accountId, UUID stockId) {
        validationService.validateAccountExists(accountId);

        List<DividendPayment> payments = paymentRepository.findPaymentsByIdAccountAndStockId(accountId, stockId);

        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get payments bya account
    @Override
    public List<DividendPaymentDTO> getDividendPaymentsForAccount(UUID accountId) {
        List<DividendPayment> payments = paymentRepository.findPaymentsByIdAccountAndStockId(accountId, null);

        // Map to DTOs
        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get payments by stock
    @Override
    public List<DividendPaymentDTO> getDividendPaymentsForStock(UUID stockId) {
        List<DividendPayment> payments = paymentRepository.findPaymentsByIdAccountAndStockId(null, stockId);

        // Map to DTOs
        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get payments within a date range
    @Override
    public List<DividendPaymentDTO> getDividendPaymentsByAccountInDateRange(UUID accountId,
                                                                    LocalDateTime start,
                                                                    LocalDateTime end) {
        validationService.validateAccountExists(accountId);
        validationService.validateDateRange(start, end);

        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountAndDateRange(accountId, start, end);

        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Calculate total divided from an account
    @Override
    public BigDecimal calculateTotalDividendsByAccount(UUID accountId) {
        validationService.validateAccountExists(accountId);
        return paymentRepository.calculateTotalDividendsByAccount(accountId);
    }

    // Calculate total divided from an account and stock
    @Override
    public BigDecimal calculateTotalDividendsByAccountAndStock(UUID accountId, UUID stockId) {
        validationService.validateAccountExists(accountId);
        return paymentRepository.calculateTotalDividendsByAccountAndStock(accountId, stockId);
    }

    // Get pending payments
    @Override
    public List<DividendPaymentDTO> getPendingPayments(UUID accountId) {
        validationService.validateAccountExists(accountId);

        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountAndStatus(
                accountId,
                DividendPayment.PaymentStatus.PENDING
        );

        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Process payment for dividend
    @Override
    @Transactional
    public void processPaymentsForDividend(UUID dividendId) {
        // When a dividend is announced, automatically create payment records
        // for all accounts that hold this stock

        Dividend dividend = dividendRepository.findById(dividendId)
                .orElseThrow(() -> new RuntimeException("Dividend not found with ID: " + dividendId));

        Stock stock = dividend.getStock();

        // Find all holdings of this stock
        List<Holding> holdings = holdingRepository.findByStock_StockId(stock.getStockId());

        // Create payment record for each holding
        for (Holding holding : holdings) {

            // Skip if payment already exists
            if (paymentRepository.existsByAccountAndDividend(holding.getAccount(), dividend)) {
                continue;
            }

            // Create payment
            DividendPayment payment = new DividendPayment(
                    holding.getAccount(),
                    stock,
                    dividend,
                    holding.getQuantity(),
                    dividend.getDividendAmountPerShare().multiply(holding.getQuantity()),
                    dividend.getPayDate()
            );

            paymentRepository.save(payment);
        }
    }

    // Delete payment
    @Override
    @Transactional
    public void deleteDividendPayment(UUID paymentId) {
        DividendPayment payment = validationService.validatePaymentExists(paymentId);
        paymentRepository.delete(payment);
    }

    // Get all payments
    @Override
    public List<DividendPaymentDTO> getAllDividendPayments() {
        // Fetch all dividend payment entities
        List<DividendPayment> payments = paymentRepository.findAll();

        // Map each entity to a DTO
        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

}
