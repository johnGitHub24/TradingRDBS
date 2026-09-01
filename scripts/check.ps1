# TradingRDBS — verification entry (portable; Windows)

. "$PSScriptRoot\env.ps1"
Set-Location (Split-Path $PSScriptRoot -Parent)
if (Test-Path '.\gradlew.bat') {
    .\gradlew.bat check
} elseif (Test-Path '.\gradlew') {
    .\gradlew check
} else {
    Write-Error 'gradlew not found'
}
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host 'TradingRDBS check OK' -ForegroundColor Green
