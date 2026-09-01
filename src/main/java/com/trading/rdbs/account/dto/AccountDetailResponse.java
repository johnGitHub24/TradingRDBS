package com.trading.rdbs.account.dto;

import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.order.dto.OrderSummaryResponse;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AccountDetailResponse(
        Long id,
        String accountNo,
        String ownerName,
        LocalDateTime createdAt,
        List<OrderSummaryResponse> orders
) {
    public static AccountDetailResponse from(Account account, List<OrderSummaryResponse> orders) {
        return AccountDetailResponse.builder()
                .id(account.getId())
                .accountNo(account.getAccountNo())
                .ownerName(account.getOwnerName())
                .createdAt(account.getCreatedAt())
                .orders(orders)
                .build();
    }
}
