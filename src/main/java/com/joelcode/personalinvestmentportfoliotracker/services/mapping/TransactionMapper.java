package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    // Convert transaction creation request DTO to entity
    public static Transaction toEntity(TransactionCreateRequest request) {
        Transaction transaction = new Transaction();
        transaction.setShareQuantity(request.getShareQuantity());
        transaction.setPricePerShare(request.getPricePerShare());
        transaction.setAccountId(request.getAccountId());
        transaction.setStockId(request.getStockId());
        return transaction;
    }

    // Convert transaction entity to transaction response DTO
    public static TransactionDTO toDTO(Transaction transaction) {
        if (transaction == null) return null;
        return new TransactionDTO(transaction.getTransactionId(),
                transaction.getStock().getStockId(),
            transaction.getAccount().getAccountId(),
                transaction.getShareQuantity(),
                transaction.getPricePerShare(),
                transaction.getTransactionType());
    }
}
