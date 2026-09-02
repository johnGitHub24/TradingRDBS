# Testing — TradingRDBS

## 入口

```powershell
.\scripts\check.ps1
```

= `gradlew check` = unit (`test`) + integration (`integrationTest`)

## Case 成對

| Case ID | 行為 | 單元 | 整合 |
|---------|------|------|------|
| RDBS-001 | 建立帳戶 | AccountServiceTest | TradingRdbsIntegrationTest |
| RDBS-002 | 建立標的 | SymbolServiceTest | TradingRdbsIntegrationTest |
| RDBS-003 | 建立委託 1→N→1 | OrderServiceTest | TradingRdbsIntegrationTest |
| RDBS-004 | 查帳戶含 orders | AccountServiceTest | TradingRdbsIntegrationTest |
| RDBS-005 | 依 symbolId 列委託 | OrderServiceTest | TradingRdbsIntegrationTest |
| RDBS-006 | 404 | AccountServiceTest, OrderServiceTest | TradingRdbsIntegrationTest |
| AUTH-001 | 登入取得 JWT | JwtTokenProviderTest（簽發） | AuthIntegrationTest |
| AUTH-002 | 無 token／錯誤密碼 401 | — | AuthIntegrationTest |
| AUTH-003 | JWT 簽發驗證 | JwtTokenProviderTest | — |
| SVP-DEMO-001～006 | 系統檢測 Panel + 藍圖 + FinTechDemo 風格 test-reports | — | DemoReadyIntegrationTest |

## Fixture（外部 JSON）

載入器：`src/test/java/com/trading/rdbs/support/RdbsTestFixtures.java`

| domain | JSON | 用途 |
|--------|------|------|
| `account/` | `RDBS-001-SUCCESS.json` | 單元建立帳戶 |
| `account/` | `RDBS-001-INTEGRATION.json` | 整合 POST accounts |
| `symbol/` | `RDBS-002-SUCCESS.json` | 單元建立標的 |
| `symbol/` | `RDBS-002-INTEGRATION.json` | 整合 POST symbols |
| `order/` | `RDBS-003-SUCCESS.json` | 單元建立委託（含 FK id） |
| `order/` | `RDBS-003-BODY.json` | 整合 POST orders（執行期補 accountId／symbolId） |
| `order/` | `RDBS-006-NOT-FOUND-ACCOUNT.json` | 單元 404 account |
| `auth/` | `AUTH-001-SUCCESS.json` | 整合／Smoke 登入 |
| `auth/` | `AUTH-002-BAD-CREDENTIALS.json` | 整合錯誤密碼 401 |

規範：EOS `knowledge/testing.md` §Fixture。

## 分層

```powershell
.\gradlew.bat test
.\gradlew.bat integrationTest
```

## Demo-ready

腳本目錄：`docs/scripts/`（見 [docs/scripts/README.md](scripts/README.md)）

**系統檢測 Panel + 藍圖**（EOS `service-verification-panel.md`）：**必跑靜態頁** Javadoc、單元／整合測試（FinTechDemo 風格 `test-reports.html`）；`check.ps1` = test + aggregateJavadoc + sync。

```powershell
.\gradlew.bat bootRun
.\docs\scripts\run-l0-smoke.ps1
.\docs\scripts\run-api-smoke.ps1
.\docs\scripts\run-ui-smoke.ps1 -InstallDeps
.\docs\scripts\run-release-gate.ps1 -SkipCheck
```

UI Smoke 劇情：AUTH-001～002 + RDBS-001～006 + 種子資料檢查。

驗證設計（OAuth Bearer JWT）：[docs/驗證設計.md](驗證設計.md)
