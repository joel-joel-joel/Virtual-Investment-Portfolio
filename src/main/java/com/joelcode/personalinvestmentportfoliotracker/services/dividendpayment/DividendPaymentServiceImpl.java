package com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import com.joelcode.personalinvestmentportfoliotracker.repositories.*;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.DividendPaymentMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DividendPaymentServiceImpl implements DividendPaymentService {

    private final DividendPaymentRepository paymentRepository;
    private final DividendRepository dividendRepository;
    private final AccountRepository accountRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final DividendPaymentValidationService validationService;

    public DividendPaymentServiceImpl(DividendPaymentRepository paymentRepository,
                                      DividendRepository dividendRepository,
                                      AccountRepository accountRepository,
                                      StockRepository stockRepository,
                                      HoldingRepository holdingRepository,
                                      DividendPaymentValidationService validationService) {
        this.paymentRepository = paymentRepository;
        this.dividendRepository = dividendRepository;
        this.accountRepository = accountRepository;
        this.stockRepository = stockRepository;
        this.holdingRepository = holdingRepository;
        this.validationService = validationService;
    }

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

        return DividendPaymentMapper.toDTO(payment);
    }

    @Override
    public DividendPaymentDTO getDividendPaymentById(UUID paymentId) {
        DividendPayment payment = validationService.validatePaymentExists(paymentId);
        return DividendPaymentMapper.toDTO(payment);
    }

    @Override
    public List<DividendPaymentDTO> getDividendPaymentsByAccount(UUID accountId) {
        validationService.validateAccountExists(accountId);

        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountOrderByDate(accountId);

        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DividendPaymentDTO> getDividendPaymentsByAccountAndStock(UUID accountId, UUID stockId) {
        validationService.validateAccountExists(accountId);

        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountAndStock(accountId, stockId);

        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DividendPaymentDTO> getDividendPaymentsForAccount(UUID accountId) {
        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountAndStock(accountId, null);

        // Map to DTOs
        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DividendPaymentDTO> getDividendPaymentsForStock(UUID stockId) {
        List<DividendPayment> payments = paymentRepository.findPaymentsByAccountAndStock(null, stockId);

        // Map to DTOs
        return payments.stream()
                .map(DividendPaymentMapper::toDTO)
                .collect(Collectors.toList());
    }


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

    @Override
    public BigDecimal calculateTotalDividendsByAccount(UUID accountId) {
        validationService.validateAccountExists(accountId);
        return paymentRepository.calculateTotalDividendsByAccount(accountId);
    }

    @Override
    public BigDecimal calculateTotalDividendsByAccountAndStock(UUID accountId, UUID stockId) {
        validationService.validateAccountExists(accountId);
        return paymentRepository.calculateTotalDividendsByAccountAndStock(accountId, stockId);
    }

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
                    dividend.getAmountPerShare().multiply(holding.getQuantity()),
                    dividend.getPayDate()
            );

            paymentRepository.save(payment);
        }
    }

    @Override
    @Transactional
    public void deleteDividendPayment(UUID paymentId) {
        DividendPayment payment = validationService.validatePaymentExists(paymentId);
        paymentRepository.delete(payment);
    }
}
