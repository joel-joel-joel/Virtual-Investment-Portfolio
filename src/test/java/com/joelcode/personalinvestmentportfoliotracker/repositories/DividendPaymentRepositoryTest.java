package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DividendPaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DividendPaymentRepository dividendPaymentRepository;

    private Account testAccount;
    private Stock testStock;
    private Dividend testDividend;
    private DividendPayment testPayment;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        entityManager.persistAndFlush(user);

        testAccount = new Account();
        testAccount.setAccountName("Test Account");
        testAccount.setUser(user);
        entityManager.persistAndFlush(testAccount);

        testStock = new Stock();
        testStock.setStockCode("AAPL");
        testStock.setCompanyName("Apple");
        entityManager.persistAndFlush(testStock);

        testDividend = new Dividend();
        testDividend.setAmountPerShare(BigDecimal.valueOf(2.5));
        testDividend.setPayDate(LocalDateTime.now());
        testDividend.setStock(testStock);
        entityManager.persistAndFlush(testDividend);

        testPayment = new DividendPayment();
        testPayment.setAccount(testAccount);
        testPayment.setStock(testStock);
        testPayment.setDividend(testDividend);
        testPayment.setShareQuantity(BigDecimal.valueOf(100));
        testPayment.setPaymentDate(LocalDateTime.now());
        testPayment.setTotalDividendAmount(BigDecimal.valueOf(250));
        testPayment.setStatus(DividendPayment.PaymentStatus.PAID);
        entityManager.persistAndFlush(testPayment);
    }

    @Test
    void testFindByAccount_AccountId_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findByAccount_AccountId(testAccount.getAccountId());

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getAccount().getAccountId().equals(testAccount.getAccountId())));
    }

    @Test
    void testFindByStock_StockId_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findByStock_StockId(testStock.getStockId());

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getStock().getStockId().equals(testStock.getStockId())));
    }

    @Test
    void testFindByDividend_DividendId_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findByDividend_DividendId(testDividend.getDividendId());

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getDividend().getDividendId().equals(testDividend.getDividendId())));
    }

    @Test
    void testFindByAccount_AccountIdAndDividend_DividendId_Success() {
        // Act
        Optional<DividendPayment> result = dividendPaymentRepository.findByAccount_AccountIdAndDividend_DividendId(
                testAccount.getAccountId(),
                testDividend.getDividendId()
        );

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPayment.getPaymentId(), result.get().getPaymentId());
    }

    @Test
    void testExistsByAccountAndDividend_Success() {
        // Act
        boolean exists = dividendPaymentRepository.existsByAccountAndDividend(testAccount, testDividend);

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByAccountAndDividend_NotFound() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setAccountName("New Account");
        newAccount.setUser(testAccount.getUser());
        entityManager.persistAndFlush(newAccount);

        // Act
        boolean exists = dividendPaymentRepository.existsByAccountAndDividend(newAccount, testDividend);

        // Assert
        assertFalse(exists);
    }

    @Test
    void testFindPaymentsByAccountOrderByDate_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findPaymentsByAccountOrderByDate(testAccount.getAccountId());

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(testPayment.getPaymentId(), results.get(0).getPaymentId());
    }

    @Test
    void testFindPaymentsByAccountAndDateRange_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // Act
        List<DividendPayment> results = dividendPaymentRepository.findPaymentsByAccountAndDateRange(
                testAccount.getAccountId(),
                start,
                end
        );

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getAccount().getAccountId().equals(testAccount.getAccountId())));
    }

    @Test
    void testFindPaymentsByAccountAndStock_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findPaymentsByIdAccountAndStockId(
                testAccount.getAccountId(),
                testStock.getStockId()
        );

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(p -> p.getStock().getStockId().equals(testStock.getStockId())));
    }

    @Test
    void testCalculateTotalDividendsByAccount_Success() {
        // Act
        BigDecimal total = dividendPaymentRepository.calculateTotalDividendsByAccount(testAccount.getAccountId());

        // Assert
        assertNotNull(total);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateTotalDividendsByAccountAndStock_Success() {
        // Act
        BigDecimal total = dividendPaymentRepository.calculateTotalDividendsByAccountAndStock(
                testAccount.getAccountId(),
                testStock.getStockId()
        );

        // Assert
        assertNotNull(total);
        assertTrue(total.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateDividendsInDateRange_Success() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // Act
        BigDecimal total = dividendPaymentRepository.calculateDividendsInDateRange(
                testAccount.getAccountId(),
                start,
                end
        );

        // Assert
        assertNotNull(total);
        assertTrue(total.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testFindPaymentsByAccountAndStatus_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findPaymentsByAccountAndStatus(
                testAccount.getAccountId(),
                DividendPayment.PaymentStatus.PAID
        );

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(p -> p.getStatus() == DividendPayment.PaymentStatus.PAID));
    }

    @Test
    void testFindPaymentsByAccountAndStatus_Empty() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findPaymentsByAccountAndStatus(
                testAccount.getAccountId(),
                DividendPayment.PaymentStatus.PENDING
        );

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    void testFindByStatus_Success() {
        // Act
        List<DividendPayment> results = dividendPaymentRepository.findByStatus(DividendPayment.PaymentStatus.PAID);

        // Assert
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(p -> p.getStatus() == DividendPayment.PaymentStatus.PAID));
    }

    @Test
    void testCountPaymentsByAccount_Success() {
        // Act
        Long count = dividendPaymentRepository.countPaymentsByAccount(testAccount.getAccountId());

        // Assert
        assertNotNull(count);
        assertTrue(count > 0);
        assertEquals(1L, count);
    }

    @Test
    void testCountPaymentsByAccount_Zero() {
        // Arrange
        Account newAccount = new Account();
        newAccount.setAccountName("Empty Account");
        newAccount.setUser(testAccount.getUser());
        entityManager.persistAndFlush(newAccount);

        // Act
        Long count = dividendPaymentRepository.countPaymentsByAccount(newAccount.getAccountId());

        // Assert
        assertEquals(0L, count);
    }
}