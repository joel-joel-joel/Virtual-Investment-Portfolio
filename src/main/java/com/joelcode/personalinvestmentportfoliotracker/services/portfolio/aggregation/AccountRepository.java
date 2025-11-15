package com.joelcode.personalinvestmentportfoliotracker.services.portfolio.aggregation;

import com.joelcode.personalinvestmentportfoliotracker.entities.Account;
import org.springframework.data.repository.Repository;

import java.util.UUID;

interface AccountRepository extends Repository<Account, UUID> {
}
