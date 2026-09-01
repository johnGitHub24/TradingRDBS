package com.trading.rdbs.order.domain;

import com.trading.rdbs.account.domain.Account;
import com.trading.rdbs.symbol.domain.Symbol;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 【職責】委託單實體；串接 Account(1) 與 Symbol(1) 的 N 端。
 * 【技巧】只存 {@code account_id}、{@code symbol_id} FK 及委託自身欄位（side/qty/price）。
 * 【概念】3NF：非鍵欄位僅依賴主鍵 {@code id}，不複製帳戶名或股票名稱。
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_account_id", columnList = "account_id"),
        @Index(name = "idx_orders_symbol_id", columnList = "symbol_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private OrderSide side;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal unitPrice;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
