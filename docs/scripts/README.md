# docs/scripts — 延伸層腳本

Pure 入口仍在專案根 `scripts/check.ps1`；本目錄放 Demo-ready Smoke／Release 編排（EOS：`knowledge/documentation.md` §docs/scripts）。

| 腳本 | 用途 |
|------|------|
| `run-l0-smoke.ps1` | L0：health、UI、demo-entry、**SVP／藍圖**、runner |
| `run-api-smoke.ps1` | L1 API 劇情（RDBS-001～006） |
| `run-ui-smoke.ps1` | L1 UI（Puppeteer／fetch fallback） |
| `run-smoke-l1.ps1` | L0 + API + UI 編排 |
| `run-release-gate.ps1` | check + L1（Release） |
| `verify-runner-served.ps1` | 確認 bootRun 端點與 static 一致 |
| `smoke-utf8.ps1` / `encoding-io.ps1` | UTF-8 共用（Smoke 腳本 dot-source） |

UI 資產：`docs/ui-smoke/`。測試 JSON：`docs/test-data/`。
