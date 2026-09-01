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

## 分層

```powershell
.\gradlew.bat test
.\gradlew.bat integrationTest
```

## Demo-ready

```powershell
.\gradlew.bat bootRun
.\docs\run-l0-smoke.ps1
.\docs\run-api-smoke.ps1
.\docs\run-ui-smoke.ps1 -InstallDeps
.\docs\run-release-gate.ps1 -SkipCheck
```

UI Smoke 劇情：RDBS-001～006 + 種子資料檢查。
