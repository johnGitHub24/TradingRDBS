package com.trading.rdbs.symbol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymbolRequest {

    @NotBlank
    @Size(max = 16)
    private String ticker;

    @NotBlank
    @Size(max = 200)
    private String companyName;

    @NotBlank
    @Size(max = 16)
    private String exchangeCode;
}
