# smoke-utf8.ps1 — Demo-ready Smoke 共用（委派 encoding-io.ps1 · SSOT: knowledge/encoding.md）
param()

. "$PSScriptRoot\encoding-io.ps1"
Initialize-EosConsoleUtf8

function Get-SmokeWebText {
    param(
        [Parameter(Mandatory)][string]$Url,
        [int]$TimeoutSec = 10
    )
    Get-EosWebText -Url $Url -TimeoutSec $TimeoutSec
}

function Test-EosDemoEntry {
    param([Parameter(Mandatory)][string]$Html)
    return ($Html -match 'data-eos-demo-entry\s*=\s*["'']true["'']')
}
