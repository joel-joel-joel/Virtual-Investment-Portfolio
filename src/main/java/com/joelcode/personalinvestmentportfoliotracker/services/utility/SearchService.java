package com.joelcode.personalinvestmentportfoliotracker.services.utility;

import com.joelcode.personalinvestmentportfoliotracker.dto.utility.SearchDTO;

import java.util.UUID;

public interface SearchService {

    SearchDTO search(String query, UUID userId);
}
