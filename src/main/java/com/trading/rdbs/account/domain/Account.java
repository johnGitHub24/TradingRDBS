package com.trading.rdbs.account.domain;

import com.trading.rdbs.order.domain.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 【職責】交易帳戶實體；3NF 中帳戶屬性只依賴 {@code id}。
 * 【技巧】{@code @OneToMany(mappedBy="account")} 為 1 端；訂單表以 {@code account_id} FK 指向此表。
 * 【概念】不在 {@code orders} 表冗餘 {@code owner_name}——避免違反 3NF 的遞移相依。
 */
@Entity
@Table(name = "accounts", uniqueConstraints = @UniqueConstraint(name = "uk_accounts_account_no", columnNames = "account_no"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", nullable = false, length = 32, unique = true)
    private String accountNo;

    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "account", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    /**
     * 【技巧】雙向關聯維護：新增訂單時同步設定 Order.account。
     */
    public void addOrder(Order order) {
        orders.add(order);
        order.setAccount(this);
    }
}
