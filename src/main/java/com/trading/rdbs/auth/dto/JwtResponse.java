package com.trading.rdbs.auth.dto;

import java.util.List;

/**
 * 【職責】OAuth 2.0 Bearer Token 登入回應（RFC 6750）。
 */
public record JwtResponse(
        String token,
        String tokenType,
        String username,
        List<String> roles
) {
    public static JwtResponse bearer(String token, String username, List<String> roles) {
        return new JwtResponse(token, "Bearer", username, roles);
    }
}
