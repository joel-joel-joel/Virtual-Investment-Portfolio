package com.joelcode.personalinvestmentportfoliotracker.services.transaction;

import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionDTO;

import java.util.UUID;

public interface TransactionProcessorService {

    TransactionDTO processTransaction(TransactionCreateRequest request);

}
