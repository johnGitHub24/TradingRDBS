# TradingRDBS 規格書

## 1. 目的

練習 **關聯式資料庫正規化（至第三正規化 3NF）** 與 **JPA 1→N→1 映射**。

## 2. 領域模型

| 實體 | 說明 |
|------|------|
| Account | 交易帳戶 |
| Order | 委託單（N 端，連接 Account 與 Symbol） |
| Symbol | 可交易標的（股票代碼） |

關聯：

- Account **1 — N** Order
- Symbol **1 — N** Order

## 3. 第三正規化（3NF）

1. **1NF**：欄位原子性（無重複群組）
2. **2NF**：非鍵欄位完全依賴主鍵（使用 surrogate `id`）
3. **3NF**：非鍵欄位不遞移依賴其他非鍵欄位

### 反模式（本專案刻意不做）

在 `orders` 表同時存 `owner_name`、`ticker`、`company_name`：

- `owner_name` 依賴 `account_no` 而非 `order.id` → 違反 3NF
- `company_name` 依賴 `ticker` 而非 `order.id` → 違反 3NF

### 正規化做法

- `accounts`：帳戶屬性
- `symbols`：標的屬性
- `orders`：僅 FK + 委託自身欄位（side、quantity、unit_price）

API 回應 DTO 可 JOIN 帶出 `accountNo`、`ticker` 供閱讀；**持久層**仍只存 FK。

## 4. 技術

- Spring Boot 3.2、Java 21、JPA
- H2（dev/test）、PostgreSQL（prod）
- Port **8095**

## 5. API

Base: `/api/v1`

詳見 [README.md](README.md) 與 Swagger。

## 6. 測試 Case

| ID | 說明 |
|----|------|
| RDBS-001 | 建立帳戶 |
| RDBS-002 | 建立標的 |
| RDBS-003 | 建立委託（1→N→1 連結） |
| RDBS-004 | 查帳戶含委託列表 |
| RDBS-005 | 依 symbolId 查委託（N→1） |
| RDBS-006 | 404 找不到資源 |

## 7. 驗證

```powershell
.\scripts\check.ps1
```
