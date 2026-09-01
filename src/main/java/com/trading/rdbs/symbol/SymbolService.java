package com.trading.rdbs.symbol;

import com.trading.rdbs.common.ResourceNotFoundException;
import com.trading.rdbs.symbol.domain.Symbol;
import com.trading.rdbs.symbol.dto.SymbolRequest;
import com.trading.rdbs.symbol.dto.SymbolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SymbolService {

    private final SymbolRepository symbolRepository;

    @Transactional(readOnly = true)
    public List<SymbolResponse> listSymbols() {
        return symbolRepository.findAll().stream()
                .map(SymbolResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SymbolResponse getSymbol(Long id) {
        return SymbolResponse.from(findSymbolOrThrow(id));
    }

    @Transactional
    public SymbolResponse createSymbol(SymbolRequest request) {
        Symbol symbol = Symbol.builder()
                .ticker(request.getTicker())
                .companyName(request.getCompanyName())
                .exchangeCode(request.getExchangeCode())
                .build();
        return SymbolResponse.from(symbolRepository.save(symbol));
    }

    Symbol findSymbolOrThrow(Long id) {
        return symbolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Symbol not found with id: " + id));
    }
}
