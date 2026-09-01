# verify-runner-served.ps1 - 確認 bootRun 端點與磁碟一致
param(
    [string]$BaseUrl = 'http://localhost:8095',
    [string[]]$MustContain = @('RDBS-001'),
    [string[]]$MustNotContain = @('SAGA-001', 'PAY-001', 'TRADE-001')
)

. "$PSScriptRoot\smoke-utf8.ps1"
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$paths = @('/', '/test/runner.html', '/test/suite.js')

Write-Host "Verify runner served <- $BaseUrl" -ForegroundColor Cyan

foreach ($path in $paths) {
    $url = $BaseUrl + $path
    try {
        $body = Get-SmokeWebText -Url $url -TimeoutSec 10
    } catch {
        throw "Cannot fetch $url — is bootRun UP? ($($_.Exception.Message))"
    }
    foreach ($needle in $MustContain) {
        if ($needle -and $body -notmatch [regex]::Escape($needle)) {
            throw "${path}: missing expected '$needle'. Restart bootRun after editing static/."
        }
    }
    if ($path -eq '/') {
        if (-not (Test-EosDemoEntry -Html $body)) {
            throw '/: missing data-eos-demo-entry="true" — see EOS knowledge/frontend-demo-entry.md'
        }
        if ($body -notmatch 'Demo 入口') {
            throw '/: missing Demo 入口 label (UTF-8) — sync index.html fragment'
        }
    }
    foreach ($bad in $MustNotContain) {
        if ($bad -and $body -match [regex]::Escape($bad)) {
            throw "${path}: golden drift marker '$bad'. Align Case IDs."
        }
    }
    Write-Host "  OK $path" -ForegroundColor Green
}

Write-Host 'ALL_RUNNER_SERVED_OK' -ForegroundColor Green
