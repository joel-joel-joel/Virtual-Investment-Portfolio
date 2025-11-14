package com.joelcode.personalinvestmentportfoliotracker.dto.dividend;

import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DividendDTO {

    // Dividend response DTO (output)
    private final UUID dividendId;
    private final UUID stockId;
    private final BigDecimal amountPerShare;
    private final LocalDateTime payDate;

    // Constructor
    public DividendDTO(UUID dividendId, UUID stockId, BigDecimal amountPerShare, LocalDateTime payDate) {
        this.dividendId = dividendId;
        this.stockId = stockId;
        this.amountPerShare = amountPerShare;
        this.payDate = payDate;
    }

    public DividendDTO(Dividend dividend) {
        this.dividendId = dividend.getDividendId();
        this.stockId = dividend.getStock().getStockId();
        this.amountPerShare = dividend.getAmountPerShare();
        this.payDate = dividend.getPayDate();
    }

    // Getters
    public UUID getDividendId() {return dividendId;}

    public UUID getStockCode() {return stockId;}

    public BigDecimal getAmountPerShare() {return amountPerShare;}

    public LocalDateTime getPayDate() {return payDate;}
}
