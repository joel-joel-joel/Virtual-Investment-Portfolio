package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PortfolioSnapshotMapper {

    // Convert portfolio snapshot creation request DTO to entity
    public static PortfolioSnapshot toEntity(PortfolioSnapshotCreateRequest request, Account account) {
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setAccount(account);
        snapshot.setSnapshotDate(request.getSnapshotDate());
        snapshot.setTotalValue(request.getTotalValue());
        snapshot.setCashBalance(request.getCashBalance());
        snapshot.setTotalInvested(request.getTotalInvested());
        snapshot.setTotalGain(request.getTotalGain() != null ? request.getTotalGain() : BigDecimal.ZERO);
        snapshot.setDayChange(request.getDayChange() != null ? request.getDayChange() : BigDecimal.ZERO);
        snapshot.setDayChangePercent(request.getDayChangePercent() != null ? request.getDayChangePercent() : BigDecimal.ZERO);
        return snapshot;
    }

    // Convert portfolio snapshot entity to portfolio snapshot response DTO
    public static PortfolioSnapshotDTO toDTO(PortfolioSnapshot snapshot) {
        if (snapshot == null) return null;
        return new PortfolioSnapshotDTO(
                snapshot.getSnapshotId(),
                snapshot.getAccount().getAccountId(),
                snapshot.getSnapshotDate(),
                snapshot.getTotalValue(),
                snapshot.getCashBalance(),
                snapshot.getTotalInvested(),
                snapshot.getTotalGain(),
                snapshot.getTotalGainPercent(),
                snapshot.getDayChange(),
                snapshot.getDayChangePercent(),
                snapshot.getMarketValue()
        );
    }
}