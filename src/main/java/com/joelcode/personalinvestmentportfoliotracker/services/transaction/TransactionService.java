package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionDTO createTransaction(TransactionCreateRequest request);

    TransactionDTO getTransactionById(UUID transactionId);

    List<TransactionDTO> getAllTransactions();

    void deleteTransaction(UUID transactionId);
}
