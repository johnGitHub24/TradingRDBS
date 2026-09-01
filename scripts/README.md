# scripts/

| 腳本 | 用途 |
|------|------|
| `check.ps1` / `check.sh` | 驗證入口：`gradlew check`（unit + integration） |
| `fix-intellij-run.ps1` | 修復 IntelliJ 預設 Run = Gradle `bootRun`（勿用 Application 綠箭頭） |
| `env.ps1` / `env.sh` | 載入 JDK 21 與 UTF-8 環境 |
| `portable-env.ps1` | 本機 portable env（勿寫死磁碟路徑） |

```powershell
.\scripts\check.ps1
.\gradlew.bat bootRun
```
