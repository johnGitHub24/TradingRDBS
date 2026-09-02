package com.trading.rdbs.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】整合測試 OAuth 登入輔助。
 */
public final class IntegrationTestSupport {

    private IntegrationTestSupport() {
    }

    public static String loginAndGetToken(MockMvc mockMvc, ObjectMapper objectMapper,
                                          String username, String password) throws Exception {
        String body = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(response);
        return node.get("token").asText();
    }

    public static String loginWithFixture(MockMvc mockMvc, ObjectMapper objectMapper,
                                          String caseId) throws Exception {
        JsonNode creds = objectMapper.readTree(RdbsTestFixtures.loadJson("auth", caseId));
        return loginAndGetToken(mockMvc, objectMapper,
                creds.get("username").asText(), creds.get("password").asText());
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}
