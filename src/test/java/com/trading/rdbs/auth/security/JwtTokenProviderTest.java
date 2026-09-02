package com.trading.rdbs.auth.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Unit Tests (AUTH-003)")
class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(
            "TradingRDBS-unit-test-secret-at-least-32b", 3_600_000);

    @Test
    @DisplayName("AUTH-003 generate and validate token with roles")
    void generateAndValidate_roundTrip() {
        String token = provider.generateToken("demo", List.of("ROLE_USER"));
        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("demo");
        assertThat(provider.getRoles(token)).containsExactly("ROLE_USER");
    }

    @Test
    @DisplayName("AUTH-003 tampered token fails validation")
    void validate_tamperedToken_returnsFalse() {
        String token = provider.generateToken("demo", List.of("ROLE_USER"));
        assertThat(provider.validateToken(token + "x")).isFalse();
    }
}
