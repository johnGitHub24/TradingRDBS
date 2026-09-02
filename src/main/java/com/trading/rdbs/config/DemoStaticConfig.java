package com.trading.rdbs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 【職責】同埠 Demo 靜態資源路由（藍圖、文件入口）。
 * 【技巧】Spring Boot 子目錄 static 不自動解析 index；/blueprint/ 需明確導向。
 */
@Configuration
public class DemoStaticConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/blueprint", "/blueprint/index.html");
        registry.addRedirectViewController("/blueprint/", "/blueprint/index.html");
        registry.addRedirectViewController("/docs/reports/unit", "/docs/reports/unit/index.html");
        registry.addRedirectViewController("/docs/reports/unit/", "/docs/reports/unit/index.html");
        registry.addRedirectViewController("/docs/reports/integration", "/docs/reports/integration/index.html");
        registry.addRedirectViewController("/docs/reports/integration/", "/docs/reports/integration/index.html");
    }
}
