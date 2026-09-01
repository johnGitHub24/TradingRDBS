package com.trading.rdbs.symbol.dto;

import com.trading.rdbs.symbol.domain.Symbol;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SymbolResponse(
        Long id,
        String ticker,
        String companyName,
        String exchangeCode,
        LocalDateTime createdAt
) {
    public static SymbolResponse from(Symbol symbol) {
        return SymbolResponse.builder()
                .id(symbol.getId())
                .ticker(symbol.getTicker())
                .companyName(symbol.getCompanyName())
                .exchangeCode(symbol.getExchangeCode())
                .createdAt(symbol.getCreatedAt())
                .build();
    }
}
