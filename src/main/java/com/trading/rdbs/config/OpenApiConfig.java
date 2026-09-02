package com.trading.rdbs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI tradingRdbsOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("TradingRDBS API")
            .description("3NF relational model with OAuth 2.0 Bearer JWT. "
                + "Login at POST /api/v1/auth/login, then use Authorization: Bearer <token>.")
            .version("0.2.0-SNAPSHOT"))
        .components(new Components().addSecuritySchemes(BEARER_SCHEME,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("OAuth 2.0 Bearer Token from /api/v1/auth/login")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
  }
}
