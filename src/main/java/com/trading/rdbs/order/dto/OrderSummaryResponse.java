package com.trading.rdbs.order.dto;

import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.domain.OrderSide;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderSummaryResponse(
        Long id,
        String ticker,
        OrderSide side,
        Integer quantity,
        BigDecimal unitPrice,
        LocalDateTime createdAt
) {
    public static OrderSummaryResponse from(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .ticker(order.getSymbol().getTicker())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
