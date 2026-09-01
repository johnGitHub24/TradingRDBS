# Architecture — TradingRDBS

## 分層

```
com.trading.rdbs
├── account/     AccountController → AccountService → AccountRepository
├── symbol/      SymbolController → SymbolService → SymbolRepository
├── order/       OrderController → OrderService → OrderRepository
├── common/      GlobalExceptionHandler, ResourceNotFoundException
└── config/      DataSeeder, OpenApiConfig
```

## JPA 關聯（練習核心）

| 端 | 註解 | 說明 |
|----|------|------|
| Account.orders | `@OneToMany(mappedBy="account")` | 1 端 |
| Order.account | `@ManyToOne` + `account_id` | N 端 |
| Order.symbol | `@ManyToOne` + `symbol_id` | N 端 |
| Symbol.orders | `@OneToMany(mappedBy="symbol")` | 1 端（反向） |

建立委託時：`account.addOrder(order)` 維護雙向關聯，再 `orderRepository.save(order)`。

## 3NF 與 DTO

- **Entity / 表**：只存 FK，不冗餘父表欄位
- **OrderResponse**：Service 層透過已載入的關聯組裝 `accountNo`、`ticker` 供 API 閱讀

## 種子資料

`DataSeeder`：2 帳戶、3 標的、4 筆委託，展示多帳戶共用同一 Symbol 的 N→1 情境。
