package com.trading.rdbs.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【職責】Demo-ready 靜態資產整合探針（系統檢測 Panel + 藍圖）。
 * 【概念】對齊 EOS service-verification-panel.md L0 閉環。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "startup.info.enabled=false")
@DisplayName("Demo-ready static (SVP + blueprint)")
class DemoReadyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SVP-DEMO-001 service-links.manifest.json is public")
    void serviceLinksManifest_ok() throws Exception {
        mockMvc.perform(get("/service-links.manifest.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.groups[0].title").exists());
    }

    @Test
    @DisplayName("SVP-DEMO-002 service-verification-panel.js is public")
    void serviceVerificationPanelJs_ok() throws Exception {
        mockMvc.perform(get("/service-verification-panel.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("eos-svp-mount")));
    }

    @Test
    @DisplayName("SVP-DEMO-003 /blueprint/ redirects to index.html")
    void blueprintSlash_redirects() throws Exception {
        mockMvc.perform(get("/blueprint/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/blueprint/index.html"));
    }

    @Test
    @DisplayName("SVP-DEMO-004 /blueprint/index.html serves Mermaid page")
    void blueprintIndex_ok() throws Exception {
        mockMvc.perform(get("/blueprint/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("mermaid")));
    }

    @Test
    @DisplayName("SVP-DEMO-005 index.html includes SVP mount and blueprint link")
    void homeIncludesSvpHeader() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("eos-svp-mount")))
                .andExpect(content().string(containsString("/blueprint/")));
    }

    @Test
    @DisplayName("SVP-DEMO-006 test-reports hub FinTechDemo style")
    void testReportsHub_ok() throws Exception {
        mockMvc.perform(get("/docs/portals/test-reports.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"unit\"")))
                .andExpect(content().string(containsString("id=\"integration\"")))
                .andExpect(content().string(containsString("mod-grid")));
    }

    @Test
    @DisplayName("SVP-DEMO-007 /docs/reports/unit/ redirects to index.html")
    void unitReportRedirect_ok() throws Exception {
        mockMvc.perform(get("/docs/reports/unit/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/docs/reports/unit/index.html"));
    }

    @Test
    @DisplayName("SVP-DEMO-008 /docs/reports/integration/ redirects to index.html")
    void integrationReportRedirect_ok() throws Exception {
        mockMvc.perform(get("/docs/reports/integration/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/docs/reports/integration/index.html"));
    }
}
