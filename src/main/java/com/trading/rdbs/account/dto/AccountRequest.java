package com.trading.rdbs.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequest {

    @NotBlank
    @Size(max = 32)
    private String accountNo;

    @NotBlank
    @Size(max = 100)
    private String ownerName;
}
