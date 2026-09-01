# run-smoke-l1.ps1 - L1: L0 + API + UI（有 UI 時 UI 必跑）
param(
    [string]$BaseUrl = 'http://localhost:8095',
    [switch]$SkipUi,
    [switch]$InstallUiDeps
)

. "$PSScriptRoot\smoke-utf8.ps1"
$here = $PSScriptRoot

Write-Host '=== L1 Smoke (L0 + API + UI) ===' -ForegroundColor Yellow

& (Join-Path $here 'run-l0-smoke.ps1') -BaseUrl $BaseUrl
if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& (Join-Path $here 'run-api-smoke.ps1') -BaseUrl $BaseUrl
if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$uiAuto = 'N/A'
if (-not $SkipUi) {
    $uiArgs = @{ BaseUrl = $BaseUrl }
    if ($InstallUiDeps) { $uiArgs['InstallDeps'] = $true }
    & (Join-Path $here 'run-ui-smoke.ps1') @uiArgs
    if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) {
        Write-Host 'UI automation=FAIL' -ForegroundColor Red
        exit $LASTEXITCODE
    }
    $uiAuto = 'PASS'
}

Write-Host ''
Write-Host '=== L1 Smoke complete ===' -ForegroundColor Green
Write-Host "UI automation=$uiAuto"
