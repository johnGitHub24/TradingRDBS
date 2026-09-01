# TradingRDBS — 專案規則（薄）

繼承：EngineeringOS eos-minimal @ **0.1.13**
公版：`EngineeringOS/eos-minimal/`
權威規格：[TradingRDBS 規格書.md](TradingRDBS%20規格書.md)

## 與公版差異

- Backend port: **8095**
- Framework: Spring Boot 3.2 · Java 21 · JPA
- DB: H2（dev/test）／PostgreSQL（prod）
- optional-frontend: **no**
- 驗證入口：`.\scripts\check.ps1`
- 本機 Demo：Gradle `bootRun` → http://localhost:8095

## 本專案專屬

- Domain: 3NF + Account(1)→Order(N)→Symbol(1)
- Case：RDBS-001～006（單元＋整合成對）
- 架構：`docs/architecture.md`；DB：`docs/資料庫設計.md`

## 註解深度
- comment_verbosity: **detailed**

## Git Remote
- 帳號：`johnGitHub24`

## 回寫
→ `EngineeringOS/eos-minimal/feedback/SYNC_LOG.md`
