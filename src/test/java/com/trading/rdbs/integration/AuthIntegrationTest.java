package com.trading.rdbs.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.rdbs.support.IntegrationTestSupport;
import com.trading.rdbs.support.RdbsTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】OAuth Bearer JWT 整合測試（AUTH-001～002）。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth Integration Tests (AUTH-001～002)")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("AUTH-001 login returns Bearer token")
    void login_validCredentials_returnsJwt() throws Exception {
        String body = RdbsTestFixtures.loadJson("auth", "AUTH-001-SUCCESS");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("demo"));
    }

    @Test
    @DisplayName("AUTH-002 protected API without token returns 401")
    void accounts_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-002 bad credentials returns 401")
    void login_badCredentials_returns401() throws Exception {
        String body = RdbsTestFixtures.loadJson("auth", "AUTH-002-BAD-CREDENTIALS");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTH-001 token grants access to accounts API")
    void accounts_withBearerToken_returns200() throws Exception {
        String token = IntegrationTestSupport.loginWithFixture(mockMvc, objectMapper, "AUTH-001-SUCCESS");
        mockMvc.perform(get("/api/v1/accounts")
                        .header("Authorization", IntegrationTestSupport.bearer(token)))
                .andExpect(status().isOk());
    }
}
