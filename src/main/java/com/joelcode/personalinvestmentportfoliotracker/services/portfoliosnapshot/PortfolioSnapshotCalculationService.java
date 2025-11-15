package com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PortfolioSnapshotCalculationService {

    PortfolioSnapshotDTO generateSnapshotForToday(UUID accountId);

    BigDecimal calculateTimeWeightedReturn(UUID accountId, LocalDate startDate, LocalDate endDate);

    BigDecimal calculateAveragePortfolioValue(UUID accountId, LocalDate startDate, LocalDate endDate);
}