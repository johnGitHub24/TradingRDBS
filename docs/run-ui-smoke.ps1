# run-ui-smoke.ps1 - L1 UI Smoke (Puppeteer or Node fetch fallback)
param(
    [string]$BaseUrl = 'http://localhost:8095',
    [switch]$StartApp,
    [switch]$InstallDeps,
    [switch]$Headed,
    [int]$HealthTimeoutSec = 120
)

. "$PSScriptRoot\smoke-utf8.ps1"
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location $projectRoot

$uiSmokeDir = Join-Path $PSScriptRoot 'ui-smoke'
$script:bootJob = $null

function Stop-BootJob {
    if ($script:bootJob) {
        Write-Host 'Stopping bootRun job...' -ForegroundColor Yellow
        Stop-Job $script:bootJob -ErrorAction SilentlyContinue
        Remove-Job $script:bootJob -Force -ErrorAction SilentlyContinue
    }
}

try {
    if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
        throw 'Node.js required. Use .\docs\run-api-smoke.ps1 for API-only L1.'
    }

    $healthUrl = '{0}/actuator/health' -f $BaseUrl.TrimEnd('/')
    $healthy = $false
    try {
        $r = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 3
        if ($r.status -eq 'UP') { $healthy = $true }
    } catch { }

    if (-not $healthy -and $StartApp) {
        Write-Host "Starting bootRun ($BaseUrl)..." -ForegroundColor Yellow
        $script:bootJob = Start-Job -ScriptBlock {
            Set-Location $using:projectRoot
            & .\gradlew.bat bootRun 2>&1
        }
        $deadline = (Get-Date).AddSeconds($HealthTimeoutSec)
        while ((Get-Date) -lt $deadline) {
            Start-Sleep -Seconds 3
            try {
                $r = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
                if ($r.status -eq 'UP') { $healthy = $true; break }
            } catch { }
        }
    }

    if (-not $healthy) {
        throw "Service not ready: $healthUrl. Run gradlew bootRun or use -StartApp"
    }

    $puppeteerPath = Join-Path $uiSmokeDir 'node_modules\puppeteer'
    $usePuppeteer = (Test-Path $puppeteerPath) -and -not $env:SMOKE_USE_FETCH

    if (-not $usePuppeteer -and $InstallDeps) {
        if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
            Write-Host 'npm not found — using Node fetch smoke' -ForegroundColor Yellow
        } else {
            Write-Host 'Installing puppeteer (optional, 180s timeout)...' -ForegroundColor Yellow
            Push-Location $uiSmokeDir
            try {
                $job = Start-Job { Set-Location $using:uiSmokeDir; npm install --no-fund --no-audit 2>&1 }
                $done = Wait-Job $job -Timeout 180
                if (-not $done) {
                    Stop-Job $job -ErrorAction SilentlyContinue; Remove-Job $job -Force
                    Write-Host 'puppeteer timeout — fallback to Node fetch smoke' -ForegroundColor Yellow
                } else {
                    Receive-Job $job | Out-Null
                    Remove-Job $job -Force
                    $usePuppeteer = Test-Path $puppeteerPath
                }
            } finally {
                Pop-Location
            }
        }
    }

    if ($usePuppeteer) {
        $mjs = Join-Path $uiSmokeDir 'run-headless.mjs'
        Push-Location $uiSmokeDir
        try {
            $nodeArgs = @($mjs, "--baseUrl=$BaseUrl")
            if ($Headed) { $nodeArgs += '--headed' }
            & node @nodeArgs
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        } finally {
            Pop-Location
        }
    } else {
        Write-Host 'Running Node fetch smoke (suite.js equivalent)...' -ForegroundColor Cyan
        $fetchMjs = Join-Path $uiSmokeDir 'run-fetch-smoke.mjs'
        & node $fetchMjs "--baseUrl=$BaseUrl"
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }

    Write-Host ''
    Write-Host '[OK] UI Smoke L1 passed' -ForegroundColor Green
} finally {
    Stop-BootJob
}
