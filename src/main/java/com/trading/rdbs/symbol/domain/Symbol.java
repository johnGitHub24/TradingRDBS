package com.trading.rdbs.symbol.domain;

import com.trading.rdbs.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 【職責】可交易標的（股票代碼）實體；3NF 中標的屬性只依賴 {@code id}。
 * 【技巧】{@code ticker} 為業務唯一鍵；{@code company_name}、{@code exchange_code} 直接依賴主鍵。
 * 【概念】不在 {@code orders} 表冗餘 {@code ticker}／{@code company_name}。
 */
@Entity
@Table(name = "symbols", uniqueConstraints = @UniqueConstraint(name = "uk_symbols_ticker", columnNames = "ticker"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Symbol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 16, unique = true)
    private String ticker;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "exchange_code", nullable = false, length = 16)
    private String exchangeCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "symbol", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
