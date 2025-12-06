package com.joelcode.personalinvestmentportfoliotracker.repositories;

import com.joelcode.personalinvestmentportfoliotracker.entities.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, UUID> {

    List<PriceAlert> findByUserId(UUID userId);

    List<PriceAlert> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    List<PriceAlert> findByStockId(UUID stockId);

    void deleteByUserIdAndAlertId(UUID userId, UUID alertId);
}
