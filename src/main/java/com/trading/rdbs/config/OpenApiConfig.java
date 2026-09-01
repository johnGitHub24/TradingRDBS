package com.trading.rdbs.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tradingRdbsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradingRDBS API")
                        .description("3NF relational model: Account (1) -> Order (N) -> Symbol (1). "
                                + "H2 console at /h2-console (dev).")
                        .version("0.1.0-SNAPSHOT"));
    }
}
