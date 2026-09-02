# run-l0-smoke.ps1 - L0 probe (health + UI; 教學展示層依 eos-demo.profile.json)
param(
    [string]$BaseUrl = 'http://localhost:8095'
)

. "$PSScriptRoot\smoke-utf8.ps1"
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
$healthUrl = '{0}/actuator/health' -f $BaseUrl

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$eosHooks = Join-Path (Split-Path (Split-Path $projectRoot -Parent) -Parent) 'EngineeringOS\eos-minimal\hooks'
if (-not (Test-Path -LiteralPath $eosHooks)) {
    $eosHooks = Join-Path (Split-Path $projectRoot -Parent) 'EngineeringOS\eos-minimal\hooks'
}

$demoPresentationOn = $true
$profileScript = Join-Path $eosHooks 'Get-EosDemoProfile.ps1'
if (Test-Path -LiteralPath $profileScript) {
    . $profileScript
    $eosProfile = Get-EosDemoProfile -ProjectRoot $projectRoot
    $demoPresentationOn = Test-EosDemoPresentationEnabled -Profile $eosProfile
}

function Write-L0Skip {
    param([string]$Name)
    Write-Host "SKIP $Name (demoPresentation=off)" -ForegroundColor DarkYellow
}

Write-Host "L0 Smoke -> $BaseUrl (presentationLayer=$demoPresentationOn)" -ForegroundColor Cyan

$h = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 5
if ($h.status -ne 'UP') { throw "health not UP: $($h.status)" }
Write-Host 'health=UP' -ForegroundColor Green

$homeHtml = Get-SmokeWebText -Url $BaseUrl -TimeoutSec 5
Write-Host 'UI=200' -ForegroundColor Green

if ($demoPresentationOn) {
    if (-not (Test-EosDemoEntry -Html $homeHtml)) {
        throw "UI home missing data-eos-demo-entry=`"true`" — merge static-demo-entry.fragment.html.template before #app (see knowledge/frontend-demo-entry.md)"
    }
    if ($homeHtml -notmatch 'Demo 入口') {
        throw "UI home missing visible Demo 入口 label (UTF-8) — sync index.html fragment"
    }
    Write-Host 'demo-entry=OK' -ForegroundColor Green

    if ($homeHtml -notmatch 'id="eos-svp-mount"') {
        throw 'UI home missing #eos-svp-mount — see service-verification-panel.md'
    }
    if ($homeHtml -notmatch 'service-verification-panel\.js') {
        throw 'UI home missing service-verification-panel.js script'
    }
    if ($homeHtml -notmatch '/blueprint/') {
        throw 'UI home missing /blueprint/ link (系統運作藍圖)'
    }
    Write-Host 'svp-header=OK' -ForegroundColor Green

    $manifest = Invoke-RestMethod -Uri ('{0}/service-links.manifest.json' -f $BaseUrl) -TimeoutSec 5
    if (-not $manifest.groups -or @($manifest.groups).Count -lt 1) {
        throw 'service-links.manifest.json missing groups'
    }
    Write-Host 'service-links.manifest=OK' -ForegroundColor Green

    $panelResp = Invoke-WebRequest -Uri ('{0}/service-verification-panel.js' -f $BaseUrl) -UseBasicParsing -TimeoutSec 5
    if ($panelResp.StatusCode -ne 200) { throw 'service-verification-panel.js not 200' }
    Write-Host 'service-verification-panel.js=200' -ForegroundColor Green

    $blueprintHtml = Get-SmokeWebText -Url ('{0}/blueprint/' -f $BaseUrl) -TimeoutSec 5
    if ($blueprintHtml.Length -lt 1) { throw 'blueprint empty' }
    if ($blueprintHtml -notmatch 'mermaid' -and $blueprintHtml -notmatch '系統運作藍圖') {
        throw 'blueprint missing Mermaid / title — sync static/blueprint/index.html'
    }
    Write-Host 'blueprint=OK' -ForegroundColor Green

    $javadocResp = Invoke-WebRequest -Uri ('{0}/docs/javadoc/index.html' -f $BaseUrl) -UseBasicParsing -TimeoutSec 5
    if ($javadocResp.StatusCode -ne 200) { throw 'javadoc not 200 — run scripts/check.ps1 (aggregateJavadoc + sync)' }
    Write-Host 'javadoc=200' -ForegroundColor Green

    $trHtml = Get-SmokeWebText -Url ('{0}/docs/portals/test-reports.html' -f $BaseUrl) -TimeoutSec 5
    if ($trHtml -notmatch 'id="unit"') { throw 'test-reports missing #unit section' }
    if ($trHtml -notmatch 'id="integration"') { throw 'test-reports missing #integration section' }
    if ($trHtml -notmatch 'mod-grid') { throw 'test-reports not FinTechDemo style (mod-grid)' }
    Write-Host 'test-reports=OK' -ForegroundColor Green

    $unitReport = Invoke-WebRequest -Uri ('{0}/docs/reports/unit/index.html' -f $BaseUrl) -UseBasicParsing -TimeoutSec 5
    if ($unitReport.StatusCode -ne 200) { throw 'unit report not 200 — run scripts/check.ps1 then restart bootRun' }
    Write-Host 'unit-report=200' -ForegroundColor Green

    $intReport = Invoke-WebRequest -Uri ('{0}/docs/reports/integration/index.html' -f $BaseUrl) -UseBasicParsing -TimeoutSec 5
    if ($intReport.StatusCode -ne 200) { throw 'integration report not 200 — run scripts/check.ps1 then restart bootRun' }
    Write-Host 'integration-report=200' -ForegroundColor Green
}
else {
    Write-L0Skip 'demo-entry'
    Write-L0Skip 'svp-header'
    Write-L0Skip 'service-links.manifest'
    Write-L0Skip 'blueprint'
    Write-L0Skip 'javadoc'
    Write-L0Skip 'test-reports'
    Write-L0Skip 'unit-report'
    Write-L0Skip 'integration-report'
}

$runnerHtml = Get-SmokeWebText -Url ('{0}/test/runner.html' -f $BaseUrl) -TimeoutSec 5
if ($runnerHtml.Length -lt 1) { throw 'runner empty' }
Write-Host 'runner=200' -ForegroundColor Green

Write-Host ''
Write-Host 'ALL_L0_SMOKE_OK' -ForegroundColor Green
