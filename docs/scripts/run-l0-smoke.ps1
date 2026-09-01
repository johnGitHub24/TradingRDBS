# run-l0-smoke.ps1 - L0 probe (health + UI + demo-entry + runner) — 新建專案必跑
param(
    [string]$BaseUrl = 'http://localhost:8095'
)

. "$PSScriptRoot\smoke-utf8.ps1"
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$healthUrl = '{0}/actuator/health' -f $BaseUrl

Write-Host "L0 Smoke -> $BaseUrl" -ForegroundColor Cyan

$h = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
if ($h.status -ne 'UP') { throw "health not UP: $($h.status)" }
Write-Host 'health=UP' -ForegroundColor Green

$homeHtml = Get-SmokeWebText -Url $BaseUrl -TimeoutSec 5
Write-Host 'UI=200' -ForegroundColor Green

if (-not (Test-EosDemoEntry -Html $homeHtml)) {
    throw "UI home missing data-eos-demo-entry=`"true`" — merge static-demo-entry.fragment.html.template before #app (see knowledge/frontend-demo-entry.md)"
}
if ($homeHtml -notmatch 'Demo 入口') {
    throw "UI home missing visible Demo 入口 label (UTF-8) — sync index.html fragment"
}
Write-Host 'demo-entry=OK' -ForegroundColor Green

$runnerHtml = Get-SmokeWebText -Url ('{0}/test/runner.html' -f $BaseUrl) -TimeoutSec 5
if ($runnerHtml.Length -lt 1) { throw 'runner empty' }
Write-Host 'runner=200' -ForegroundColor Green

Write-Host ''
Write-Host 'ALL_L0_SMOKE_OK' -ForegroundColor Green
