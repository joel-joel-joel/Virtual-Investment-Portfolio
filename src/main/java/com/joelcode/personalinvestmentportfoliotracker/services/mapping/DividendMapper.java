package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendCreateRequest;
import com.joelcode.personalinvestmentportfoliotracker.dto.dividend.DividendDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Dividend;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;
import org.springframework.stereotype.Component;

@Component
public class DividendMapper {

    public static DividendDTO toDTO(Dividend dividend) {
        if (dividend == null) {
            return null;
        }
        return new DividendDTO(dividend);
    }

    public static Dividend toEntity(DividendCreateRequest request, Stock stock) {
        if (request == null) {
            return null;
        }

        return new Dividend(
                request.getDividendPerShare(),
                request.getPayDate(),
                stock
        );
    }
}
