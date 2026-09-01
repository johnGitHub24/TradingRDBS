package com.trading.rdbs.order.dto;

import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.domain.OrderSide;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderResponse(
        Long id,
        Long accountId,
        String accountNo,
        Long symbolId,
        String ticker,
        OrderSide side,
        Integer quantity,
        BigDecimal unitPrice,
        LocalDateTime createdAt
) {
    /**
     * 【概念】回應 DTO 可 JOIN 帶出帳號／代碼供 API 閱讀；持久層仍只存 FK（3NF）。
     */
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .accountId(order.getAccount().getId())
                .accountNo(order.getAccount().getAccountNo())
                .symbolId(order.getSymbol().getId())
                .ticker(order.getSymbol().getTicker())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
