# TradingRDBS — 專案規則（薄）

繼承：EngineeringOS eos-minimal @ **0.1.13**
公版：`EngineeringOS/eos-minimal/`
權威規格：[TradingRDBS 規格書.md](TradingRDBS%20規格書.md)

## 與公版差異

- Backend port: **8095**
- Framework: Spring Boot 3.2 · Java 21 · JPA · **Spring Security（OAuth Bearer JWT）**
- DB: H2（dev/test）／PostgreSQL（prod）
- optional-frontend: **yes**（同埠 Vue 3 靜態 `src/main/resources/static/`）
- **Demo-ready：** `bootRun` → `docs/scripts/run-release-gate.ps1`；UI Smoke `/test/runner.html`
- 驗證入口：`.\scripts\check.ps1`（`gradlew check`＝unit + integration）
- 本機 Demo：IntelliJ／Gradle `bootRun`（**勿**對 `*Application` 綠箭頭）→ 見 EOS `intellij-bootRun-portable.md`、[`docs/IntelliJ-IDE-啟動設定.md`](docs/IntelliJ-IDE-啟動設定.md)

## 本專案專屬

- Domain: **第三正規化（3NF）** 關聯模型 — Account(1) → Order(N) → Symbol(1)
- Case：RDBS-001～006、AUTH-001～003（單元＋整合成對）
- 架構：`docs/architecture.md`；DB：`docs/資料庫設計.md`；測試：`docs/testing.md`；驗證：`docs/驗證設計.md`

## 註解深度
- comment_verbosity: **detailed**
- 權威：`EngineeringOS/eos-minimal/knowledge/comments.md` §0／§3b

## Git Remote
- 帳號：`johnGitHub24`；一專案一 repo
- 規範：`EngineeringOS/eos-minimal/knowledge/專案上船-GitHub.md`

## 回寫
問題與公版改善建議 → `EngineeringOS/eos-minimal/feedback/SYNC_LOG.md`
