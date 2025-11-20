package com.joelcode.personalinvestmentportfoliotracker.dto.dividend;

import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DividendDTO {

    // Stock-level dividend information
    // Dividend Response DTO (output)
    private final UUID dividendId;
    private final UUID stockId;
    private final String stockCode;  // Added for convenience
    private final BigDecimal dividendPerShare;
    private final LocalDateTime payDate;
    private final LocalDateTime announcementDate;

    // Constructors
    public DividendDTO(UUID dividendId, UUID stockId, String stockCode,
                       BigDecimal dividendPerShare, LocalDateTime payDate,
                       LocalDateTime announcementDate) {
        this.dividendId = dividendId;
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.dividendPerShare = dividendPerShare;
        this.payDate = payDate;
        this.announcementDate = announcementDate;
    }

    public DividendDTO(Dividend dividend) {
        this.dividendId = dividend.getDividendId();
        this.stockId = dividend.getStockId();
        this.stockCode = dividend.getStock() != null ? dividend.getStock().getStockCode() : null;
        this.dividendPerShare = dividend.getAmountPerShare();
        this.payDate = dividend.getPayDate();
        this.announcementDate = dividend.getAnnouncementDate();
    }

    // Getters
    public UUID getDividendId() {return dividendId;}

    public UUID getStockId() {return stockId;}

    public String getStockCode() {return stockCode;}

    public BigDecimal getDividendPerShare() {return dividendPerShare;}

    public LocalDateTime getPayDate() {return payDate;}

    public LocalDateTime getAnnouncementDate() {return announcementDate;}
}