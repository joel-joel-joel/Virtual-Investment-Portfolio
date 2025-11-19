package com.joelcode.personalinvestmentportfoliotracker.services.dividendpayment;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DividendPaymentService {

    DividendPaymentDTO createDividendPayment(DividendPaymentCreateRequest request);

    DividendPaymentDTO getDividendPaymentById(UUID paymentId);

    List<DividendPaymentDTO> getDividendPaymentsByAccount(UUID accountId);

    List<DividendPaymentDTO> getDividendPaymentsByAccountAndStock(UUID accountId, UUID stockId);

    List<DividendPaymentDTO> getDividendPaymentsByAccountInDateRange(UUID accountId, LocalDateTime start, LocalDateTime end);

    BigDecimal calculateTotalDividendsByAccount(UUID accountId);

    BigDecimal calculateTotalDividendsByAccountAndStock(UUID accountId, UUID stockId);

    List<DividendPaymentDTO> getPendingPayments(UUID accountId);

    void processPaymentsForDividend(UUID dividendId);

    void deleteDividendPayment(UUID paymentId);

    List<DividendPaymentDTO> getDividendPaymentsForAccount(UUID accountId);

    List<DividendPaymentDTO> getDividendPaymentsForStock(UUID stockId);
}