package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividendpayment.DividendPaymentDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.DividendPayment;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.stereotype.Component;

@Component
public class DividendPaymentMapper {

    // Convert account creation request DTO to entity
    public static DividendPayment toEntity(DividendPaymentCreateRequest request,
                                           Account account,
                                           Dividend dividend,
                                           Stock stock) {
        if (request == null) {
            return null;
        }

        if (dividend == null) {
            throw new NullPointerException("Dividend cannot be null");
        }

        DividendPayment payment = new DividendPayment();
        payment.setAccount(account);
        payment.setStock(stock);
        payment.setDividend(dividend);
        payment.setShareQuantity(request.getShareQuantity());
        payment.setPaymentDate(request.getPaymentDate());
        payment.setStatus(DividendPayment.PaymentStatus.PAID);

        // Calculate total amount automatically
        payment.calculateTotalAmount();

        return payment;
    }

    // Convert entity to response dividend payment dto
    public static DividendPaymentDTO toDTO(DividendPayment payment) {
        if (payment == null) {
            return null;
        }
        return new DividendPaymentDTO(payment);
    }

}