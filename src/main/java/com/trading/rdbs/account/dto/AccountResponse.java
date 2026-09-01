package com.trading.rdbs.account.dto;

import com.trading.rdbs.account.domain.Account;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AccountResponse(
        Long id,
        String accountNo,
        String ownerName,
        LocalDateTime createdAt
) {
    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNo(account.getAccountNo())
                .ownerName(account.getOwnerName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
