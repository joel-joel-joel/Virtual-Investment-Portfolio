package com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PortfolioSnapshotService {

    PortfolioSnapshotDTO createSnapshot(PortfolioSnapshotCreateRequest request);

    PortfolioSnapshotDTO getSnapshotById(UUID snapshotId);

    List<PortfolioSnapshotDTO> getAllSnapshots();

    List<PortfolioSnapshotDTO> getSnapshotsByAccount(UUID accountId);

    List<PortfolioSnapshotDTO> getSnapshotsByDateRange(UUID accountId, LocalDate startDate, LocalDate endDate);

    PortfolioSnapshotDTO getLatestSnapshot(UUID accountId);

    void deleteSnapshot(UUID id);

    List<PortfolioSnapshotDTO> getSnapshotsForAccount(UUID accountId);
}