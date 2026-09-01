# IntelliJ 啟動 TradingRDBS

> 權威：EngineeringOS `eos-minimal/knowledge/intellij-bootRun-portable.md`

## 使用

Run 下拉選 **TradingRdbsApplication** 或 **bootRun (Gradle)** — 兩者皆為 Gradle **`bootRun`**，不是 `java.exe + idea_rt.jar`。

啟動後：**http://localhost:8095/**

## 必要設定（一次）

1. **File → Project Structure → Project SDK** = JDK **21**
2. **Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - **Build and run using:** **Gradle**
   - **Run tests using:** **Gradle**
   - **Gradle JVM:** Project SDK

## 0xC0000005 / idea_rt.jar

1. **Run → Edit Configurations…** → 刪除 **Spring Boot** 類型的 `TradingRdbsApplication`（保留 **Gradle** 同名項）
2. 執行：

```powershell
.\scripts\fix-intellij-run.ps1
```

3. 再選 **Gradle** 的 `TradingRdbsApplication` 或 `bootRun (Gradle)`

## 埠占用

```powershell
netstat -ano | findstr ":8095"
Stop-Process -Id <PID> -Force
```
