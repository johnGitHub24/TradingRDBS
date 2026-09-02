# TradingRDBS — verification entry (portable; Windows)

. "$PSScriptRoot\env.ps1"
Set-Location (Split-Path $PSScriptRoot -Parent)

$eosHooks = Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) 'EngineeringOS\eos-minimal\hooks'
if (-not (Test-Path -LiteralPath $eosHooks)) {
    $eosHooks = Join-Path (Split-Path $PSScriptRoot -Parent) '..\EngineeringOS\eos-minimal\hooks'
}
$gen = Join-Path $eosHooks 'generate-service-links.ps1'
if (Test-Path -LiteralPath $gen) {
    & $gen -ProjectRoot (Get-Location) -Quiet
}

$gradlew = if (Test-Path '.\gradlew.bat') { '.\gradlew.bat' } else { '.\gradlew' }

& $gradlew check
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host 'aggregateJavadoc + syncDemoStaticToClasspath (Javadoc / test reports -> bootRun classpath)...' -ForegroundColor Cyan
& $gradlew aggregateJavadoc syncDemoStaticToClasspath --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host 'Tip: 若 bootRun 已在跑，改 static 後請停掉再 gradlew bootRun（或 Ctrl+F5 無效時必重啟）' -ForegroundColor DarkYellow

if (Test-Path -LiteralPath $gen) {
    & $gen -ProjectRoot (Get-Location) -Quiet
}

Write-Host 'TradingRDBS check OK' -ForegroundColor Green
