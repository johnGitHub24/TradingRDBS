# TradingRDBS — verification entry (portable; Windows)

. "$PSScriptRoot\env.ps1"
Set-Location (Split-Path $PSScriptRoot -Parent)

$eosHooks = Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) 'EngineeringOS\eos-minimal\hooks'
if (-not (Test-Path -LiteralPath $eosHooks)) {
    $eosHooks = Join-Path (Split-Path $PSScriptRoot -Parent) '..\EngineeringOS\eos-minimal\hooks'
}
. (Join-Path $eosHooks 'Get-EosDemoProfile.ps1')
$eosProfile = Get-EosDemoProfile -ProjectRoot (Get-Location)
$demoPresentationOn = Test-EosDemoPresentationEnabled -Profile $eosProfile

$gen = Join-Path $eosHooks 'generate-service-links.ps1'
if (Test-Path -LiteralPath $gen) {
    & $gen -ProjectRoot (Get-Location) -Quiet
}

$gradlew = if (Test-Path '.\gradlew.bat') { '.\gradlew.bat' } else { '.\gradlew' }

& $gradlew check
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($demoPresentationOn) {
    Write-Host 'aggregateJavadoc + syncDemoStaticToClasspath (demoPresentation=on)...' -ForegroundColor Cyan
    & $gradlew aggregateJavadoc syncDemoStaticToClasspath --no-daemon
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    $sync = Join-Path $eosHooks 'sync-demo-static-artifacts.ps1'
    if (Test-Path -LiteralPath $sync) {
        & $sync -ProjectRoot (Get-Location) -Quiet
    }
    Write-Host 'Tip: 改 static 後請停 bootRun 再啟動；瀏覽器 Ctrl+F5' -ForegroundColor DarkYellow
    if (Test-Path -LiteralPath $gen) {
        & $gen -ProjectRoot (Get-Location) -Quiet
    }
}
else {
    Write-Host 'demoPresentation=off — skip javadoc/static sync (see docs/eos-demo.profile.json)' -ForegroundColor DarkYellow
}

Write-Host 'TradingRDBS check OK' -ForegroundColor Green
