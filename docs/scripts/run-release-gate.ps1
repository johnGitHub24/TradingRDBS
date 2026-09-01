# run-release-gate.ps1 - check + L1 Smoke（新建交付勿 -SkipSmoke）
param(
    [string]$BaseUrl = 'http://localhost:8095',
    [switch]$SkipCheck,
    [switch]$SkipSmoke,
    [switch]$SkipUi,
    [switch]$InstallUiDeps
)

. "$PSScriptRoot\smoke-utf8.ps1"
$ErrorActionPreference = 'Stop'
$docsRoot = Split-Path $PSScriptRoot -Parent
$projectRoot = Split-Path $docsRoot -Parent
$here = $PSScriptRoot

Write-Host '=== Release Gate (check + L1 Smoke) ===' -ForegroundColor Yellow

if (-not $SkipCheck) {
    Write-Host '[1/2] Pure check...' -ForegroundColor Cyan
    & (Join-Path $projectRoot 'scripts\check.ps1')
    if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    Write-Host '[OK] check passed' -ForegroundColor Green
}

if ($SkipSmoke) {
    Write-Host 'ALL_RELEASE_GATE_CHECK_OK' -ForegroundColor Green
    exit 0
}

Write-Host '[2/2] L1 Smoke...' -ForegroundColor Cyan
$healthUrl = '{0}/actuator/health' -f $BaseUrl.TrimEnd('/')
try {
    $h = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
    if ($h.status -ne 'UP') { throw "health=$($h.status)" }
} catch {
    Write-Host "Service not ready. Start: .\gradlew.bat bootRun" -ForegroundColor Red
    exit 1
}

$smokeArgs = @{ BaseUrl = $BaseUrl }
if ($SkipUi) { $smokeArgs['SkipUi'] = $true }
if ($InstallUiDeps) { $smokeArgs['InstallUiDeps'] = $true }
& (Join-Path $here 'run-smoke-l1.ps1') @smokeArgs
if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ''
Write-Host 'ALL_RELEASE_GATE_OK' -ForegroundColor Green
