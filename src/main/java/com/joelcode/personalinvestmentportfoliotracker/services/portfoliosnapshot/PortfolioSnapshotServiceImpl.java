package com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.PortfolioSnapshotMapper;
import org.springframework.stereotype.Service;

import javax.sound.sampled.Port;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PortfolioSnapshotServiceImpl implements PortfolioSnapshotService {

    // Define key fields
    private final PortfolioSnapshotRepository snapshotRepository;
    private final PortfolioSnapshotValidationService snapshotValidationService;
    private final PortfolioSnapshotRepository portfolioSnapshotRepository;

    // Constructor
    public PortfolioSnapshotServiceImpl(PortfolioSnapshotRepository snapshotRepository,
                                        PortfolioSnapshotValidationService snapshotValidationService, PortfolioSnapshotRepository portfolioSnapshotRepository) {
        this.snapshotRepository = snapshotRepository;
        this.snapshotValidationService = snapshotValidationService;
        this.portfolioSnapshotRepository = portfolioSnapshotRepository;
    }

    // Create snapshot entity from request dto
    @Override
    public PortfolioSnapshotDTO createSnapshot(PortfolioSnapshotCreateRequest request) {

        // Validate fields and relationships
        Account account = snapshotValidationService.validateAccountExists(request.getAccountId());
        snapshotValidationService.validateCreateRequest(
                request.getTotalValue(),
                request.getCashBalance(),
                request.getTotalInvested(),
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

    @Override
    public PortfolioSnapshotDTO getSnapshotById(UUID snapshotId) {
        PortfolioSnapshot snapshot = snapshotValidationService.validateSnapshotExists(snapshotId);
        return PortfolioSnapshotMapper.toDTO(snapshot);
    }

    @Override
    public List<PortfolioSnapshotDTO> getAllSnapshots() {
        return snapshotRepository.findAll()
                .stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsByAccount(UUID accountId) {
        Account account = snapshotValidationService.validateAccountExists(accountId);
        return snapshotRepository.findByAccountOrderBySnapshotDateDesc(account)
                .stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsByDateRange(UUID accountId, LocalDate startDate, LocalDate endDate) {
        Account account = snapshotValidationService.validateAccountExists(accountId);
        snapshotValidationService.validateDateRange(startDate, endDate);

        return snapshotRepository.findByAccountAndSnapshotDateBetween(account, startDate, endDate)
                .stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioSnapshotDTO getLatestSnapshot(UUID accountId) {
        Account account = snapshotValidationService.validateAccountExists(accountId);
        PortfolioSnapshot snapshot = snapshotRepository.findLatestByAccount(account)
                .orElseThrow(() -> new RuntimeException("No snapshots found for account ID: " + accountId));
        return PortfolioSnapshotMapper.toDTO(snapshot);
    }

    @Override
    public void deleteSnapshot(UUID snapshotId) {
        PortfolioSnapshot snapshot = snapshotValidationService.validateSnapshotExists(snapshotId);
        snapshotRepository.delete(snapshot);
    }

    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsForUser(UUID userId) {
        // Fetch all snapshots for the user
        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository.findByUser_IdOrderByDateDesc(userId);

        // Map to DTOs
        List<PortfolioSnapshotDTO> snapshotDTOs = snapshots.stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());

        return snapshotDTOs;
    }

    @Override
    public List<PortfolioSnapshotDTO> getSnapshotsForAccount(UUID accountId) {
        // Fetch all snapshots for the account
        List<PortfolioSnapshot> snapshots = portfolioSnapshotRepository.findByAccount_IdOrderByDateDesc(accountId);

        // Map to DTOs
        List<PortfolioSnapshotDTO> snapshotDTOs = snapshots.stream()
                .map(PortfolioSnapshotMapper::toDTO)
                .collect(Collectors.toList());

        return snapshotDTOs;
    }


}