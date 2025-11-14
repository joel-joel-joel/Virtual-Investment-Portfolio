package com.joelcode.personalinvestmentportfoliotracker.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;

public class DividendMapper {

    // Convert dividend creation request DTO to entity
    public static Dividend toEntity(DividendCreateRequest request) {
        Dividend dividend = new Dividend();
        dividend.setDividendId(request.getStockId());
        dividend.setAmountPerShare(request.getAmountPerShare());
        dividend.setPayDate(request.getPayDate());
        return dividend;
    }

    // Convert dividend entity to response account DTO
    public static DividendDTO toDTO(Dividend dividend) {
        if (dividend == null) return null;
        return new DividendDTO(
                dividend.getDividendId(),
                dividend.getStock().getStockId(),
                dividend.getAmountPerShare(),
                dividend.getPayDate());
    }
}
