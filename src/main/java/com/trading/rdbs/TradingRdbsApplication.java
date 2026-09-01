package com.trading.rdbs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 【職責】TradingRDBS 應用程式進入點。
 * 【概念】關聯式資料庫練習：第三正規化（3NF）＋ Account(1)→Order(N)→Symbol(1) JPA 映射。
 * 【IntelliJ】請用 Run → {@code TradingRdbsApplication}（Gradle bootRun），勿用 Spring Boot 綠箭頭（Windows 易 0xC0000005）。
 *           見 {@code docs/IntelliJ-IDE-啟動設定.md}。
 */
@SpringBootApplication
public class TradingRdbsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingRdbsApplication.class, args);
    }
}
