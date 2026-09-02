package com.trading.rdbs.config;

import com.trading.rdbs.auth.AppUserRepository;
import com.trading.rdbs.auth.AppUserService;
import com.trading.rdbs.auth.domain.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 【職責】啟動時種子 OAuth 使用者（admin + demo）。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthDataSeeder {

    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.demo.username}")
    private String demoUsername;

    @Value("${app.demo.password}")
    private String demoPassword;

    @Bean
    CommandLineRunner seedAuthUsers() {
        return args -> {
            if (appUserRepository.count() > 0) {
                log.info("AuthDataSeeder: users exist, skip.");
                return;
            }
            appUserService.register(adminUsername, adminPassword, Set.of(Role.ADMIN, Role.USER));
            appUserService.register(demoUsername, demoPassword, Set.of(Role.USER));
            log.info("AuthDataSeeder: seeded admin={} demo={}", adminUsername, demoUsername);
        };
    }
}
