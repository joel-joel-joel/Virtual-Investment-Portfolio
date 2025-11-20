package com.joelcode.personalinvestmentportfoliotracker.services;

import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.portfoliosnapshot.PortfolioSnapshotDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import com.joelcode.personalinvestmentportfoliotracker.entities.PortfolioSnapshot;
import com.joelcode.personalinvestmentportfoliotracker.repositories.PortfolioSnapshotRepository;
import com.joelcode.personalinvestmentportfoliotracker.services.mapping.PortfolioSnapshotMapper;
import com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot.PortfolioSnapshotServiceImpl;
import com.joelcode.personalinvestmentportfoliotracker.services.portfoliosnapshot.PortfolioSnapshotValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Testing PortfolioSnapshot service layer business logic
public class PortfolioSnapshotServiceImplTest {

    // Define mock key fields
    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Mock
    private PortfolioSnapshotValidationService snapshotValidationService;

    @Mock
    private PortfolioSnapshotRepository portfolioSnapshotRepository;

    @InjectMocks
    private PortfolioSnapshotServiceImpl snapshotService;

    private PortfolioSnapshot testSnapshot;
    private Account testAccount;
    private UUID snapshotId;
    private UUID accountId;
    private UUID userId;

    // Set up a test snapshot and account
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        snapshotId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        userId = UUID.randomUUID();

        testAccount = new Account();
        testAccount.setAccountId(accountId);
        testAccount.setUserId(userId);

        testSnapshot = new PortfolioSnapshot();
        testSnapshot.setSnapshotId(snapshotId);
        testSnapshot.setAccount(testAccount);
        testSnapshot.setTotalValue(BigDecimal.valueOf(1000));
        testSnapshot.setCashBalance(BigDecimal.valueOf(200));
        testSnapshot.setTotalCostBasis(BigDecimal.valueOf(800));
        testSnapshot.setSnapshotDate(LocalDate.now());
    }

    // Test snapshot creation
    @Test
    void testCreateSnapshot_Success() {
        PortfolioSnapshotCreateRequest request = new PortfolioSnapshotCreateRequest();
        request.setAccountId(accountId);
        request.setTotalValue(BigDecimal.valueOf(1000));
        request.setCashBalance(BigDecimal.valueOf(200));
        request.setTotalCostBasis(BigDecimal.valueOf(800));
        request.setSnapshotDate(LocalDate.now());

        when(snapshotValidationService.validateAccountExists(accountId)).thenReturn(testAccount);
        doNothing().when(snapshotValidationService).validateCreateRequest(
                request.getTotalValue(), request.getCashBalance(), request.getTotalCostBasis(), request.getSnapshotDate()
        );
        doNothing().when(snapshotValidationService).validateSnapshotDoesNotExist(testAccount, request.getSnapshotDate());
        when(snapshotRepository.save(any(PortfolioSnapshot.class))).thenReturn(testSnapshot);

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toEntity(request, testAccount)).thenReturn(testSnapshot);
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            PortfolioSnapshotDTO result = snapshotService.createSnapshot(request);

            assertNotNull(result);
            verify(snapshotRepository, times(1)).save(any(PortfolioSnapshot.class));
        }
    }

    // Test getting snapshot by ID
    @Test
    void testGetSnapshotById_ReturnsCorrectDTO() {
        when(snapshotValidationService.validateSnapshotExists(snapshotId)).thenReturn(testSnapshot);

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            PortfolioSnapshotDTO result = snapshotService.getSnapshotById(snapshotId);
            assertNotNull(result);
        }
    }

    // Test getting all snapshots
    @Test
    void testGetAllSnapshots_ReturnsCorrectList() {
        when(snapshotRepository.findAll()).thenReturn(List.of(testSnapshot));

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            List<PortfolioSnapshotDTO> result = snapshotService.getAllSnapshots();
            assertEquals(1, result.size());
        }
    }

    // Test deleting a snapshot
    @Test
    void testDeleteSnapshot_Success() {
        when(snapshotValidationService.validateSnapshotExists(snapshotId)).thenReturn(testSnapshot);

        snapshotService.deleteSnapshot(snapshotId);

        verify(snapshotRepository, times(1)).delete(testSnapshot);
    }

    // Test getting snapshots by account
    @Test
    void testGetSnapshotsByAccount_ReturnsCorrectList() {
        when(snapshotValidationService.validateAccountExists(accountId)).thenReturn(testAccount);
        when(snapshotRepository.findByAccountOrderBySnapshotDateDesc(testAccount)).thenReturn(List.of(testSnapshot));

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            List<PortfolioSnapshotDTO> result = snapshotService.getSnapshotsByAccount(accountId);
            assertEquals(1, result.size());
        }
    }

    // Test getting snapshots by date range
    @Test
    void testGetSnapshotsByDateRange_ReturnsCorrectList() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now();

        when(snapshotValidationService.validateAccountExists(accountId)).thenReturn(testAccount);
        doNothing().when(snapshotValidationService).validateDateRange(startDate, endDate);
        when(snapshotRepository.findByAccountAndSnapshotDateBetween(testAccount, startDate, endDate))
                .thenReturn(List.of(testSnapshot));

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            List<PortfolioSnapshotDTO> result = snapshotService.getSnapshotsByDateRange(accountId, startDate, endDate);
            assertEquals(1, result.size());
        }
    }

    // Test getting latest snapshot
    @Test
    void testGetLatestSnapshot_ReturnsCorrectDTO() {
        when(snapshotValidationService.validateAccountExists(accountId)).thenReturn(testAccount);
        when(snapshotRepository.findLatestByAccount(testAccount)).thenReturn(Optional.of(testSnapshot));

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            PortfolioSnapshotDTO result = snapshotService.getLatestSnapshot(accountId);
            assertNotNull(result);
        }
    }

    // Test getting snapshots for account
    @Test
    void testGetSnapshotsForAccount_ReturnsCorrectList() {
        when(portfolioSnapshotRepository.findByAccount_IdOrderBySnapshotDateDesc(accountId)).thenReturn(List.of(testSnapshot));

        try (MockedStatic<PortfolioSnapshotMapper> mapperMock = Mockito.mockStatic(PortfolioSnapshotMapper.class)) {
            mapperMock.when(() -> PortfolioSnapshotMapper.toDTO(testSnapshot)).thenReturn(new PortfolioSnapshotDTO());

            List<PortfolioSnapshotDTO> result = snapshotService.getSnapshotsForAccount(accountId);
            assertEquals(1, result.size());
        }
    }
}
