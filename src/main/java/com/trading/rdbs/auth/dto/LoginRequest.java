package com.trading.rdbs.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username 不可為空") String username,
        @NotBlank(message = "password 不可為空") String password
) {
}
