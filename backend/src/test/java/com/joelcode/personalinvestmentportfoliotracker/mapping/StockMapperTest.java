package com.joelcode.personalinvestmentportfoliotracker.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockDTO;
import com.joelcode.personalinvestmentportfoliotracker.dto.stock.StockUpdateRequest;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.StockMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockMapperTest {

    @Test
    void testToEntity_Success() {
        // Arrange
        StockCreateRequest request = new StockCreateRequest(UUID.randomUUID(), "Apple Inc", "AAPL",
                BigDecimal.valueOf(20), "Tech");
        request.setStockValue(BigDecimal.valueOf(150.0));

        // Act
        Stock stock = StockMapper.toEntity(request);

        // Assert
        assertNotNull(stock);
        assertEquals("AAPL", stock.getStockCode());
        assertEquals("Apple Inc", stock.getCompanyName());
        assertEquals(BigDecimal.valueOf(150.0), stock.getStockValue());
    }

    @Test
    void testToEntity_NullRequest() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            StockMapper.toEntity(null);
        });
    }

    @Test
    void testUpdateEntity_AllFields() {
        // Arrange
        Stock stock = new Stock();
        stock.setStockCode("MSFT");
        stock.setCompanyName("Microsoft");

        StockUpdateRequest request = new StockUpdateRequest("MSFT-UPDATED", "Microsoft Corp", UUID.randomUUID(), "Tech");

        // Act
        StockMapper.updateEntity(stock, request);

        // Assert
        assertEquals("MSFT-UPDATED", stock.getStockCode());
        assertEquals("Microsoft Corp", stock.getCompanyName());
    }

    @Test
    void testUpdateEntity_PartialFields() {
        // Arrange
        Stock stock = new Stock();
        stock.setStockCode("GOOGL");
        stock.setCompanyName("Google");

        StockUpdateRequest request = new StockUpdateRequest("GOOGL", null, UUID.randomUUID(), "Tech");

        // Act
        StockMapper.updateEntity(stock, request);

        // Assert
        assertEquals("GOOGL", stock.getStockCode());
        assertEquals("Google", stock.getCompanyName());
    }

    @Test
    void testUpdateEntity_AllNull() {
        // Arrange
        Stock stock = new Stock();
        stock.setStockCode("TSLA");
        stock.setCompanyName("Tesla");

        StockUpdateRequest request = new StockUpdateRequest(null, null, null, null);

        // Act
        StockMapper.updateEntity(stock, request);

        // Assert
        assertEquals("TSLA", stock.getStockCode());
        assertEquals("Tesla", stock.getCompanyName());
    }

    @Test
    void testToDTO_Success() {
        // Arrange
        Stock stock = new Stock();
        UUID stockId = UUID.randomUUID();
        stock.setStockId(stockId);
        stock.setStockCode("AAPL");
        stock.setCompanyName("Apple Inc");
        stock.setStockValue(BigDecimal.valueOf(150));

        // Act
        StockDTO dto = StockMapper.toDTO(stock);

        // Assert
        assertNotNull(dto);
        assertEquals(stockId, dto.getStockId());
        assertEquals("AAPL", dto.getStockCode());
        assertEquals("Apple Inc", dto.getCompanyName());
        assertEquals(BigDecimal.valueOf(150), dto.getStockValue());
    }

    @Test
    void testToDTO_Null() {
        // Act
        StockDTO dto = StockMapper.toDTO(null);

        // Assert
        assertNull(dto);
    }
}