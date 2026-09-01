package com.trading.rdbs.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 【職責】啟動框線：disabled 不印；static 前台印首頁與 runner。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("StartupInfoLogger unit")
class StartupInfoLoggerTest {

    @Mock
    private ApplicationReadyEvent event;
    @Mock
    private ConfigurableApplicationContext applicationContext;
    @Mock
    private ConfigurableEnvironment env;

    private final StartupInfoLogger logger = new StartupInfoLogger();

    @Test
    @DisplayName("enabled=false → prints nothing")
    void disabled_printsNothing() {
        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("startup.info.enabled", Boolean.class, true)).thenReturn(false);

        String out = captureStdout(() -> logger.onApplicationEvent(event));

        assertThat(out).doesNotContain("localhost");
    }

    @Test
    @DisplayName("static frontend → prints homepage on :8095")
    void static_printsHome() {
        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("startup.info.enabled", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.project-name", "TradingRDBS")).thenReturn("TradingRDBS");
        when(env.getProperty("server.port", "8095")).thenReturn("8095");
        when(env.getProperty("startup.info.frontend", "none")).thenReturn("static");
        when(env.getProperty("startup.info.h2", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.api-docs", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.probe", Boolean.class, true)).thenReturn(false);
        when(env.getProperty("startup.info.home-path", "/")).thenReturn("/");
        when(env.getProperty("startup.info.extra-paths[0]")).thenReturn(null);
        when(env.getProperty("springdoc.api-docs.path", "/v3/api-docs")).thenReturn("/v3/api-docs");
        when(env.getProperty("spring.datasource.url", "jdbc:h2:mem:rdbs"))
                .thenReturn("jdbc:h2:mem:rdbs");

        String out = captureStdout(() -> logger.onApplicationEvent(event));

        assertThat(out).contains("TradingRDBS 後端已啟動");
        assertThat(out).contains("http://localhost:8095/");
        assertThat(out).contains("/test/runner.html");
        assertThat(out).contains("jdbc:h2:mem:rdbs");
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
