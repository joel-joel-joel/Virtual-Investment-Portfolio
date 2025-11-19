package com.joelcode.personalinvestmentportfoliotracker.services.holding;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.transaction.TransactionCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface HoldingService {

    HoldingDTO createHolding(HoldingCreateRequest request);

    HoldingDTO getHoldingById(UUID id);

    List<HoldingDTO> getAllHoldings();

    List<HoldingDTO> getHoldingsByAccount(UUID accountId);

    HoldingDTO updateHolding(UUID id, HoldingUpdateRequest request);

    void updateHoldingAfterSale(Holding holding, BigDecimal quantitySold, BigDecimal salePrice);

    void updateOrCreateHoldingFromTransaction(TransactionCreateRequest request);

    void deleteHolding(UUID id);

    List<HoldingDTO> getHoldingsForAccount(UUID accountId);
}