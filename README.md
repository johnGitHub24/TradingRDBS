# TradingRDBS

關聯式資料庫練習專案：**第三正規化（3NF）** 與 **Account(1) → Order(N) → Symbol(1)** JPA 映射。

## 文件入口

| 文件 | 說明 |
|------|------|
| [TradingRDBS 規格書.md](TradingRDBS%20規格書.md) | 主規格 |
| [docs/資料庫設計.md](docs/資料庫設計.md) | ER、3NF 說明、反模式對照 |
| [docs/architecture.md](docs/architecture.md) | 分層架構 |
| [docs/testing.md](docs/testing.md) | Case RDBS-001～006 |
| [docs/portals/service-links.html](docs/portals/service-links.html) | 服務連結（與 Console／Demo 入口同源） |
| [CLAUDE.md](CLAUDE.md) | AI 薄規則 |

## Quick Start

```powershell
.\scripts\check.ps1
.\gradlew.bat bootRun          # 終端／IntelliJ 請用 Gradle bootRun
# 瀏覽器 → http://localhost:8095/
```

> **IntelliJ：** 請用 Run → **bootRun (Gradle)**，**勿**對 `TradingRdbsApplication.java` 按綠箭頭（Windows 易 `0xC0000005`）。見 [docs/IntelliJ-IDE-啟動設定.md](docs/IntelliJ-IDE-啟動設定.md)。

### 新建交付 Smoke（必跑）

```powershell
.\scripts\check.ps1
# 終端 1：.\gradlew.bat bootRun
.\docs\run-l0-smoke.ps1
.\docs\run-smoke-l1.ps1
.\docs\run-ui-smoke.ps1 -Headed
.\docs\verify-runner-served.ps1
.\docs\run-release-gate.ps1 -SkipCheck
```

### Release Gate（真開埠）

```powershell
# 終端 1
.\gradlew.bat bootRun

# 終端 2
.\docs\run-release-gate.ps1
```

| URL | 說明 |
|-----|------|
| http://localhost:8095/ | **Vue 3 Demo**（帳戶／標的／委託 CRUD） |
| http://localhost:8095/test/runner.html | UI Smoke（RDBS-001～006） |
| http://localhost:8095/h2-console | H2（JDBC: `jdbc:h2:mem:rdbs`，sa／空白） |
| http://localhost:8095/actuator/health | 健康檢查 |

## 關聯模型（練習重點）

```
accounts (1) ──< orders >── (1) symbols
```

- 一個 **Account** 可有多筆 **Order**（`@OneToMany` / `@ManyToOne`）
- 一筆 **Order** 只指向一個 **Symbol**（`@ManyToOne` / `@OneToMany` 反向）
- **3NF**：`orders` 表只存 `account_id`、`symbol_id` FK，不冗餘 `owner_name` 或 `ticker`

## API 摘要

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/v1/accounts` | 建立帳戶 |
| GET | `/api/v1/accounts/{id}` | 帳戶＋委託列表 |
| POST | `/api/v1/symbols` | 建立標的 |
| POST | `/api/v1/orders` | 建立委託（連結 account + symbol） |
| GET | `/api/v1/orders?accountId=` | 依帳戶查委託 |
| GET | `/api/v1/orders?symbolId=` | 依標的查委託（N→1 反向） |

## Stack

Spring Boot 3.2 · Java 21 · Spring Data JPA · H2 · springdoc-openapi
