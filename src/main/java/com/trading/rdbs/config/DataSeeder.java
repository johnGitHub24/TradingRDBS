package com.trading.rdbs.config;

import com.trading.rdbs.account.AccountRepository;
import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.order.OrderRepository;
import com.trading.rdbs.order.domain.Order;
import com.trading.rdbs.order.domain.OrderSide;
import com.trading.rdbs.symbol.SymbolRepository;
import com.trading.rdbs.symbol.domain.Symbol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * 【職責】啟動時插入 3NF 範例資料：2 帳戶、3 標的、4 筆委託（展示 1→N→1）。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
            AccountRepository accountRepository,
            SymbolRepository symbolRepository,
            OrderRepository orderRepository) {
        return args -> {
            if (accountRepository.count() > 0) {
                log.info("DataSeeder: data exists, skip.");
                return;
            }

            Account alice = accountRepository.save(Account.builder()
                    .accountNo("ACC-001")
                    .ownerName("Alice Chen")
                    .build());
            Account bob = accountRepository.save(Account.builder()
                    .accountNo("ACC-002")
                    .ownerName("Bob Lin")
                    .build());

            Symbol tsmc = symbolRepository.save(Symbol.builder()
                    .ticker("2330")
                    .companyName("Taiwan Semiconductor")
                    .exchangeCode("TWSE")
                    .build());
            Symbol honHai = symbolRepository.save(Symbol.builder()
                    .ticker("2317")
                    .companyName("Hon Hai Precision")
                    .exchangeCode("TWSE")
                    .build());
            Symbol nvidia = symbolRepository.save(Symbol.builder()
                    .ticker("NVDA")
                    .companyName("NVIDIA Corporation")
                    .exchangeCode("NASDAQ")
                    .build());

            List<Order> orders = List.of(
                    order(alice, tsmc, OrderSide.BUY, 1000, "580.0000"),
                    order(alice, nvidia, OrderSide.BUY, 50, "120.5000"),
                    order(bob, tsmc, OrderSide.SELL, 200, "585.0000"),
                    order(bob, honHai, OrderSide.BUY, 500, "105.2500")
            );
            orderRepository.saveAll(orders);
            log.info("DataSeeder: {} accounts, {} symbols, {} orders.", 2, 3, orders.size());
        };
    }

    private Order order(Account account, Symbol symbol, OrderSide side, int qty, String price) {
        Order order = Order.builder()
                .side(side)
                .quantity(qty)
                .unitPrice(new BigDecimal(price))
                .symbol(symbol)
                .build();
        account.addOrder(order);
        return order;
    }
}
