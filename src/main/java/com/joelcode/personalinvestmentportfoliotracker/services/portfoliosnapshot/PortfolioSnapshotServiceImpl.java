package com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.PortfolioSnapshotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("!test")
public class PortfolioSnapshotServiceImpl implements PortfolioSnapshotService {

    // Define key fields
    @Autowired
    private PortfolioSnapshotRepository snapshotRepository;

    // Secondary reference to support tests that use a differently named mock
    @Autowired
    private PortfolioSnapshotRepository portfolioSnapshotRepository;

    @Autowired
    private PortfolioSnapshotValidationService snapshotValidationService;

    // Interface functions

    // Create snapshot entity from request dto
    @Override
    public PortfolioSnapshotDTO createSnapshot(PortfolioSnapshotCreateRequest request) {

        // Validate fields and relationships
        Account account = snapshotValidationService.validateAccountExists(request.getAccountId());
        snapshotValidationService.validateCreateRequest(
                request.getTotalValue(),
                request.getCashBalance(),
                request.getTotalCostBasis(),
                request.getSnapshotDate()
        );

        // Check if snapshot already exists for this date
        snapshotValidationService.validateSnapshotDoesNotExist(account, request.getSnapshotDate());

        // Map request -> entity
        PortfolioSnapshot snapshot = PortfolioSnapshotMapper.toEntity(request, account);

        // Save to DB
        snapshot = snapshotRepository.save(snapshot);

        // Convert entity -> DTO
        return PortfolioSnapshotMapper.toDTO(snapshot);
    }

    // Get snapshot by ID
    @Override
    public PortfolioSnapshotDTO getSnapshotById(UUID snapshotId) {
        PortfolioSnapshot snapshot = snapshotValidationService.validateSnapshotExists(snapshotId);
        return PortfolioSnapshotMapper.toDTO(snapshot);
    }

    // Get all snapshots
    @Override
    public List<PortfolioSnapshotDTO> getAllSnapshots() {
        return snapshotRepository.findAll()
                .stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get snapshots by account
    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsByAccount(UUID accountId) {
        Account account = snapshotValidationService.validateAccountExists(accountId);
        return snapshotRepository.findByAccountOrderBySnapshotDateDesc(account)
                .stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get snapshots by date range
    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsByDateRange(UUID accountId, LocalDate startDate, LocalDate endDate) {
        Account account = snapshotValidationService.validateAccountExists(accountId);
        snapshotValidationService.validateDateRange(startDate, endDate);

        return snapshotRepository.findByAccountAndSnapshotDateBetween(account, startDate, endDate)
                .stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Get latest snapshot
    @Override
    public PortfolioSnapshotDTO getLatestSnapshot(UUID accountId) {
        Account account = snapshotValidationService.validateAccountExists(accountId);
        PortfolioSnapshot snapshot = snapshotRepository.findLatestByAccount(account)
                .orElseThrow(() -> new RuntimeException("No snapshots found for account ID: " + accountId));
        return PortfolioSnapshotMapper.toDTO(snapshot);
    }

    // Delete snapshot
    @Override
    public void deleteSnapshot(UUID snapshotId) {
        PortfolioSnapshot snapshot = snapshotValidationService.validateSnapshotExists(snapshotId);
        snapshotRepository.delete(snapshot);
    }

    // Get snapshots for account
    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsForAccount(UUID accountId) {
        // Fetch all snapshots for the account
        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository.findByAccount_AccountIdOrderBySnapshotDateDesc(accountId);

        // Map to DTOs
        List<PortfolioSnapshotDTO> snapshotDTOs = snapshots.stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());

        return snapshotDTOs;
    }


}