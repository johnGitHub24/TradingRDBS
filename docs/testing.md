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

規範：EOS `knowledge/testing.md` §Fixture。

## 分層

```powershell
.\gradlew.bat test
.\gradlew.bat integrationTest
```

## Demo-ready

腳本目錄：`docs/scripts/`（見 [docs/scripts/README.md](scripts/README.md)）

```powershell
.\gradlew.bat bootRun
.\docs\scripts\run-l0-smoke.ps1
.\docs\scripts\run-api-smoke.ps1
.\docs\scripts\run-ui-smoke.ps1 -InstallDeps
.\docs\scripts\run-release-gate.ps1 -SkipCheck
```

UI Smoke 劇情：RDBS-001～006 + 種子資料檢查。
