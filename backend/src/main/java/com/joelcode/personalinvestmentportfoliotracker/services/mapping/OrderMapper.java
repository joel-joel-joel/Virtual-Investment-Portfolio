package com.joelcode.personalinvestmentportfoliotracker.services.mapping;

import com.joelcode.personalinvestmentportfoliotracker.dto.order.OrderDTO;
import com.joelcode.personalinvestmentportfoliotracker.entities.Order;
import com.joelcode.personalinvestmentportfoliotracker.entities.Stock;

public class OrderMapper {

    /**
     * Convert Order entity to OrderDTO
     */
    public static OrderDTO toDTO(Order order, Stock stock) {
        if (order == null) {
            return null;
        }

        return new OrderDTO(
                order.getOrderId(),
                order.getStock().getStockId(),
                stock.getStockCode(),
                order.getAccount().getAccountId(),
                order.getOrderType(),
                order.getQuantity(),
                order.getLimitPrice(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getExecutedAt(),
                order.getCancelledAt(),
                order.getFailureReason()
        );
    }
}
