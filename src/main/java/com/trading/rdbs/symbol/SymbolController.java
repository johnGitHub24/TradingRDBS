package com.trading.rdbs.symbol;

import com.trading.rdbs.symbol.dto.SymbolRequest;
import com.trading.rdbs.symbol.dto.SymbolResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/symbols")
@RequiredArgsConstructor
public class SymbolController {

    private final SymbolService symbolService;

    @GetMapping
    public List<SymbolResponse> listSymbols() {
        return symbolService.listSymbols();
    }

    @GetMapping("/{id}")
    public SymbolResponse getSymbol(@PathVariable Long id) {
        return symbolService.getSymbol(id);
    }

    @PostMapping
    public ResponseEntity<SymbolResponse> createSymbol(@Valid @RequestBody SymbolRequest request) {
        SymbolResponse created = symbolService.createSymbol(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
