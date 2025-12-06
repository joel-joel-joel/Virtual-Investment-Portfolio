package com.joelcode.personalinvestmentportfoliotracker.dto.earnings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class EarningsDTO {

    @JsonProperty("earningId")
    private UUID earningId;

    @JsonProperty("stockId")
    private UUID stockId;

    @JsonProperty("stockCode")
    private String stockCode;

    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("earningsDate")
    private LocalDate earningsDate;

    @JsonProperty("estimatedEPS")
    private BigDecimal estimatedEPS;

    @JsonProperty("actualEPS")
    private BigDecimal actualEPS;

    @JsonProperty("reportTime")
    private String reportTime;

    public EarningsDTO() {}

    public EarningsDTO(UUID earningId, UUID stockId, String stockCode, String companyName,
                       LocalDate earningsDate, BigDecimal estimatedEPS, BigDecimal actualEPS, String reportTime) {
        this.earningId = earningId;
        this.stockId = stockId;
        this.stockCode = stockCode;
        this.companyName = companyName;
        this.earningsDate = earningsDate;
        this.estimatedEPS = estimatedEPS;
        this.actualEPS = actualEPS;
        this.reportTime = reportTime;
    }

    public UUID getEarningId() { return earningId; }
    public void setEarningId(UUID earningId) { this.earningId = earningId; }

    public UUID getStockId() { return stockId; }
    public void setStockId(UUID stockId) { this.stockId = stockId; }

    public String getStockCode() { return stockCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public LocalDate getEarningsDate() { return earningsDate; }
    public void setEarningsDate(LocalDate earningsDate) { this.earningsDate = earningsDate; }

    public BigDecimal getEstimatedEPS() { return estimatedEPS; }
    public void setEstimatedEPS(BigDecimal estimatedEPS) { this.estimatedEPS = estimatedEPS; }

    public BigDecimal getActualEPS() { return actualEPS; }
    public void setActualEPS(BigDecimal actualEPS) { this.actualEPS = actualEPS; }

    public String getReportTime() { return reportTime; }
    public void setReportTime(String reportTime) { this.reportTime = reportTime; }
}
