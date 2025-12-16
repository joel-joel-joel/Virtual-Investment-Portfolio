package com.joelcode.personalinvestmentportfoliotracker.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.holding.HoldingUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.Holding;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.HoldingMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HoldingMapperTest {

    @Test
    void testToEntity_Success() {
        // Arrange
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());

        Stock stock = new Stock();
        stock.setStockId(UUID.randomUUID());

        HoldingCreateRequest request = new HoldingCreateRequest(
                account.getAccountId(),
                stock.getStockId(),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(150.0),
                BigDecimal.valueOf(15000.0),
                stock.getIndustry()
        );

        // Act
        Holding holding = HoldingMapper.toEntity(request, account, stock);

        // Assert
        assertNotNull(holding);
        assertEquals(account, holding.getAccount());
        assertEquals(stock, holding.getStock());
        assertEquals(BigDecimal.valueOf(100), holding.getQuantity());
        assertEquals(BigDecimal.valueOf(150.0), holding.getAverageCostBasis());
        assertEquals(BigDecimal.valueOf(15000.0), holding.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, holding.getRealizedGain());
        assertNotNull(holding.getFirstPurchaseDate());
    }

    @Test
    void testToEntity_NullAccount() {
        // Arrange
        Stock stock = new Stock();
        HoldingCreateRequest request = new HoldingCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(150.0),
                BigDecimal.valueOf(15000.0),
                stock.getIndustry()
        );

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            HoldingMapper.toEntity(request, null, stock);
        });
    }

    @Test
    void testToEntity_NullStock() {
        // Arrange
        Account account = new Account();
        HoldingCreateRequest request = new HoldingCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(150.0),
                BigDecimal.valueOf(15000.0),
                null
        );

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            HoldingMapper.toEntity(request, account, null);
        });
    }

    @Test
    void testUpdateEntity_AllFields() {
        // Arrange
        Holding holding = new Holding();
        holding.setQuantity(BigDecimal.valueOf(100));
        holding.setAverageCostBasis(BigDecimal.valueOf(150.0));
        holding.setTotalCostBasis(BigDecimal.valueOf(15000.0));
        holding.setRealizedGain(BigDecimal.valueOf(500.0));

        HoldingUpdateRequest request = new HoldingUpdateRequest(
                BigDecimal.valueOf(150),
                BigDecimal.valueOf(155.0),
                BigDecimal.valueOf(23250.0),
                BigDecimal.valueOf(750.0)
        );

        // Act
        HoldingMapper.updateEntity(holding, request);

        // Assert
        assertEquals(BigDecimal.valueOf(150), holding.getQuantity());
        assertEquals(BigDecimal.valueOf(155.0), holding.getAverageCostBasis());
        assertEquals(BigDecimal.valueOf(23250.0), holding.getTotalCostBasis());
        assertEquals(BigDecimal.valueOf(750.0), holding.getRealizedGain());
    }

    @Test
    void testUpdateEntity_PartialFields() {
        // Arrange
        Holding holding = new Holding();
        holding.setQuantity(BigDecimal.valueOf(100));
        holding.setAverageCostBasis(BigDecimal.valueOf(150.0));
        holding.setTotalCostBasis(BigDecimal.valueOf(15000.0));
        holding.setRealizedGain(BigDecimal.ZERO);

        HoldingUpdateRequest request = new HoldingUpdateRequest(
                BigDecimal.valueOf(120),
                null,
                null,
                null
        );

        // Act
        HoldingMapper.updateEntity(holding, request);

        // Assert
        assertEquals(BigDecimal.valueOf(120), holding.getQuantity());
        assertEquals(BigDecimal.valueOf(150.0), holding.getAverageCostBasis());
        assertEquals(BigDecimal.valueOf(15000.0), holding.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, holding.getRealizedGain());
    }

    @Test
    void testUpdateEntity_AllNull() {
        // Arrange
        Holding holding = new Holding();
        holding.setQuantity(BigDecimal.valueOf(100));
        holding.setAverageCostBasis(BigDecimal.valueOf(150.0));
        holding.setTotalCostBasis(BigDecimal.valueOf(15000.0));
        holding.setRealizedGain(BigDecimal.ZERO);

        HoldingUpdateRequest request = new HoldingUpdateRequest(
                null,
                null,
                null,
                null
        );

        // Act
        HoldingMapper.updateEntity(holding, request);

        // Assert
        assertEquals(BigDecimal.valueOf(100), holding.getQuantity());
        assertEquals(BigDecimal.valueOf(150.0), holding.getAverageCostBasis());
        assertEquals(BigDecimal.valueOf(15000.0), holding.getTotalCostBasis());
        assertEquals(BigDecimal.ZERO, holding.getRealizedGain());
    }

    @Test
    void testToDTO_Success() {
        // Arrange
        Holding holding = new Holding();
        UUID holdingId = UUID.randomUUID();
        holding.setHoldingId(holdingId);

        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        holding.setAccount(account);

        Stock stock = new Stock();
        UUID stockId = UUID.randomUUID();
        stock.setStockId(stockId);
        stock.setStockCode("AAPL");
        holding.setStock(stock);

        holding.setQuantity(BigDecimal.valueOf(100));
        holding.setAverageCostBasis(BigDecimal.valueOf(150.0));
        holding.setTotalCostBasis(BigDecimal.valueOf(15000.0));
        holding.setRealizedGain(BigDecimal.valueOf(500.0));
        holding.setFirstPurchaseDate(LocalDateTime.now());

        BigDecimal currentPrice = BigDecimal.valueOf(160.0);

        // Act
        HoldingDTO dto = HoldingMapper.toDTO(holding, currentPrice);

        // Assert
        assertNotNull(dto);
        assertEquals(holdingId, dto.getHoldingId());
        assertEquals(account.getAccountId(), dto.getAccountId());
        assertEquals(stockId, dto.getStockId());
        assertEquals("AAPL", dto.getStockSymbol());
        assertEquals(BigDecimal.valueOf(100), dto.getQuantity());
        assertEquals(BigDecimal.valueOf(150.0), dto.getAverageCostBasis());
    }

    @Test
    void testToDTO_Null() {
        // Act
        HoldingDTO dto = HoldingMapper.toDTO(null, BigDecimal.valueOf(150.0));

        // Assert
        assertNull(dto);
    }
}