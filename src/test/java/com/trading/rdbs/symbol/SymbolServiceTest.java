package com.trading.rdbs.symbol;

import com.trading.rdbs.symbol.domain.Symbol;
import com.trading.rdbs.symbol.dto.SymbolRequest;
import com.trading.rdbs.symbol.dto.SymbolResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("SymbolService Unit Tests (RDBS-002)")
class SymbolServiceTest {

    @Mock
    private SymbolRepository symbolRepository;

    @InjectMocks
    private SymbolService symbolService;

    @Test
    @DisplayName("RDBS-002 create symbol")
    void createSymbol_savesAndReturns() {
        SymbolRequest request = new SymbolRequest();
        request.setTicker("2330");
        request.setCompanyName("TSMC");
        request.setExchangeCode("TWSE");

        given(symbolRepository.save(org.mockito.ArgumentMatchers.any(Symbol.class)))
                .willAnswer(inv -> {
                    Symbol s = inv.getArgument(0);
                    s.setId(1L);
                    s.setCreatedAt(LocalDateTime.now());
                    return s;
                });

        SymbolResponse response = symbolService.createSymbol(request);

        assertThat(response.ticker()).isEqualTo("2330");
        assertThat(response.id()).isEqualTo(1L);
    }
}
